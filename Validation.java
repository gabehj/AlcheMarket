
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

public class Validation {
    private final Game game;
    private final ConsoleView view;
    private final List<String> selected = new ArrayList<>();
    private String greeting;
    private int haggle;
    private boolean rookStory;

    public Validation(Game game, ConsoleView view) {
        this.game = game;
        this.view = view;
    }

    public void run() {
        openTurn("");
        view.prompt(game.getPlayerName());
        while (processLine(view.readLine())) {
            view.prompt(game.getPlayerName());
        }
    }

    public boolean processLine(String line) {
        if (line == null || line.strip().equalsIgnoreCase("quit")) {
            return false;
        }
        String input = line.strip();
        if (game.isDayClosed()) {
            if (game.isOver() || input.equalsIgnoreCase("q")) {
                return false;
            }
            if (input.isEmpty()) {
                game.startNextDay();
                openTurn("");
            }
            else {
                view.showMessage("Press Enter for the next day, or Q to quit.");
            }
            return true;
        }
        if (game.isOver()) {
            view.showMessage("The game has ended.");
            return false;
        }
        if (input.equalsIgnoreCase("q")) {
            return false;
        }
        if (game.currentCustomer() instanceof RookVisit) {
            return handleRook(input);
        }
        if (game.currentCustomer() instanceof AdvancedOrder
            && (input.equalsIgnoreCase("t") || input.equalsIgnoreCase("story"))) {
            rookStory = !rookStory;
            redraw("");
            return true;
        }
        if (input.equalsIgnoreCase("d") || input.equalsIgnoreCase("decline")) {
            game.declineCustomer();
            selected.clear();
            return openTurn("Visitor moved on.");
        }
        String[] words = input.split("\\s+");
        if (words[0].equalsIgnoreCase("haggle") || words[0].equalsIgnoreCase("h")) {
            try {
                if (words.length != 2) {
                    throw new IllegalArgumentException("Use haggle 0 or +/-5, 15, 20, or 50.");
                }
                int rate = Integer.parseInt(words[1]);
                Game.validateHaggle(rate);
                haggle = rate;
                redraw("Trade terms updated.");
            }
            catch (IllegalArgumentException error) {
                redraw("Invalid selection. Use haggle 0 or +/-5, 15, 20, or 50.");
            }
            return true;
        }
        if ((game.currentCustomer() instanceof HerbVendor || game.currentCustomer() instanceof RecipeDealer)
            && !input.equalsIgnoreCase("q")) {
            String[] purchase = input.split("\\s+");
            boolean dealer = game.currentCustomer() instanceof RecipeDealer;
            String feedback;
            try {
                if (purchase.length != (dealer ? 2 : 3) || !purchase[0].equalsIgnoreCase("buy")) {
                    throw new IllegalArgumentException(
                        dealer ? "Use buy <recipe> or D to move on."
                            : "Use buy <offer> <quantity> or D to move on.");
                }
                int item = Integer.parseInt(purchase[1]);
                if (dealer) {
                    List<String> offers = game.recipeOffers();
                    int cost = game.buyRecipe(item, haggle);
                    feedback = "Purchased " + offers.get(item - 1) + " recipe for " + cost
                        + " gold. Added to the recipe book.";
                }
                else {
                    int quantity = Integer.parseInt(purchase[2]);
                    int cost = game.buyHerb(item, quantity, haggle);
                    String herb = game.vendorOffers().get(item - 1);
                    feedback = "Purchased " + quantity + " x " + herb + " for " + cost + " gold.";
                }
                haggle = 0;
                if (game.isOver()) {
                    view.refresh();
                    view.showFeedback(feedback);
                    view.showScore(game.getReputation(), game.getDiscoveredPotionCount());
                    return false;
                }
                redraw(feedback);
            }
            catch (NumberFormatException error) {
                redraw("Invalid selection. Enter numeric offer and quantity values.");
            }
            catch (IllegalArgumentException error) {
                redraw("Invalid selection. " + error.getMessage());
            }
            return true;
        }
        if (input.equalsIgnoreCase("a") || input.equalsIgnoreCase("accept")) {
            redraw("Order accepted. Select three ingredients.");
            return true;
        }
        switch (input.toLowerCase(Locale.ROOT)) {
            case "b": input = "brew"; break;
            case "r": input = "recipes"; break;
            case "u": input = "undo"; break;
            case "x": input = "reset"; break;
            case "q": return false;
            default: break;
        }
        String[] command = input.split("\\s+", 2);
        switch (input.toLowerCase(Locale.ROOT)) {
            case "recipes":
                view.showRecipeBook(game.getDiscoveredRecipes(), game.getRecipeTips(),
                    game.getAdvancedFormulas());
                view.showMessage("Press Enter to return to the counter.");
                if (view.readLine() == null) {
                    return false;
                }
                redraw("");
                return true;
            case "reset":
                selected.clear();
                redraw("Cauldron cleared.");
                return true;
            case "undo":
                if (selected.isEmpty()) {
                    redraw("The cauldron is already empty.");
                }
                else {
                    selected.remove(selected.size() - 1);
                    redraw("Last ingredient removed.");
                }
                return true;
            default:
                break;
        }
        String feedback;
        Game.ServiceResult result;
        try {
            Customer customer = game.currentCustomer();
            if (customer instanceof AdvancedOrder && input.equalsIgnoreCase("combine")) {
                result = game.combineAdvancedOrder(haggle);
                feedback = view.describeOutcome(customer, result);
                selected.clear();
                return openTurn(feedback);
            }
            if (!command[0].equalsIgnoreCase("brew")) {
                selectIngredient(input);
                return true;
            }
            List<String> recipe = command.length == 2 ? parseRecipe(command[1]) : selected;
            if (customer instanceof AdvancedOrder) {
                String component = game.brewAdvancedComponent(recipe);
                selected.clear();
                redraw("Brewed " + component + ". Bottle set aside.");
                return true;
            }
            result = game.serveWithDetails(recipe, haggle);
            selected.clear();
            feedback = view.describeOutcome(customer, result);
        }
        catch (IllegalArgumentException error) {
            redraw("Invalid selection. " + error.getMessage());
            return true;
        }
        catch (NoSuchElementException error) {
            view.showMessage("No customers are waiting.");
            return false;
        }
        catch (IllegalStateException error) {
            view.showMessage(error.getMessage());
            return false;
        }
        if (game.isOver()) {
            view.refresh();
            if (result.getOutcome() == Game.Outcome.POISON) {
                view.showPoisonScene();
            }
            view.showFeedback(feedback);
            view.showScore(game.getReputation(), game.getDiscoveredPotionCount());
            return false;
        }
        return openTurn(feedback);
    }

    private boolean openTurn(String feedback) {
        haggle = 0;
        rookStory = false;
        if (game.isDayClosed()) {
            view.refresh();
            view.showFeedback(feedback);
            view.showDailySummary(game.getDay(), game.getGold(), game.getDailySales(),
                game.getDailyGoldChange(), game.getReputation());
        }
        else {
            greeting = game.currentCustomer() instanceof HerbVendor || game.currentCustomer() instanceof RecipeDealer
                ? null : view.greetingFor(game.currentCustomer());
            redraw(feedback);
        }
        return true;
    }

    private boolean handleRook(String input) {
        RookVisit rook = (RookVisit) game.currentCustomer();
        try {
            switch (input.toLowerCase(Locale.ROOT)) {
                case "t":
                case "story":
                    rookStory = !rookStory;
                    redraw("");
                    return true;
                case "a":
                case "play":
                    game.drawWithRook();
                    redraw("");
                    return true;
                case "gold":
                case "herb":
                    if (rook.getPlayerCard() <= rook.getRookCard()) {
                        throw new IllegalStateException("Win the draw before claiming a prize.");
                    }
                    boolean herb = input.equalsIgnoreCase("herb");
                    game.finishRookRound(herb);
                    return openTurn(herb ? "Rook hands over 1 Moonpetal."
                        : "Rook pays you " + rook.getStake() + " gold.");
                case "loan":
                    game.lendToRook();
                    return openTurn("Rook: I'll be back tomorrow. You can put that in ink.");
                case "collect":
                    game.collectRookRepayment();
                    return openTurn("Rook delivered 2 Moonpetal.");
                case "d":
                case "decline":
                    game.declineCustomer();
                    return openTurn("Rook: You could've said that before I wiped the cards.");
                case "":
                    if (rook.hasDrawn() && rook.getPlayerCard() <= rook.getRookCard()) {
                        game.finishRookRound(false);
                        return openTurn(rook.getPlayerCard() == rook.getRookCard()
                            ? "Draw. No gold changed hands." : "Rook won " + rook.getStake() + " gold.");
                    }
                    break;
                default:
                    break;
            }
            redraw("Invalid selection. Use an action shown on the table.");
        }
        catch (IllegalArgumentException | IllegalStateException error) {
            redraw("Invalid selection. " + error.getMessage());
        }
        return true;
    }

    private void selectIngredient(String input) {
        String ingredient = resolveIngredient(input);
        if (game.stockOf(ingredient) == 0) {
            throw new IllegalArgumentException("Out of stock: " + ingredient);
        }
        if (selected.contains(ingredient)) {
            throw new IllegalArgumentException("That ingredient is already in the cauldron.");
        }
        if (selected.size() == Game.RECIPE_SIZE) {
            throw new IllegalArgumentException("Cauldron is full. Use brew, undo, or reset.");
        }
        selected.add(ingredient);
        redraw("Added " + ingredient + ".");
    }

    private List<String> parseRecipe(String selections) {
        String[] tokens = selections.split(",", -1);
        if (tokens.length != Game.RECIPE_SIZE) {
            throw new IllegalArgumentException("Select 3 ingredients.");
        }
        List<String> recipe = new ArrayList<>();
        for (String token : tokens) {
            recipe.add(resolveIngredient(token.strip()));
        }
        return recipe;
    }

    private String resolveIngredient(String selection) {
        List<String> available = game.getAvailableIngredients();
        if (selection.matches("[+-]?\\d+")) {
            int index;
            try {
                index = Integer.parseInt(selection);
            }
            catch (NumberFormatException error) {
                throw new IllegalArgumentException("Ingredient number is out of range.");
            }
            if (index < 1 || index > available.size()) {
                throw new IllegalArgumentException("Ingredient number is out of range.");
            }
            return available.get(index - 1);
        }
        return available.stream()
            .filter(ingredient -> ingredient.equalsIgnoreCase(selection))
            .findFirst().orElseThrow(() ->
                new IllegalArgumentException("Enter an available ingredient number or name."));
    }

    private void redraw(String feedback) {
        view.refresh();
        Customer customer = game.currentCustomer();
        if (customer instanceof RookVisit) {
            view.showRook((RookVisit) customer, game.getDay(), game.getGold(),
                game.getReputation(), rookStory, feedback);
            return;
        }
        if (customer instanceof AdvancedOrder) {
            view.showAdvancedOrder((AdvancedOrder) customer, game.getDay(), game.getGold(),
                game.getReputation(), game.getAvailableIngredients(), selected, game::stockOf,
                rookStory, haggle, feedback);
            return;
        }
        boolean dealer = customer instanceof RecipeDealer;
        boolean buying = dealer || customer instanceof HerbVendor;
        int saleQuote = !buying && selected.size() == Game.RECIPE_SIZE
            ? game.dealPrice(game.potionPrice(selected), true, haggle) : 0;
        String potion = customer.getDesiredPotion();
        view.showTurn(customer, game.getAvailableIngredients(),
            selected, game.getReputation(), game.waitingCustomerNames(), greeting, feedback,
            game.getDay(), game.getGold(), game.getDailySales(),
            dealer ? game.recipeOffers() : game.vendorOffers(),
            game.canBuyRareHerb(), game::stockOf,
            offer -> game.dealPrice(dealer ? game.recipeOfferPrice(offer) : game.herbPrice(offer),
                false, haggle),
            dealer ? game::recipeOfferNeedsLockedHerbs : offer -> false,
            game.getDiscoveredRecipes().getOrDefault(potion, List.of()),
            game.getRecipeTips().getOrDefault(potion, List.of()), haggle, saleQuote);
    }
}

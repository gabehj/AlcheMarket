import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

public class Validation {
    private final Game game;
    private final ConsoleView view;

    public Validation(Game game, ConsoleView view) {
        this.game = game;
        this.view = view;
    }

    public void run() {
        showTurn();
        while (processLine(view.readLine())) {
            // Commands decide whether play continues.
        }
    }

    public boolean processLine(String line) {
        if (line == null || line.strip().equalsIgnoreCase("quit")) {
            return false;
        }
        String[] command = line.strip().split("\\s+", 2);
        if (command[0].equalsIgnoreCase("recipes") && command.length == 1) {
            game.getDiscoveredRecipes().forEach((potion, recipes) ->
                view.showMessage(potion + ": " + recipes));
            return true;
        }
        if (!command[0].equalsIgnoreCase("brew")) {
            view.showMessage("Invalid command. Enter brew, recipes, or quit.");
            return true;
        }
        try {
            List<String> recipe = parseRecipe(command.length == 2 ? command[1] : "");
            Game.Outcome outcome = game.serve(recipe);
            view.showOutcome(outcome, game.getReputation());
        }
        catch (IllegalArgumentException error) {
            view.showMessage("Invalid selection. " + error.getMessage());
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
            view.showScore(game.getReputation(), game.getDiscoveredPotionCount(), game.getScore());
            return false;
        }
        showTurn();
        return true;
    }

    private List<String> parseRecipe(String selections) {
        String[] tokens = selections.split(",", -1);
        if (tokens.length != game.getNumIngredients()) {
            throw new IllegalArgumentException("Select " + game.getNumIngredients() + " ingredients.");
        }
        List<String> available = game.getAvailableIngredients();
        List<String> recipe = new ArrayList<>();
        for (String token : tokens) {
            String selection = token.strip();
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
                recipe.add(available.get(index - 1));
            }
            else {
                String name = selection.toLowerCase(Locale.ROOT);
                recipe.add(available.stream()
                    .filter(ingredient -> ingredient.toLowerCase(Locale.ROOT).equals(name))
                    .findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Unknown or locked ingredient: " + selection)));
            }
        }
        return recipe;
    }

    private void showTurn() {
        view.showTurn(game.currentCustomer(), game.getAvailableIngredients(), game.getNumIngredients());
    }
}


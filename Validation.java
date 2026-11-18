import java.util.Scanner;

/**
 * // -------------------------------------------------------------------------
/**
 *  Class that handles user input and runs command loop?
 * 
 *  @author Sarah Beatty
 *  @version Nov 11, 2026
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

public class Validation {
    private final Game game;
    private final ConsoleView view;
    private final List<String> selected = new ArrayList<>();
    private String greeting;

    public Validation(Game game, ConsoleView view) {
        this.game = game;
        this.view = view;
    }

    public void run() {
        greeting = view.greetingFor(game.currentCustomer());
        redraw("");
        view.prompt();
        while (processLine(view.readLine())) {
            view.prompt();
        }
    }

    public boolean processLine(String line) {
        if (line == null || line.strip().equalsIgnoreCase("quit")) {
            return false;
        }
        String input = line.strip();
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
                view.showRecipeBook(game.getDiscoveredRecipes());
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
            if (!command[0].equalsIgnoreCase("brew")) {
                selectIngredient(input);
                return true;
            }
            List<String> recipe = command.length == 2 ? parseRecipe(command[1]) : selected;
            result = game.serveWithDetails(recipe);
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
        greeting = view.greetingFor(game.currentCustomer());
        redraw(feedback);
        return true;
    }

    private void selectIngredient(String input) {
        String ingredient = resolveIngredient(input);
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
        view.showTurn(game.currentCustomer(), game.getAvailableIngredients(),
            selected, game.getReputation(), game.waitingCustomerNames(), greeting, feedback);
    }
}

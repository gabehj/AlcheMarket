import java.util.ArrayList;
import java.util.Queue;
import java.util.List;

public class Game {
    
    double recipe_coverage = 0.75; // Fraction of recipes that don't result in duds

    private int num_ingredients = 3;  // Number of ingredients for every recipe
        
    private ArrayList<ArrayList<String>> combos = new ArrayList<>(); // Stores all possible recipes

    private ArrayList<String> ingredientNames; // All ingredients
    private ArrayList<String> potionNames; // All potion names


    public Game() {
        // ADD INGREDIENT NAMES HERE
        ingredientNames = new ArrayList<String>(List.of(
            "Mandrake Root", 
            "Unicorn Hair", 
            "Dragon Scale", 
            "Phoenix Feather"
            )
        );
        // ADD POTION NAMES HERE
        potionNames = new ArrayList<String>(List.of(
            "Healing",
            "Invisibility", 
            "Strength", 
            "Speed", 
            "Luck",
            "Poison: Infinite Scoville",
            "Poison: Gamma Explosion")
        );
        
        generateRecipeCombos(0, new ArrayList<String>()); // Populate combination table with recipes
    }

    // 
    // Space: O((N! / M!) ** 2) , where N is the length of ingredentNames and M is the length of the Recipe
    // Time:  O((N! / M!) * M)  , where N is the length of ingredentNames and M is the length of the Recipe
    public void generateRecipeCombos(int last_index, ArrayList<String> combo) {
        
        // Base Case: Add recipe to combos when it is complete
        if (combo.size() == num_ingredients) combos.add(combo);
        
        // Recursive Step: Re-call function for each possible next ingredient that could be added, order maintained
        else {
            // Start at first possible ingredient addition, end at last ingredient addition that still leaves enough 
            // ingredients to reach the recipe length
            for (int i=last_index; i < ingredientNames.size() - num_ingredients + combo.size() + 1; i++) {
                // Add new ingredient to the current recipe
                ArrayList<String> newCombo = new ArrayList<>(combo);
                newCombo.add(ingredientNames.get(i));
                // Re-call function with updated recipe, only allowing future ingredients 
                // to succeed the last added ingredient
                generateRecipeCombos(i+1, newCombo);
            }
        }
    }

    // GETTERS
    public int getNumIngredients() {
        return num_ingredients;
    }

    public ArrayList<ArrayList<String>> getCombos() {
        return combos;
    }

    public ArrayList<String> getIngredientNames() {
        return ingredientNames;
    }
}
import java.util.ArrayList;
import java.util.Queue;
import java.util.List;
import java.util.Collections; // ArrayList.sort()
public class Game {
    
    double recipe_coverage = 0.75; // Fraction of recipes that don't result in duds

    private int num_ingredients = 3;  // Number of ingredients for every recipe
        
    private ArrayList<String> ingredientNames; // All ingredients
    private ArrayList<String> potionNames; // All potion names
    private ArrayList<String> poisonNames; // All poison names

    private ArrayList<ArrayList<String>> combos = new ArrayList<>(); // Stores all possible recipes

    private ArrayList<Potion> potions = new ArrayList<>(); // Stores all potions


    public Game() {
        System.out.print("Opening AlcheMarket... ");
        // ADD INGREDIENT NAMES HERE
        ingredientNames = new ArrayList<String>(List.of(
            "Mandrake Root", 
            "Unicorn Hair", 
            "Dragon Scale", 
            "Phoenix Feather",
            "Nightshade",
            "Wolfsbane"
        ));
        // ADD POTION NAMES HERE
        
        potionNames = new ArrayList<String>(List.of(
            "Healing",
            "Invisibility", 
            "Adrenaline", 
            "Speed of a Tardy Kobold", 
            "Luck of a Leprechaun",
            "Dangerously High Charisma",
            "City-Melting Dragon Breath",
            "Michelin Star Ambrosia",
            "New Perspectives"
        ));
        // ADD POISON NAMES HERE
        poisonNames = new ArrayList<String>(List.of(
            "Poison: Hellfire Scoville",
            "Poison: Gamma Explosion",
            "Poison: Distilled Dysentery",
            "Poison: Throat-Melter",
            "Poison: Heart-Shrinker",
            "Poison: Neverclear Alcohol"
        ));
        
        // O(NlogN) // Sort ingredients alphabetically to make recipes unordered
        Collections.sort(ingredientNames); 
        // Populate combination table with recipes
        generateRecipeCombos(0, new ArrayList<String>()); 
        System.out.println("Possible Potion Recipes: " + combos.size());

        // Shuffle potion recipes
        int random_index;
        ArrayList<String> temp;
        for (int i = 0; i < combos.size(); i++) {
            random_index = (int) (Math.random() * ingredientNames.size());
            temp = combos.get(i);
            combos.set(i, combos.get(random_index));
            combos.set(random_index, temp);
        }

        // Add potions to potion list
        for (String potionName: potionNames) {
            potions.add(new Potion(potionName));
        }
        for (String poisonName: poisonNames) {
            potions.add(new Potion(poisonName, true));
        }

        // Add recipes to potions
        for (int i = 0; i < (int)(recipe_coverage * combos.size()) ; i++) {
            potions.get(i % potions.size()).addRecipe(combos.get(i));
        }



    }

    // 
    // Space: O((N! / (M! * (N - M)!)) * M)  = O(M(NcM)), where N is the length of ingredentNames and M is the length of each Recipe
    // Time:  O((N! / (M! * (N - M)!)) * M)  = O(M(NcM))  , where N is the length of ingredentNames and M is the length of each Recipe
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

    public double getRecipeCoverage() {
        return recipe_coverage;
    }

    public ArrayList<Potion> getPotions() {
        return potions;
    }

    public ArrayList<ArrayList<String>> getCombos() {
        return combos;
    }

    public ArrayList<String> getIngredientNames() {
        return ingredientNames;
    }
}
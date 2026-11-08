import java.util.ArrayList;
import java.util.Queue;
import java.util.List;

public class Game {
    
    double recipe_coverage = 0.75; // Fraction of recipes that don't result in duds

    private int num_ingredients = 3;  // Number of ingredients for every recipe
        
    private ArrayList<ArrayList<String>> combos = new ArrayList<>(); // Stores all possible recipes

    private ArrayList<String> ingredientNames; // All ingredients
    private ArrayList<String> potionNames; // All potion names
    private ArrayList<String> poisonNames; // All poison names


    public Game() {
        System.out.print("Opening AlcheMarket... ");
        // ADD INGREDIENT NAMES HERE
        ingredientNames = new ArrayList<String>(List.of(
            "Mandrake Root", 
            "Unicorn Hair", 
            "Dragon Scale", 
            "Phoenix Feather",
            "Nightshade",
            "Wolfsbane",
            "Ginseng Root",
            "Mamba Venom",
            "Fairy Dust",
            "Troll Dung",
            "Bear Claw",
            "Eye of Beholder",
            "Basilisk Fang",
            "Four-Leaf Clover",
            "Mummy Guts",
            "Silver Bullet",
            "Pickaxe Powder",
            "Bantha Fodder",
            "Everclear",
            "Unpaid Potion Intern",
            "Half-Eaten Sandwich",
            "Fermented Ox Brain",
            "Can-Do Attitude",
            "Elbow Grease",
            "Unwashed Goblin Loincloth",
            "Ancient Oak Knot",
            "Vial of Souls",
            "Wing of Bat",
            "Fur of Werewolf",
            "Vampire Artery",
            "Demon Horn",
            "Destroyer Angel Shroom",
            "Deathcap Shroom",
            "Human Heart (chopped in two)",
            "Antler of Chimera",
            "Pickled Spider Brain",
            "Ichor of a Fallen God",
            "Dust of the Elder Scroll",
            "Urn of Pyroclastic Ash",
            "Secret written in the blood of a thousand virgins",
            "Unicorn Extract",
            "Fallen soldier torn from the beak of a buzzard",
            "Splinter from the mast of a sunken pirate ship",
            "String of an elven bow",
            "Guarded trinket of an alchemist's homunculus",
            "Box with a child's last words echoing inside",
            "First drop of rain to fall on a barren desert",
            "Wand core of a formidible battle mage",
            "Shard of a narcisist's mirror",
            "Root of a lightning-struck tree"
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

        generateRecipeCombos(0, new ArrayList<String>()); // Populate combination table with recipes
        System.out.println("Done!");
    }

    // 
    // Space: O((N! / (M! * (N - M)!)) ** 2) = O((NcM)**2), where N is the length of ingredentNames and M is the length of each Recipe
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

    public ArrayList<ArrayList<String>> getCombos() {
        return combos;
    }

    public ArrayList<String> getIngredientNames() {
        return ingredientNames;
    }
}
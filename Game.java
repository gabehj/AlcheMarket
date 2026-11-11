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

    private Queue<Customer> customers = new Queue<>(); // Customers in line

    private int reputation = 50; // Score of player, Game Over when zero

    private Validation input = new Validation();

    private int startingIngredients = 4; // Ingredients player first has to make potions
    private ArrayList<String> playerIngredients = new ArrayList<>(); // Players ingredients

    private Player player; // Player object

    // CONSTRUCTORS
    public Game() {
        new Game("Paracelsus");
    }

    public Game(String name) {

        // Instantiate New Player
        player = new Player(name);

        System.out.print("Opening AlcheMarket... ");

        // Setup Variables
        brewDataStructures();

        // Enqueue Customers
        customers.add(new Customer());
        customers.add(new Customer());
        customers.add(new Customer());

        // Give player starting ingredients
        for (int i = 0; i < startingIngredients; i++) {
            playerIngredients.add(ingredientNames.remove((int) (Math.random() * ingredientNames.size())));
        }

        // GAME LOOP
        while (!customers.isEmpty()) {
            // Next customer is dequeued
            Customer current = customers.remove();
            System.out.println("~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~");
            // Customer greets player and asks for a specific potion
            System.out.println(current.getIntroMessage());

            // Player enters different ingredients equal to num_ingredients to make their potion
            ArrayList<String> brew = input.brewPotion(playerIngredients, num_ingredients);
            // Sort ingredients so it matches potion recipes
            Collections.sort(brew);

            // Traverse all recipes in all potions until match is found
            // If ingredients make a potion, store recipe for player
            boolean isDud = true;
            Potion playerPotion;
            for (Potion potion : potions) {
                for (ArrayList<String> recipe : potion.getRecipes()) {
                    if (brew.equals(recipe)) {
                        isDud = false;
                        playerPotion = potion;
                        player.saveRecipe(potion.getName(), recipe);
                        break;
                    }
                }
            }

            // If ingredients do not match a potion recipe, then player fails and moves on to next customer
            if (isDud) {
                // Customer returns a generic message of disappointment in player for not making potion
                System.out.println(current.getDudMessage());
                reputation -= 3;
            }
            // If player made poison, they lose a ton of reputation
            else if (playerPotion.isPoison()) {
                reputation -= 50;
                System.out.println("The customer curls over and dies from your "+playerPotion.getName());
            }
            // If player makes a potion (right or wrong), then the customer tries it and returns appropriate message
            else {
                // Customer checks player potion name against their desired potion
                boolean isCorrect = current.drinkPotion(playerPotion);

                // Player success
                if (isCorrect) {
                    // Customer returns message saying how happy they are
                    current.getCorrectMessage()
                    reputation += 15;
                    // Player randomly gets new ingredient
                    String newIngredient = ingredientNames.remove((int) (Math.random() * ingredientNames.size()));
                    playerIngredients.add(newIngredient);
                    System.out.println("You received the new ingredient: "+newIngredient+"!");
                    System.out.println("You now have "+playerIngredients.size()+" in your collection.");
                }
                else {
                    // Customer returns message saying that they didn't want that particular potion. 
                    // Must reference players potion by name
                    current.getIncorrectMessage(playerPotion);
                    reputation -= 10;
                }
            }

            // Player kills too many people and game ends
            if (reputation <= 0) {
                System.out.println("The villagers break into your laboratory, drag you into the town square, and burn you at the stake");
                System.out.println("~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ GAME OVER ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~");
                break;
            }
        }
        // Message if player satisfies all customers
        if (reputation > 0) {
            System.out.println("~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ QUEST COMPLETE! ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~");
            System.out.println("Congratulations! You have satisfied all of the customers and become a master alchemist!");
        }
        // Score outputted
        System.out.println(gameScore());
    }

    // Run all methods necessary to populate potions and ingredient data structures
    public void brewDataStructures() {
        
        // Store item names in arraylist<String>'s
        listNames();

        // O(NlogN) // Sort ingredients alphabetically to make recipes unordered
        Collections.sort(ingredientNames); 

        // Populate combination table with recipes
        generateRecipeCombos(0, new ArrayList<String>()); 
        System.out.println("Possible Potion Recipes: " + combos.size());
        
        // Scramble combos so potions are unpredictable
        scrambleCombos();

        // Populate potion list with potions and recipe combos
        generatePotions();
    }

    // List creative, human-ideated names for ingredients, potions, and poisons
    public void listNames() {
        // ADD INGREDIENT NAMES HERE
        ingredientNames = new ArrayList<String>(List.of(
            "Mandrake Root", 
            "Unicorn Hair", 
            "Dragon Scale", 
            "Phoenix Feather",
            "Nightshade",
            "Wolfsbane",
            "Bramble Root",
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
    }

    //
    // Space: O(P + N), where P is the # of potions and N is the # of combos
    // Time:  O(P + N), where P is the # of potions and N is the # of combos
    public void generatePotions() {
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
    // Space: O(1)
    // Time:  O(N), where N is the length of combos
    public void scrambleCombos() {
        int random_index;
        ArrayList<String> temp;
        for (int i = 0; i < combos.size(); i++) {
            random_index = (int) (Math.random() * ingredientNames.size());
            temp = combos.get(i);
            combos.set(i, combos.get(random_index));
            combos.set(random_index, temp);
        }
    }

    // 
    // Space: O((N! / (M! * (N - M)!)) * M)  = O(M(C(N,M))), where N is the length of ingredentNames and M is the length of each Recipe
    // Time:  O((N! / (M! * (N - M)!)) * M)  = O(M(C(N,M))), where N is the length of ingredentNames and M is the length of each Recipe
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

    public String gameScore() {
        String message = "";
        message += "~ ~ ~ SCORE ~ ~ ~";
        message += "\n Reputation: "+reputation;
        message += "\n Recipes: "+player.getSavedRecipes().values().size();
        int score = reputation + player.getSavedRecipes().values().size();
        message += "\n +++++++++++++++ ";
        message += "\n Score: Reputation + Recipes = "+score;
        return message + "\n";
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

public class Game {
    public enum Outcome {
        CORRECT, WRONG_POTION, DUD, POISON
    }

    public static final class ServiceResult {
        private final Outcome outcome;
        private final String brewedPotion;

        private ServiceResult(Outcome outcome, String brewedPotion) {
            this.outcome = outcome;
            this.brewedPotion = brewedPotion;
        }

        public Outcome getOutcome() {
            return outcome;
        }

        public String getBrewedPotion() {
            return brewedPotion;
        }
    }

    private static final int RECIPE_SIZE = 3;
    private static final int STARTING_INGREDIENTS = 4;
    private static final double RECIPE_COVERAGE = 0.75;

    private List<String> ingredientNames;
    private List<String> potionNames;
    private List<String> poisonNames;
    private final List<List<String>> combos = new ArrayList<>();
    private final Map<List<String>, Potion> recipes = new HashMap<>();
    private final LinkedQueue<Customer> customers = new LinkedQueue<>();
    private final List<String> availableIngredients = new ArrayList<>();
    private final List<String> lockedIngredients = new ArrayList<>();
    private final Player player;
    private final List<String> starterRecipe;

    public Game() {
        this("Paracelsus");
    }

    public Game(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Enter a player name.");
        }
        player = new Player(name);
        player.setReputation(50);
        Random random = new Random();
        listNames();
        Collections.sort(ingredientNames);
        generateRecipeCombos(0, new ArrayList<>());
        Collections.shuffle(combos, random);
        generatePotions();

        // The first assigned recipe is a normal potion, and its materials start unlocked.
        starterRecipe = List.copyOf(combos.get(0));
        availableIngredients.addAll(starterRecipe);
        lockedIngredients.addAll(ingredientNames);
        lockedIngredients.removeAll(availableIngredients);
        Collections.shuffle(lockedIngredients, random);
        while (availableIngredients.size() < STARTING_INGREDIENTS) {
            unlockIngredient();
        }

        Potion starter = recipes.get(starterRecipe);
        player.saveRecipe(starter.getName(), new ArrayList<>(starterRecipe));
        customers.enqueue(new NormalCustomer("Villager", CustomerType.TOWNSPERSON,
            starter.getName(), starterRecipe));
        Random typeRandom = new Random();
        CustomerType[] types = CustomerType.values();
        customers.enqueue(createCustomer("Traveler", types[typeRandom.nextInt(types.length)], random));
        customers.enqueue(createCustomer("Merchant", types[typeRandom.nextInt(types.length)], random));
        customers.enqueue(createCustomer("Priest", types[typeRandom.nextInt(types.length)], random));
        customers.enqueue(createCustomer("Magistrate", types[typeRandom.nextInt(types.length)], random));
        customers.enqueue(createCustomer("Soldier", types[typeRandom.nextInt(types.length)], random));
        customers.enqueue(createCustomer("Warlock", types[typeRandom.nextInt(types.length)], random));
    }

    private Customer createCustomer(String name, CustomerType type, Random random) {
        List<List<String>> accessibleRecipes = new ArrayList<>();
        for (int first = 0; first < availableIngredients.size() - 2; first++) {
            for (int second = first + 1; second < availableIngredients.size() - 1; second++) {
                for (int third = second + 1; third < availableIngredients.size(); third++) {
                    List<String> recipe = new ArrayList<>(List.of(
                        availableIngredients.get(first), availableIngredients.get(second),
                        availableIngredients.get(third)));
                    Collections.sort(recipe);
                    Potion potion = recipes.get(recipe);
                    if (potion != null && !potion.isPoison()) {
                        accessibleRecipes.add(recipe);
                    }
                }
            }
        }
        List<String> recipe = accessibleRecipes.get(random.nextInt(accessibleRecipes.size()));
        return new NormalCustomer(name, type, recipes.get(recipe).getName(),
            recipe.subList(0, RECIPE_SIZE - 1));
    }

    public Outcome serve(List<String> ingredients) {
        return serveWithDetails(ingredients).getOutcome();
    }

    public ServiceResult serveWithDetails(List<String> ingredients) {
        if (customers.isEmpty()) {
            throw new NoSuchElementException("No customers are waiting.");
        }
        if (isOver()) {
            throw new IllegalStateException("The game has ended.");
        }
        List<String> recipe = validateRecipe(ingredients);
        Potion potion = recipes.get(recipe);
        Customer customer = customers.dequeue();
        if (potion == null) {
            player.decreaseReputation(3);
            return new ServiceResult(Outcome.DUD, null);
        }
        player.saveRecipe(potion.getName(), new ArrayList<>(recipe));
        if (potion.isPoison()) {
            player.decreaseReputation(50);
            return new ServiceResult(Outcome.POISON, potion.getName());
        }
        if (!customer.getDesiredPotion().equals(potion.getName())) {
            player.decreaseReputation(10);
            return new ServiceResult(Outcome.WRONG_POTION, potion.getName());
        }
        player.increaseReputation(15);
        unlockIngredient();
        return new ServiceResult(Outcome.CORRECT, potion.getName());
    }

    private List<String> validateRecipe(List<String> ingredients) {
        if (ingredients == null || ingredients.size() != RECIPE_SIZE) {
            throw new IllegalArgumentException("Select " + RECIPE_SIZE + " ingredients.");
        }
        List<String> recipe = new ArrayList<>();
        for (String ingredient : ingredients) {
            if (!availableIngredients.contains(ingredient)) {
                throw new IllegalArgumentException("Unknown or locked ingredient: " + ingredient);
            }
            if (recipe.contains(ingredient)) {
                throw new IllegalArgumentException("Select different ingredients.");
            }
            recipe.add(ingredient);
        }
        Collections.sort(recipe);
        return recipe;
    }

    private void unlockIngredient() {
        if (!lockedIngredients.isEmpty()) {
            availableIngredients.add(lockedIngredients.remove(lockedIngredients.size() - 1));
        }
    }

    private void generatePotions() {
        List<Potion> potions = new ArrayList<>();
        for (String name : potionNames) {
            potions.add(new Potion(name));
        }
        for (String name : poisonNames) {
            potions.add(new Potion(name, true));
        }
        int assigned = (int) (RECIPE_COVERAGE * combos.size());
        for (int index = 0; index < assigned; index++) {
            recipes.put(combos.get(index), potions.get(index % potions.size()));
        }
    }

    // List creative, human-ideated names for ingredients, potions, and poisons
    private void listNames() {
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
    // Space: O((N! / (M! * (N - M)!)) * M)  = O(M(C(N,M))), where N is the length of ingredentNames and M is the length of each Recipe
    // Time:  O((N! / (M! * (N - M)!)) * M)  = O(M(C(N,M))), where N is the length of ingredentNames and M is the length of each Recipe
    private void generateRecipeCombos(int start, ArrayList<String> combo) {
        
        // Base Case: Add recipe to combos when it is complete
        if (combo.size() == RECIPE_SIZE) combos.add(combo);
        
        // Recursive Step: Re-call function for each possible next ingredient that could be added, order maintained
        else {
            // Start at first possible ingredient addition, end at last ingredient addition that still leaves enough 
            // ingredients to reach the recipe length
            for (int i=start; i < ingredientNames.size() - RECIPE_SIZE + combo.size() + 1; i++) {
                // Add new ingredient to the current recipe
                ArrayList<String> newCombo = new ArrayList<>(combo);
                newCombo.add(ingredientNames.get(i));
                // Re-call function with updated recipe, only allowing future ingredients 
                // to succeed the last added ingredient
                generateRecipeCombos(i+1, newCombo);
            }
        }
    }


    public Customer currentCustomer() {
        return customers.peek();
    }

    public boolean isOver() {
        return customers.isEmpty() || getReputation() <= 0;
    }

    public int getReputation() {
        return player.getReputation();
    }

    public int getScore() {
        return getReputation() + getDiscoveredPotionCount();
    }

    public int getDiscoveredPotionCount() {
        return player.getSavedRecipes().size();
    }

    public Map<String, List<List<String>>> getDiscoveredRecipes() {
        Map<String, List<List<String>>> discoveries = new java.util.TreeMap<>();
        player.getSavedRecipes().forEach((potion, recipesForPotion) -> {
            List<List<String>> copies = new ArrayList<>();
            for (List<String> recipe : recipesForPotion) {
                copies.add(List.copyOf(recipe));
            }
            discoveries.put(potion, List.copyOf(copies));
        });
        return Collections.unmodifiableMap(discoveries);
    }

    public List<String> getIngredientNames() {
        return List.copyOf(ingredientNames);
    }

    public List<String> getAvailableIngredients() {
        return List.copyOf(availableIngredients);
    }

    public List<String> starterRecipe() {
        return starterRecipe;
    }

    public int getNumIngredients() {
        return RECIPE_SIZE;
    }
}
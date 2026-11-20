import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Game {
    
    public enum Outcome {
        CORRECT, WRONG_POTION, DUD, POISON
    }

    public static final class ServiceResult {
        private final Outcome outcome;
        private final String brewedPotion;
        private final int goldEarned;

        private ServiceResult(Outcome outcome, String brewedPotion) {
            this(outcome, brewedPotion, 0);
        }

        private ServiceResult(Outcome outcome, String brewedPotion, int goldEarned) {
            this.outcome = outcome;
            this.brewedPotion = brewedPotion;
            this.goldEarned = goldEarned;
        }

        public Outcome getOutcome() {
            return outcome;
        }

        public String getBrewedPotion() {
            return brewedPotion;
        }

        public int getGoldEarned() {
            return goldEarned;
        }
    }

    public static final int RECIPE_SIZE = 3;
    private static final int STARTING_INGREDIENTS = 4;
    private static final double RECIPE_COVERAGE = 0.75;
    private static final List<String> NEW_HERBS = List.of("Silverleaf", "Emberroot", "Frostmint");
    private static final List<String> NEW_POTIONS = List.of("Fever Tonic", "Emberguard Draught",
        "Frostveil Elixir");
    public static final String RARE_HERB = "Moonpetal";
    public static final int RARE_REPUTATION = 65;
    public static final int RARE_DAILY_SALES = 2;
    public static final int TRUSTED_REPUTATION = 80;

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
    private final Random arrivals;
    private final Random rookCards;
    private RookVisit.Result rookLastResult = RookVisit.Result.NONE;
    private final Map<String, Integer> stock = new HashMap<>();
    private final Map<String, List<String>> recipeTips = new java.util.TreeMap<>();
    private final List<String> specialRecipe;
    private final List<List<String>> newFormulas = new ArrayList<>();
    private boolean advancedOrdersUnlocked;
    private int gold = 30;
    private int day = 1;
    private int dailySales;
    private int openingGold = gold;
    private int previousVisitorCount;
    private boolean specialQueued;

    public Game() {
        this("Paracelsus");
    }

    public Game(String name) {
        this(name, new Random().nextLong());
    }

    public Game(String name, long seed) {
        Random random = new Random(seed);
        arrivals = new Random(~seed);
        rookCards = new Random(seed ^ 0x5DEECE66DL);

        // Initialize Player
        player = new Player(name);
        player.setReputation(50);
        // List of ingredients
        listNames();
        Collections.sort(ingredientNames);
        // All ingredient combos
        generateRecipeCombos(0, new ArrayList<>());
        Collections.shuffle(combos, random);
        int starterIndex = 0;
        while (combos.get(starterIndex).contains(RARE_HERB)) {
            starterIndex++;
        }
        Collections.swap(combos, 0, starterIndex);
        // Map compos to potions
        generatePotions();

        // The first assigned recipe is a normal potion, and its materials start unlocked.
        starterRecipe = List.copyOf(combos.get(0));
        // Add the initial ingredients to player
        availableIngredients.addAll(starterRecipe);
        // Store other, locked ingredients in arraylist
        lockedIngredients.addAll(ingredientNames);
        lockedIngredients.removeAll(availableIngredients);
        lockedIngredients.remove(RARE_HERB);
        // Shuffle ingredients, always have minimum ingreds
        Collections.shuffle(lockedIngredients, random);
        while (availableIngredients.size() < STARTING_INGREDIENTS) {
            unlockIngredient();
        }

        // First potion recipe
        Potion starter = recipes.get(starterRecipe);
        player.saveRecipe(starter.getName(), new ArrayList<>(starterRecipe));
        availableIngredients.forEach(herb -> stock.put(herb, 3));
        List<String> rareRecipe = new ArrayList<>(starterRecipe.subList(0, 2));
        rareRecipe.add(RARE_HERB);
        Collections.sort(rareRecipe);
        specialRecipe = List.copyOf(rareRecipe);
        recipes.put(specialRecipe, new Potion("Lunar Remedy"));
        for (int index = 0; index < NEW_HERBS.size(); index++) {
            String herb = NEW_HERBS.get(index);
            ingredientNames.add(herb);
            List<String> formula = new ArrayList<>(List.of(starterRecipe.get(index),
                starterRecipe.get((index + 1) % RECIPE_SIZE)));
            formula.add(herb);
            Collections.sort(formula);
            List<String> key = List.copyOf(formula);
            newFormulas.add(key);
            combos.add(key);
            recipes.put(key, new Potion(NEW_POTIONS.get(index)));
        }
        fillDay();
    }

    // private void fillDay() {
    //     boolean rookArrives = (day >= 2 && day <= 4)
    //         || (day == 5 && rookLastResult == RookVisit.Result.LOANED);
    //     boolean valeArrives = day == 3;
    //     int specialVisitors = (rookArrives ? 1 : 0) + (valeArrives ? 1 : 0);
    //     int total = 8 + arrivals.nextInt(5);
    //     if (total + specialVisitors == previousVisitorCount) {
    //         total = total == 12 ? 8 : total + 1;
    //     }
    //     previousVisitorCount = total + specialVisitors;
    //     int vendors = total / 2 + arrivals.nextInt(3) - 1;
    //     List<Customer> visitors = new ArrayList<>();
    //     CustomerType[] types = CustomerType.values();
    //     for (int index = 1; index < total - vendors; index++) {
    //         visitors.add(createCustomer("Buyer " + (index + 1),
    //             types[arrivals.nextInt(types.length)], arrivals));
    //     }
    //     List<HerbVendor> suppliers = createSuppliers(vendors);
    //     for (int index = 1; index < vendors - 1; index++) {
    //         visitors.add(index == 1 ? new RecipeDealer() : suppliers.get(index));
    //     }
    //     Collections.shuffle(visitors, arrivals);
    //     String potion = recipes.get(starterRecipe).getName();
    //     Customer firstBuyer = new NormalCustomer("Villager", CustomerType.TOWNSPERSON,
    //         potion, starterRecipe);
    //     Customer firstVendor = suppliers.get(0);
    //     if (day == 1 || arrivals.nextBoolean()) {
    //         customers.enqueue(firstBuyer);
    //         customers.enqueue(firstVendor);
    //     }
    //     else {
    //         customers.enqueue(firstVendor);
    //         customers.enqueue(firstBuyer);
    //     }
    //     visitors.forEach(customers::enqueue);
    //     customers.enqueue(suppliers.get(vendors - 1));
    //     if (valeArrives) {
    //         customers.enqueue(createAdvancedOrder("Magister Vale", CustomerType.TOWNSPERSON, true, false));
    //     }
    //     if (rookArrives) {
    //         customers.enqueue(new RookVisit(day - 1, rookLastResult));
    //     }
    //}

    private void fillDay() {
        boolean rookArrives = (day >= 2 && day <= 4)
            || (day == 5 && rookLastResult == RookVisit.Result.LOANED);
        boolean valeArrives = day == 3;
        int specialVisitors = (rookArrives ? 1 : 0) + (valeArrives ? 1 : 0);
        int total = 8 + arrivals.nextInt(5);

        if (total + specialVisitors == previousVisitorCount) {
            total = total == 12 ? 8 : total + 1;
        }

        previousVisitorCount = total + specialVisitors;
        int vendors = total / 2 + arrivals.nextInt(3) - 1;

        List<Customer> visitors = new ArrayList<>();
        CustomerType[] types = CustomerType.values();

        // Generate regular customers first
        for (int index = 1; index < total - vendors; index++) {
            visitors.add(createCustomer("Buyer " + (index + 1),
                types[arrivals.nextInt(types.length)], arrivals));
        }

        // Create the guaranteed first buyer
        String potion = recipes.get(starterRecipe).getName();
        Customer firstBuyer = new NormalCustomer("Villager",
            CustomerType.TOWNSPERSON, potion, starterRecipe);

        // Create the advanced customer if one is scheduled
        Customer valeCustomer = valeArrives
            ? createAdvancedOrder("Magister Vale",
                CustomerType.TOWNSPERSON, true, false)
            : null;

        // Collect all potion orders for today's seller inventory
        List<Customer> dailyOrders = new ArrayList<>(visitors);
        dailyOrders.add(firstBuyer);

        if (valeCustomer != null) {
            dailyOrders.add(valeCustomer);
        }

        // Generate sellers using the day's customer orders
        List<HerbVendor> suppliers = createSuppliers(vendors, dailyOrders);

        for (int index = 1; index < vendors - 1; index++) {
            visitors.add(index == 1
                ? new RecipeDealer()
                : suppliers.get(index));
        }

        Collections.shuffle(visitors, arrivals);

        Customer firstVendor = suppliers.get(0);

        if (day == 1 || arrivals.nextBoolean()) {
            customers.enqueue(firstBuyer);
            customers.enqueue(firstVendor);
        }
        else {
            customers.enqueue(firstVendor);
            customers.enqueue(firstBuyer);
        }

        visitors.forEach(customers::enqueue);
        customers.enqueue(suppliers.get(vendors - 1));

        if (valeCustomer != null) {
            customers.enqueue(valeCustomer);
        }

        if (rookArrives) {
            customers.enqueue(new RookVisit(day - 1, rookLastResult));
        }
    }



    // private List<HerbVendor> createSuppliers(int count) {
    //     List<String> herbs = new ArrayList<>(availableIngredients);
    //     herbs.remove(RARE_HERB);
    //     List<List<String>> pairs = new ArrayList<>();
    //     for (int first = 0; first < herbs.size(); first++) {
    //         for (int second = first + 1; second < herbs.size(); second++) {
    //             pairs.add(List.of(herbs.get(first), herbs.get(second)));
    //         }
    //     }
    //     Collections.shuffle(pairs, arrivals);
    //     pairs.sort(java.util.Comparator.comparingInt(pair -> stockOf(pair.get(0)) + stockOf(pair.get(1))));

    //     List<HerbVendor> suppliers = new ArrayList<>();
    //     List<String> earlyOffers = new ArrayList<>(starterRecipe);
    //     if (day >= 2) {
    //         earlyOffers.addAll(NEW_HERBS);
    //     }
    //     earlyOffers.add(RARE_HERB);
    //     suppliers.add(new HerbVendor("Herbalist 1", earlyOffers));
    //     for (int index = 1; index < count; index++) {
    //         List<String> offers = new ArrayList<>(pairs.get(index - 1));
    //         if (index == count - 1) {
    //             offers.add(RARE_HERB);
    //         }
    //         suppliers.add(new HerbVendor("Herbalist " + (index + 1), offers));
    //     }
    //     return suppliers;
    // }


    private List<HerbVendor> createSuppliers(
        int count, List<Customer> dailyOrders) {

        List<String> herbs = new ArrayList<>(availableIngredients);
        herbs.remove(RARE_HERB);

        // Calculate ingredient demand for today's customers
        Map<String, Integer> demand = ingredientDemand(dailyOrders);

        List<List<String>> pairs = new ArrayList<>();

        for (int first = 0; first < herbs.size(); first++) {
            for (int second = first + 1; second < herbs.size(); second++) {
                pairs.add(List.of(herbs.get(first), herbs.get(second)));
            }
        }

        // Randomize pairs first to preserve randomness for ties
        Collections.shuffle(pairs, arrivals);

        // Prioritize pairs containing ingredients needed by customers
        pairs.sort((first, second) -> {
            int firstDemand = demand.getOrDefault(first.get(0), 0)
                + demand.getOrDefault(first.get(1), 0);

            int secondDemand = demand.getOrDefault(second.get(0), 0)
                + demand.getOrDefault(second.get(1), 0);

            // Higher customer demand comes first
            int demandComparison = Integer.compare(
                secondDemand, firstDemand);

            if (demandComparison != 0) {
                return demandComparison;
            }

            // If demand is equal, prioritize lower stock
            int firstStock = stockOf(first.get(0))
                + stockOf(first.get(1));

            int secondStock = stockOf(second.get(0))
                + stockOf(second.get(1));

            return Integer.compare(firstStock, secondStock);
        });

        List<HerbVendor> suppliers = new ArrayList<>();

        // Keep the first herbalist's existing inventory
        List<String> earlyOffers = new ArrayList<>(starterRecipe);

        if (day >= 2) {
            earlyOffers.addAll(NEW_HERBS);
        }

        earlyOffers.add(RARE_HERB);

        suppliers.add(new HerbVendor("Herbalist 1", earlyOffers));

        // Remaining herbalists receive prioritized ingredient pairs
        for (int index = 1; index < count; index++) {
            List<String> offers = new ArrayList<>(pairs.get(index - 1));

            if (index == count - 1) {
                offers.add(RARE_HERB);
            }

            suppliers.add(new HerbVendor("Herbalist " + (index + 1),
                offers));
        }

        return suppliers;
    }


    private Map<String, Integer> ingredientDemand(
        List<Customer> dailyOrders) {

        Map<String, Integer> demand = new HashMap<>();

        for (Customer customer : dailyOrders) {

            // Advanced orders require multiple component potions
            if (customer instanceof AdvancedOrder) {
                AdvancedOrder order = (AdvancedOrder) customer;

                for (String component : order.getComponents()) {
                    addRecipeDemand(component, List.of(), demand);
                }
            }
            else {
                // Regular and special potion customers
                addRecipeDemand(customer.getDesiredPotion(),
                    customer.getClue(), demand);
            }
        }

        return demand;
    }


    private void addRecipeDemand(String potionName,
        List<String> clue, Map<String, Integer> demand) {

        List<String> recipe = null;

        // First, look for a recipe matching the potion AND its clues
        for (Map.Entry<List<String>, Potion> entry : recipes.entrySet()) {

            if (entry.getValue().getName().equals(potionName)
                && entry.getKey().containsAll(clue)) {

                recipe = entry.getKey();
                break;
            }
        }

        // If no recipe was found, there is nothing to prioritize
        if (recipe == null) {
            return;
        }

        // Add each required ingredient to the demand count
        for (String ingredient : recipe) {
            demand.merge(ingredient, 1, Integer::sum);
        }
    }




    private Customer createCustomer(String name, CustomerType type, Random random) {
        if (advancedOrdersUnlocked && random.nextInt(10) == 0) {
            return createAdvancedOrder(name, type, false, random.nextBoolean());
        }
        List<List<String>> options = accessibleRecipes();
        List<String> recipe = options.get(random.nextInt(options.size()));
        return new NormalCustomer(name, type, recipes.get(recipe).getName(),
            recipe.subList(0, RECIPE_SIZE - 1));
    }

    private AdvancedOrder createAdvancedOrder(String name, CustomerType type, boolean royal,
        boolean winter) {
        return winter
            ? new AdvancedOrder(name, type, royal, "Winterward Elixir",
                NEW_POTIONS.subList(1, 3), newFormulas.subList(1, 3))
            : new AdvancedOrder(name, type, royal, "Royal Restorative",
                List.of(recipes.get(starterRecipe).getName(), NEW_POTIONS.get(0)),
                List.of(starterRecipe, newFormulas.get(0)));
    }

    public String brewAdvancedComponent(List<String> ingredients) {
        AdvancedOrder order = currentAdvancedOrder();
        int slot = order.getPreparedCount();
        if (slot == order.getComponents().size()) {
            throw new IllegalArgumentException("Both bottles are ready. Use combine.");
        }
        List<String> recipe = validateRecipe(ingredients);
        String expected = order.getComponents().get(slot);
        Potion potion = recipes.get(recipe);
        if (potion == null || !potion.getName().equals(expected)) {
            throw new IllegalArgumentException("This slot needs " + expected + ". Check the formula.");
        }
        recipe.forEach(herb -> stock.put(herb, stockOf(herb) - 1));
        order.prepare(ingredientCost(recipe));
        player.saveRecipe(expected, new ArrayList<>(recipe));
        return expected;
    }

    public ServiceResult combineAdvancedOrder(int haggle) {
        AdvancedOrder order = currentAdvancedOrder();
        validateHaggle(haggle);
        if (order.getPreparedCount() != order.getComponents().size()) {
            throw new IllegalArgumentException("Brew both component potions first.");
        }
        int earned = dealPrice(withMarkup(order.ingredientCost()), true, haggle);
        gold += earned;
        dailySales++;
        player.increaseReputation(15);
        applyHaggle(haggle);
        unlockIngredient();
        customers.dequeue();
        if (order.isRoyalVisit()) {
            advancedOrdersUnlocked = true;
        }
        return new ServiceResult(Outcome.CORRECT, order.getDesiredPotion(), earned);
    }

    private AdvancedOrder currentAdvancedOrder() {
        requireOpen();
        Customer customer = currentCustomer();
        if (!(customer instanceof AdvancedOrder)) {
            throw new IllegalStateException("No advanced order at the counter.");
        }
        return (AdvancedOrder) customer;
    }

    private Map<String, List<String>> findRecipeOffers() {
        Map<String, List<String>> offers = new java.util.TreeMap<>();
        for (List<List<String>> catalog : List.of(accessibleRecipes(), combos)) {
            for (List<String> recipe : catalog) {
                Potion potion = recipes.get(recipe);
                if (potion != null && !potion.isPoison()
                    && !knowsRecipe(potion.getName(), recipe)) {
                    offers.putIfAbsent(potion.getName(), List.copyOf(recipe));
                    if (offers.size() == 3) {
                        return offers;
                    }
                }
            }
        }
        return offers;
    }

    private boolean knowsRecipe(String potion, List<String> recipe) {
        List<ArrayList<String>> known = player.getSavedRecipes().get(potion);
        return known != null && known.contains(recipe);
    }

    private List<List<String>> accessibleRecipes() {
        List<List<String>> accessibleRecipes = new ArrayList<>();
        for (int first = 0; first < availableIngredients.size() - 2; first++) {
            for (int second = first + 1; second < availableIngredients.size() - 1; second++) {
                for (int third = second + 1; third < availableIngredients.size(); third++) {
                    List<String> recipe = new ArrayList<>(List.of(
                        availableIngredients.get(first), availableIngredients.get(second),
                        availableIngredients.get(third)));
                    Collections.sort(recipe);
                    Potion potion = recipes.get(recipe);
                    if (potion != null && !potion.isPoison() && !recipe.contains(RARE_HERB)) {
                        accessibleRecipes.add(recipe);
                    }
                }
            }
        }
        return accessibleRecipes;
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
            "Secret written in blood",
            "Unicorn Extract",
            "Half-digested, fallen adventurer",
            "Splinter from a sunken pirate ship",
            "String of an elven bow",
            "Wyrm's guarded trinket",
            "Box with a man's last words echoing inside",
            "First fallen raindrop on a barren desert",
            "Battlemage's Wand",
            "Shard of a narcisist's mirror",
            "Root of a lightning-struck tree",
            "Moonpetal"
        ));
        // ADD POTION NAMES HERE
        potionNames = new ArrayList<String>(List.of(
            "Healing",
            "Ghost-Stalking", 
            "Boar Adrenaline", 
            "Speed of a Tardy Kobold", 
            "Luck of a Leprechaun",
            "Dangerously High Charisma",
            "City-Melting Dragon Breath",
            "Michelin Star Ambrosia",
            "New Perspectives",
            "Hearty Ale",
            "Liquid Genius",
            "Mildly More Confidence"
        ));
        // ADD POISON NAMES HERE
        poisonNames = new ArrayList<String>(List.of(
            "Poison: Hellfire Scoville",
            "Poison: Ailing Ale",
            "Poison: Gamma Explosion",
            "Poison: Distilled Dysentery",
            "Poison: Throat-Melter",
            "Poison: Heart-Shrinker",
            "Poison: Neverclear Alcohol"
        ));
    }


    // RECIPE-MAKING ALGORITHM
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

    // 
    public Outcome serve(List<String> ingredients) {
        return serveWithDetails(ingredients).getOutcome();
    }

    public ServiceResult serveWithDetails(List<String> ingredients) {
        return serveWithDetails(ingredients, 0);
    }

    public ServiceResult serveWithDetails(List<String> ingredients, int haggle) {
        requireOpen();
        validateHaggle(haggle);
        Customer customer = currentCustomer();
        if (customer instanceof AdvancedOrder) {
            throw new IllegalStateException("Brew both component potions before combining.");
        }
        if (customer instanceof HerbVendor || customer instanceof RecipeDealer
            || customer instanceof RookVisit) {
            throw new IllegalStateException("This visitor does not buy potions.");
        }
        List<String> recipe = validateRecipe(ingredients);
        Potion potion = recipes.get(recipe);
        recipe.forEach(herb -> stock.put(herb, stockOf(herb) - 1));
        customers.dequeue();
        rememberTip(customer);
        if (potion == null) {
            player.decreaseReputation(3);
            return new ServiceResult(Outcome.DUD, null);
        }
        if (potion.isPoison()) {
            player.decreaseReputation(50);
            return new ServiceResult(Outcome.POISON, potion.getName());
        }
        player.saveRecipe(potion.getName(), new ArrayList<>(recipe));
        if (!customer.getDesiredPotion().equals(potion.getName())) {
            player.decreaseReputation(10);
            return new ServiceResult(Outcome.WRONG_POTION, potion.getName());
        }
        int earned = dealPrice(potionPrice(recipe), true, haggle);
        player.increaseReputation(15);
        applyHaggle(haggle);
        dailySales++;
        gold += earned;
        unlockIngredient();
        return new ServiceResult(Outcome.CORRECT, potion.getName(), earned);
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
            if (stockOf(ingredient) == 0) {
                throw new IllegalArgumentException("Out of stock: " + ingredient);
            }
            recipe.add(ingredient);
        }
        Collections.sort(recipe);
        return recipe;
    }


    // GETTERS

    public Customer currentCustomer() {
        return customers.peek();
    }

    public List<String> waitingCustomerNames() {
        return customers.snapshot().stream().skip(1)
            .map(customer -> (customer instanceof SpecialCustomer || customer instanceof RookVisit
                || (customer instanceof AdvancedOrder && ((AdvancedOrder) customer).isRoyalVisit())
                ? "SPECIAL " : "")
                + customer.getName())
            .collect(java.util.stream.Collectors.toUnmodifiableList());
    }

    public boolean isOver() {
        return getReputation() <= 0;
    }

    public boolean isDayClosed() {
        return customers.isEmpty();
    }

    public void startNextDay() {
        if (isOver() || !isDayClosed()) {
            throw new IllegalStateException("Finish the current day first.");
        }
        day++;
        if (day == 2) {
            availableIngredients.addAll(NEW_HERBS);
        }
        dailySales = 0;
        openingGold = gold;
        specialQueued = false;
        fillDay();
    }

    public void declineCustomer() {
        requireOpen();
        if (currentCustomer() instanceof RookVisit) {
            RookVisit rook = currentRook();
            if (rook.hasDrawn() || rook.getNumber() == 4) {
                throw new IllegalStateException("Finish the encounter before moving on.");
            }
            rookLastResult = RookVisit.Result.DECLINED;
        }

        player.decreaseReputation(10);
        Customer visitor = customers.dequeue();
        if (visitor instanceof AdvancedOrder && ((AdvancedOrder) visitor).isRoyalVisit()) {
            advancedOrdersUnlocked = true;
        }
        rememberTip(visitor);
    }

    public void drawWithRook() {
        RookVisit rook = currentRook();
        if (rook.hasDrawn() || rook.getNumber() == 4) {
            throw new IllegalStateException("No new draw is available.");
        }
        if (gold < rook.getStake()) {
            throw new IllegalArgumentException("Not enough gold for the stake.");
        }
        int first = rookCards.nextInt(52);
        int second = rookCards.nextInt(51);
        if (second >= first) {
            second++;
        }
        rook.revealCards(first % 13 + 2, second % 13 + 2);
        if (rook.getPlayerCard() < rook.getRookCard()) {
            gold -= rook.getStake();
        }
    }

    public void finishRookRound(boolean takeHerb) {
        RookVisit rook = currentRook();
        if (!rook.hasDrawn()) {
            throw new IllegalStateException("Draw the cards first.");
        }
        int result = Integer.compare(rook.getPlayerCard(), rook.getRookCard());
        if (result > 0) {
            if (takeHerb) {
                awardMoonpetal(1);
            }
            else {
                gold += rook.getStake();
            }
            rookLastResult = RookVisit.Result.WON;
        }
        else {
            if (takeHerb) {
                throw new IllegalArgumentException("There is no prize to claim.");
            }
            rookLastResult = result == 0 ? RookVisit.Result.TIED : RookVisit.Result.LOST;
        }
        customers.dequeue();
    }

    public void lendToRook() {
        RookVisit rook = currentRook();
        if (rook.getNumber() != 3 || rook.hasDrawn()) {
            throw new IllegalStateException("No loan is available.");
        }
        if (gold < rook.getStake()) {
            throw new IllegalArgumentException("Not enough gold for the loan.");
        }
        gold -= rook.getStake();
        rookLastResult = RookVisit.Result.LOANED;
        customers.dequeue();
    }

    public void collectRookRepayment() {
        if (currentRook().getNumber() != 4) {
            throw new IllegalStateException("No repayment is due.");
        }
        awardMoonpetal(2);
        customers.dequeue();
    }

    private RookVisit currentRook() {
        requireOpen();
        Customer customer = currentCustomer();
        if (!(customer instanceof RookVisit)) {
            throw new IllegalStateException("Rook is not at the counter.");
        }
        return (RookVisit) customer;
    }

    private void awardMoonpetal(int count) {
        stock.put(RARE_HERB, stockOf(RARE_HERB) + count);
        if (!availableIngredients.contains(RARE_HERB)) {
            availableIngredients.add(RARE_HERB);
        }
    }

    private void rememberTip(Customer customer) {
        if (customer.getClue().size() == RECIPE_SIZE) {
            player.saveRecipe(customer.getDesiredPotion(), new ArrayList<>(customer.getClue()));
        }
        else if (!customer.getClue().isEmpty()) {
            recipeTips.put(customer.getDesiredPotion(), customer.getClue());
        }
    }

    public Map<String, List<String>> getRecipeTips() {
        return Map.copyOf(recipeTips);
    }

    public int herbPrice(String herb) {
        if (!ingredientNames.contains(herb)) {
            throw new IllegalArgumentException("Unknown ingredient.");
        }
        int dailyShift = Math.floorMod(31 * herb.hashCode() + day, 3);
        return RARE_HERB.equals(herb) ? 11 + dailyShift
            : 1 + Math.floorMod(herb.hashCode(), 3) + dailyShift;
    }

    public int potionPrice(List<String> recipe) {
        if (recipe == null || recipe.size() != RECIPE_SIZE
            || recipe.stream().distinct().count() != RECIPE_SIZE) {
            throw new IllegalArgumentException("Quote three different ingredients.");
        }
        return withMarkup(ingredientCost(recipe));
    }

    private int ingredientCost(List<String> recipe) {
        return recipe.stream().mapToInt(this::herbPrice).sum();
    }

    private static int withMarkup(int cost) {
        return (cost * 115 + 99) / 100;
    }

    public int dealPrice(int base, boolean selling, int haggle) {
        if (base <= 0) {
            throw new IllegalArgumentException("Price must be positive.");
        }
        validateHaggle(haggle);
        int direction = selling ? 1 : -1;
        int loyalty = getReputation() >= TRUSTED_REPUTATION ? 5 : 0;
        // Round once, after applying the standing rate and the negotiated rate.
        long scaled = (long) base * (100 + direction * loyalty) * (100 + direction * haggle);
        return Math.max(1, (int) ((scaled + 5_000) / 10_000));
    }

    public static void validateHaggle(int haggle) {
        switch (Math.abs(haggle)) {
            case 0:
            case 5:
            case 15:
            case 20:
            case 50:
                return;
            default:
                throw new IllegalArgumentException("Use haggle 0 or +/-5, 15, 20, or 50.");
        }
    }

    private void applyHaggle(int haggle) {
        player.decreaseReputation(haggle / 5);
    }

    public List<String> recipeOffers() {
        Customer customer = currentCustomer();
        if (customer instanceof RecipeDealer) {
            RecipeDealer dealer = (RecipeDealer) customer;
            if (dealer.getOffers().entrySet().stream()
                .allMatch(offer -> knowsRecipe(offer.getKey(), offer.getValue()))) {
                dealer.setOffers(findRecipeOffers());
            }
            return dealer.getOffers().entrySet().stream()
                .filter(offer -> !knowsRecipe(offer.getKey(), offer.getValue()))
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toUnmodifiableList());
        }
        return List.of();
    }

    public int recipeOfferPrice(String potion) {
        return (potionPrice(offeredRecipe(potion)) + 1) / 2;
    }

    private List<String> offeredRecipe(String potion) {
        Customer customer = currentCustomer();
        if (!(customer instanceof RecipeDealer)) {
            throw new IllegalStateException("Only recipe dealers quote formulas.");
        }
        if (potion == null || !recipeOffers().contains(potion)) {
            throw new IllegalArgumentException("Unknown formula offer.");
        }
        List<String> recipe = ((RecipeDealer) customer).getOffers().get(potion);
        return recipe;
    }

    public int buyRecipe(int item) {
        return buyRecipe(item, 0);
    }

    public int buyRecipe(int item, int haggle) {
        requireOpen();
        Customer customer = currentCustomer();
        if (!(customer instanceof RecipeDealer)) {
            throw new IllegalStateException("Only recipe dealers sell formulas.");
        }
        List<String> offers = recipeOffers();
        if (item < 1 || item > offers.size()) {
            throw new IllegalArgumentException("Choose a recipe offer number.");
        }
        String potion = offers.get(item - 1);
        List<String> recipe = ((RecipeDealer) customer).getOffers().get(potion);
        int price = dealPrice(recipeOfferPrice(potion), false, haggle);
        if (gold < price) {
            throw new IllegalArgumentException("Not enough gold.");
        }
        gold -= price;
        player.saveRecipe(potion, new ArrayList<>(recipe));
        applyHaggle(haggle);
        return price;
    }

    public boolean recipeOfferNeedsLockedHerbs(String potion) {
        return !availableIngredients.containsAll(offeredRecipe(potion));
    }

    private void requireOpen() {
        if (isOver()) {
            throw new IllegalStateException("The game has ended.");
        }
    }

    public int buyHerb(int item, int quantity) {
        return buyHerb(item, quantity, 0);
    }

    public int buyHerb(int item, int quantity, int haggle) {
        requireOpen();
        if (!(currentCustomer() instanceof HerbVendor)) {
            throw new IllegalStateException("Only herbalists sell ingredients.");
        }
        List<String> offers = vendorOffers();
        if (item < 1 || item > offers.size() || quantity < 1) {
            throw new IllegalArgumentException("Choose an offer number and a positive quantity.");
        }
        String herb = offers.get(item - 1);
        boolean rare = RARE_HERB.equals(herb);
        if (rare && !canBuyRareHerb()) {
            throw new IllegalArgumentException("Moonpetal requires reputation 65 and 2 correct orders today.");
        }
        int price = dealPrice(herbPrice(herb), false, haggle);
        if (quantity > gold / price) {
            throw new IllegalArgumentException("Not enough gold.");
        }
        int total = price * quantity;
        gold -= total;
        stock.put(herb, stockOf(herb) + quantity);
        if (rare && !availableIngredients.contains(herb)) {
            availableIngredients.add(herb);
        }
        if (rare && !specialQueued) {
            customers.enqueue(new SpecialCustomer("Duke Lancaster",
                recipes.get(specialRecipe).getName(), specialRecipe));
            specialQueued = true;
        }
        applyHaggle(haggle);
        return total;
    }

    public List<String> vendorOffers() {
        Customer customer = currentCustomer();
        return customer instanceof HerbVendor ? ((HerbVendor) customer).getOffers() : List.of();
    }

    public boolean canBuyRareHerb() {
        return getReputation() >= RARE_REPUTATION && dailySales >= RARE_DAILY_SALES;
    }

    public int stockOf(String herb) {
        return stock.getOrDefault(herb, 0);
    }

    public int getGold() {
        return gold;
    }

    public int getDay() {
        return day;
    }

    public int getDailySales() {
        return dailySales;
    }

    public int getDailyGoldChange() {
        return gold - openingGold;
    }

    public int getReputation() {
        return player.getReputation();
    }

    public String getPlayerName() {
        return player.getName();
    }

    public int getScore() {
        return getReputation() + getDiscoveredPotionCount();
    }

    public int getDiscoveredPotionCount() {
        return player.getSavedRecipes().size();
    }

    public Map<String, List<String>> getAdvancedFormulas() {
        if (!advancedOrdersUnlocked) {
            return Map.of();
        }
        return Map.of("Royal Restorative",
            List.of(recipes.get(starterRecipe).getName(), NEW_POTIONS.get(0)),
            "Winterward Elixir", NEW_POTIONS.subList(1, 3));
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

}

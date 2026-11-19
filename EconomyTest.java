import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;

public class EconomyTest {
    @Test
    public void unaffordableFormulaLeavesBookAndBalanceUnchanged() {
        Game game = new Game("Tester", 0L);
        game.declineCustomer();
        String item = game.vendorOffers().get(0);
        game.buyHerb(1, game.getGold() / game.herbPrice(item));
        while (!(game.currentCustomer() instanceof RecipeDealer)) {
            game.declineCustomer();
        }
        Customer dealer = game.currentCustomer();
        List<String> offers = game.recipeOffers();
        int gold = game.getGold();
        int known = game.getDiscoveredPotionCount();
        int reputation = game.getReputation();
        int offer = 0;
        while (offer < offers.size() && game.recipeOfferPrice(offers.get(offer)) <= gold) {
            offer++;
        }
        assertTrue(offer < offers.size());
        final int itemNumber = offer + 1;
        assertThrows(IllegalArgumentException.class, () -> game.buyRecipe(itemNumber));
        assertEquals(gold, game.getGold());
        assertEquals(known, game.getDiscoveredPotionCount());
        assertEquals(reputation, game.getReputation());
        assertSame(dealer, game.currentCustomer());
    }

    @Test
    public void invalidPriceRequestsAreRejected() {
        Game game = new Game("Tester", 0L);
        assertThrows(IllegalArgumentException.class, () -> game.herbPrice("Unknown herb"));
        assertThrows(IllegalArgumentException.class, () -> game.potionPrice(List.of()));
        assertThrows(IllegalArgumentException.class, () -> game.dealPrice(0, false, 0));
        assertThrows(IllegalStateException.class, () -> game.recipeOfferNeedsLockedHerbs("Healing"));
        while (!(game.currentCustomer() instanceof RecipeDealer)) {
            game.declineCustomer();
        }
        assertThrows(IllegalArgumentException.class,
            () -> game.recipeOfferNeedsLockedHerbs("Unknown potion"));
    }

    @Test
    public void zeroStockRejectsBrewingWithoutAdvancingTheQueue() {
        Game game = new Game("Tester", 0L);
        List<String> recipe = game.starterRecipe();
        for (int day = 1; day <= 3; day++) {
            while (!game.currentCustomer().getName().equals("Villager")) {
                game.declineCustomer();
            }
            assertEquals(Game.Outcome.CORRECT, game.serve(recipe));
            while (!game.isDayClosed()) {
                game.declineCustomer();
            }
            game.startNextDay();
        }
        while (!game.currentCustomer().getName().equals("Villager")) {
            game.declineCustomer();
        }
        Customer buyer = game.currentCustomer();
        int gold = game.getGold();
        int reputation = game.getReputation();
        assertEquals(0, game.stockOf(recipe.get(0)));
        assertThrows(IllegalArgumentException.class, () -> game.serve(recipe));
        assertSame(buyer, game.currentCustomer());
        assertEquals(gold, game.getGold());
        assertEquals(reputation, game.getReputation());
        assertEquals(0, game.getDailySales());
    }

    @Test
    public void sellingAndRestockingCarryAcrossDailySettlement() {
        Game game = new Game("Tester", 0L);
        int firstDayVisitors = game.waitingCustomerNames().size() + 1;
        String herb = game.starterRecipe().get(0);
        int earned = game.potionPrice(game.starterRecipe());
        int restock = 2 * game.herbPrice(herb);
        assertThrows(IllegalStateException.class, game::startNextDay);
        assertThrows(IllegalStateException.class, () -> game.buyHerb(1, 1));
        assertEquals(Game.Outcome.CORRECT, game.serve(game.starterRecipe()));
        assertEquals(2, game.stockOf(herb));
        assertEquals(30 + earned, game.getGold());
        assertEquals(1, game.getDailySales());
        Customer supplier = game.currentCustomer();
        assertTrue(supplier instanceof HerbVendor);
        List<String> initialOffers = game.vendorOffers();
        int rare = game.vendorOffers().indexOf(Game.RARE_HERB) + 1;
        assertThrows(IllegalArgumentException.class, () -> game.buyHerb(rare, 1));
        assertThrows(IllegalArgumentException.class, () -> game.buyHerb(1, 0));
        assertThrows(IllegalArgumentException.class, () -> game.buyHerb(1, Integer.MAX_VALUE));
        assertEquals(30 + earned, game.getGold());
        assertEquals(0, game.stockOf(Game.RARE_HERB));
        assertSame(supplier, game.currentCustomer());
        assertEquals(restock, game.buyHerb(game.vendorOffers().indexOf(herb) + 1, 2));
        assertEquals(initialOffers, game.vendorOffers());
        while (!game.isDayClosed()) {
            game.declineCustomer();
        }
        assertEquals(earned - restock, game.getDailyGoldChange());
        game.startNextDay();
        assertEquals(2, game.getDay());
        assertNotEquals(firstDayVisitors, game.waitingCustomerNames().size() + 1);
        assertEquals(30 + earned - restock, game.getGold());
        assertEquals(4, game.stockOf(herb));
        assertEquals(65, game.getReputation());
        assertEquals(0, game.getDailySales());
        assertEquals(0, game.getDailyGoldChange());
    }

    @Test
    public void moonpetalUnlocksOneSpecialOrderAndPaysOnDelivery() {
        Game game = new Game("Tester", 0L);
        List<String> starting = game.getAvailableIngredients();
        game.serve(game.starterRecipe());
        while (!game.currentCustomer().getName().equals("Buyer 5")) {
            game.declineCustomer();
        }
        assertEquals(Game.Outcome.CORRECT,
            game.serve(List.of(starting.get(0), starting.get(2), starting.get(3))));
        assertTrue(game.canBuyRareHerb());
        while (!game.vendorOffers().contains(Game.RARE_HERB)) {
            game.declineCustomer();
        }
        int offer = game.vendorOffers().size();
        int rarePrice = game.dealPrice(game.herbPrice(Game.RARE_HERB), false, 0);
        assertEquals(rarePrice, game.buyHerb(offer, 1));
        game.buyHerb(offer, 1);
        assertEquals(1L, game.waitingCustomerNames().stream()
            .filter(name -> name.contains("Duke Lancaster")).count());
        while (!(game.currentCustomer() instanceof SpecialCustomer)) {
            game.declineCustomer();
        }
        int before = game.getGold();
        List<String> recipe = game.currentCustomer().getClue();
        assertTrue(recipe.contains(Game.RARE_HERB));
        int expected = game.dealPrice(game.potionPrice(recipe), true, 0);
        assertEquals(Game.Outcome.CORRECT, game.serve(recipe));
        assertEquals(before + expected, game.getGold());
        assertEquals(1, game.stockOf(Game.RARE_HERB));
        assertTrue(game.isDayClosed());
    }

    @Test
    public void suppliersHaveDistinctCatalogsAndLeaveALateMoonpetalOffer() {
        for (long seed : List.of(0L, 15L, 123L)) {
            Game game = new Game("Tester", seed);
            java.util.Set<java.util.Set<String>> catalogs = new java.util.HashSet<>();
            List<String> lastOffers = List.of();
            while (!game.isDayClosed()) {
                lastOffers = game.vendorOffers();
                if (!lastOffers.isEmpty()) {
                    assertTrue(catalogs.add(java.util.Set.copyOf(lastOffers)));
                }
                game.declineCustomer();
            }
            assertTrue(catalogs.size() >= 2);
            assertTrue(lastOffers.contains(Game.RARE_HERB));
        }
    }

    @Test
    public void pricesVaryByHerbAndByDayAndTheMarkupRoundsUp() {
        Game game = new Game("Tester", 0L);
        List<String> recipe = game.starterRecipe();
        int cost = recipe.stream().mapToInt(game::herbPrice).sum();
        assertEquals((int) Math.ceil(cost * 1.15), game.potionPrice(recipe));
        assertTrue(game.getIngredientNames().stream()
            .filter(herb -> !herb.equals(Game.RARE_HERB)).map(game::herbPrice).distinct().count() > 1);
        int before = game.herbPrice(recipe.get(0));
        while (!game.isDayClosed()) {
            game.declineCustomer();
        }
        game.startNextDay();
        assertNotEquals(before, game.herbPrice(recipe.get(0)));
    }

    @Test
    public void pastTipsAndPurchasedFormulasStayInTheBook() {
        Game game = new Game("Tester", 0L);
        RecipeDealer dealer = null;
        boolean rememberedTip = false;
        while (!game.isDayClosed()) {
            Customer customer = game.currentCustomer();
            if (customer instanceof RecipeDealer) {
                dealer = (RecipeDealer) customer;
                String potion = game.recipeOffers().get(0);
                List<String> recipe = dealer.getOffers().get(potion);
                int price = game.recipeOfferPrice(potion);
                List<Integer> stocks = recipe.stream().map(game::stockOf)
                    .collect(java.util.stream.Collectors.toList());
                assertEquals((game.potionPrice(recipe) + 1) / 2, price);
                assertThrows(IllegalArgumentException.class, () -> game.buyRecipe(0));
                assertThrows(IllegalArgumentException.class, () -> game.recipeOfferPrice("unknown"));
                int before = game.getGold();
                assertEquals(price, game.buyRecipe(1));
                assertEquals(before - price, game.getGold());
                assertEquals(stocks, recipe.stream().map(game::stockOf)
                    .collect(java.util.stream.Collectors.toList()));
                assertTrue(game.getDiscoveredRecipes().get(potion).contains(recipe));
                assertFalse(game.recipeOffers().contains(potion));
                assertFalse(game.recipeOffers().isEmpty());
                assertEquals(before - price, game.getGold());
            }
            game.declineCustomer();
            if (customer.getClue().size() == 2) {
                rememberedTip = true;
                assertEquals(customer.getClue(), game.getRecipeTips().get(customer.getDesiredPotion()));
            }
        }
        assertNotNull(dealer);
        assertTrue(rememberedTip);
        game.startNextDay();
        assertFalse(game.getRecipeTips().isEmpty());
    }

    @Test
    public void dealerOffersNamedFormulasBeyondTheUnlockedIngredients() {
        Game game = new Game("Tester", 0L);
        while (!(game.currentCustomer() instanceof RecipeDealer)) {
            game.declineCustomer();
        }
        List<String> offers = game.recipeOffers();
        assertEquals(3, offers.size());
        assertTrue(offers.stream().noneMatch(String::isBlank));
        assertTrue(offers.stream().anyMatch(game::recipeOfferNeedsLockedHerbs));
        for (String potion : offers) {
            assertTrue(game.recipeOfferPrice(potion) > 0);
        }
    }

    @Test
    public void negotiatedTradesAdjustReputationOnlyWhenTheyComplete() {
        Game game = new Game("Tester", 0L);
        List<String> recipe = game.starterRecipe();
        int sale = game.dealPrice(game.potionPrice(recipe), true, -20);
        assertThrows(IllegalArgumentException.class, () -> game.serveWithDetails(recipe, 17));
        assertEquals(50, game.getReputation());
        assertEquals(sale, game.serveWithDetails(recipe, -20).getGoldEarned());
        assertEquals(69, game.getReputation());
        String herb = game.vendorOffers().get(0);
        int beforeGold = game.getGold();
        int beforeStock = game.stockOf(herb);
        assertThrows(IllegalArgumentException.class, () -> game.buyHerb(1, 1000, 50));
        assertEquals(beforeGold, game.getGold());
        assertEquals(beforeStock, game.stockOf(herb));
        assertEquals(69, game.getReputation());
        assertEquals(game.dealPrice(game.herbPrice(herb), false, 50), game.buyHerb(1, 1, 50));
        assertEquals(59, game.getReputation());
        assertEquals(game.dealPrice(game.herbPrice(herb), false, -20), game.buyHerb(1, 1, -20));
        assertEquals(63, game.getReputation());
    }

    @Test
    public void trustedRatesApplyAutomaticallyToBothSides() {
        Game game = new Game("Tester", 0L);
        assertEquals(100, game.dealPrice(100, false, 0));
        assertEquals(100, game.dealPrice(100, true, 0));
        List<String> starting = game.getAvailableIngredients();
        game.serve(game.starterRecipe());
        while (!game.currentCustomer().getName().equals("Buyer 5")) {
            game.declineCustomer();
        }
        game.serve(List.of(starting.get(0), starting.get(2), starting.get(3)));
        assertEquals(80, game.getReputation());
        assertEquals(95, game.dealPrice(100, false, 0));
        assertEquals(105, game.dealPrice(100, true, 0));
    }
}

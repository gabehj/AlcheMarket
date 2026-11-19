import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import org.junit.Test;

public class AdvancedOrderTest {
    @Test
    public void valeNeedsTwoBottlesAndPaysOnceAfterCombining() {
        Game game = new Game("Tester", 0L);
        closeDay(game);
        game.startNextDay();
        moveTo(game, customer -> customer.getName().equals("Herbalist 1"));
        game.buyHerb(game.vendorOffers().indexOf("Silverleaf") + 1, 1);
        closeDay(game);
        game.startNextDay();
        moveTo(game, customer -> customer instanceof AdvancedOrder);
        AdvancedOrder vale = (AdvancedOrder) game.currentCustomer();
        assertTrue(vale.isRoyalVisit());
        assertTrue(game.getAdvancedFormulas().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> game.combineAdvancedOrder(0));
        assertThrows(IllegalArgumentException.class, () ->
            game.brewAdvancedComponent(vale.getFormulas().get(1)));
        assertEquals(0, vale.getPreparedCount());
        assertEquals(1, game.stockOf("Silverleaf"));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Validation controller = new Validation(game, new ConsoleView(new Scanner(""),
            new PrintStream(output, true, StandardCharsets.UTF_8)));
        int balance = game.getGold();
        int cost = vale.getFormulas().stream().flatMap(List::stream).mapToInt(game::herbPrice).sum();
        for (List<String> formula : vale.getFormulas()) {
            for (String herb : formula) {
                assertTrue(controller.processLine(
                    Integer.toString(game.getAvailableIngredients().indexOf(herb) + 1)));
            }
            assertTrue(controller.processLine("b"));
            assertSame(vale, game.currentCustomer());
            assertEquals(balance, game.getGold());
        }
        assertEquals(2, vale.getPreparedCount());
        assertEquals(0, game.stockOf("Silverleaf"));
        assertTrue(controller.processLine("combine"));
        assertEquals(balance + (cost * 115 + 99) / 100, game.getGold());
        assertNotSame(vale, game.currentCustomer());
        assertEquals(2, game.getAdvancedFormulas().size());
        assertEquals(1, game.getDailySales());
        assertThrows(IllegalStateException.class, () -> game.combineAdvancedOrder(0));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Delivered: Royal Restorative"));
    }

    @Test
    public void ordinaryTierTwoOrdersOnlyAppearAfterValeVisits() {
        Game game = new Game("Tester", 15L);
        for (int day = 1; day < 3; day++) {
            while (!game.isDayClosed()) {
                assertFalse(game.currentCustomer() instanceof AdvancedOrder);
                game.declineCustomer();
            }
            game.startNextDay();
        }
        moveTo(game, customer -> customer instanceof AdvancedOrder);
        game.declineCustomer();
        boolean found = false;
        for (int attempt = 0; attempt < 14 && !found; attempt++) {
            closeDay(game);
            game.startNextDay();
            while (!game.isDayClosed() && !found) {
                Customer customer = game.currentCustomer();
                if (customer instanceof AdvancedOrder) {
                    AdvancedOrder advanced = (AdvancedOrder) customer;
                    assertFalse(advanced.isRoyalVisit());
                    assertTrue(game.getAdvancedFormulas().containsKey(advanced.getDesiredPotion()));
                    found = true;
                }
                game.declineCustomer();
            }
        }
        assertTrue(found);
    }

    private void moveTo(Game game, Predicate<Customer> target) {
        while (!game.isDayClosed() && !target.test(game.currentCustomer())) {
            game.declineCustomer();
        }
        assertFalse(game.isDayClosed());
    }

    private void closeDay(Game game) {
        while (!game.isDayClosed()) {
            game.declineCustomer();
        }
    }
}

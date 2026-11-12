import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.junit.Test;

public class PrototypeTest {
    @Test
    public void modelServesThreeCustomersWithoutAView() {
        Game game = new Game("Tester", 15L);
        Customer first = game.currentCustomer();
        List<String> recipe = game.starterRecipe();
        List<String> reversed = List.of(recipe.get(2), recipe.get(1), recipe.get(0));
        assertEquals(Game.Outcome.CORRECT, game.serve(reversed));
        assertEquals(65, game.getReputation());
        assertNotSame(first, game.currentCustomer());
        assertEquals(Game.Outcome.CORRECT, game.serve(recipe));
        assertEquals(Game.Outcome.CORRECT, game.serve(List.of(recipe.get(0),
            recipe.get(1), game.getAvailableIngredients().get(3))));
        assertTrue(game.isOver());
        assertEquals(95, game.getReputation());
        assertThrows(NoSuchElementException.class, () -> game.serve(recipe));
    }

    @Test
    public void controllerRejectsScopeInputsThenRunsToCompletion() {
        Game game = new Game("Tester", 15L);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConsoleView view = new ConsoleView(new Scanner(new StringReader("")),
            new PrintStream(output, true, StandardCharsets.UTF_8));
        Validation controller = new Validation(game, view);
        Customer first = game.currentCustomer();
        String locked = game.getIngredientNames().stream()
            .filter(name -> !game.getAvailableIngredients().contains(name))
            .findFirst().orElseThrow();
        for (String badInput : List.of("", "unknown", "brew", "brew 1",
            "brew 1,2", "brew -1,2,3", "brew 999,2,3",
            "brew 99999999999999999999,2,3", "brew abc,2,3",
            "brew Mandrak Root,2,3", "brew 1,1,2",
            "brew " + locked + ",2,3")) {
            output.reset();
            assertTrue(badInput, controller.processLine(badInput));
            assertTrue(badInput, output.toString(StandardCharsets.UTF_8).contains("Invalid"));
            assertSame(first, game.currentCustomer());
            assertEquals(50, game.getReputation());
        }
        String brew = "brew " + String.join(",", game.starterRecipe());
        assertTrue(controller.processLine(brew));
        assertTrue(controller.processLine(brew));
        assertFalse(controller.processLine("brew 1,2,4"));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Quest complete"));
        output.reset();
        assertFalse(controller.processLine(brew));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("No customers"));
        assertEquals(95, game.getReputation());
    }

    @Test
    public void modelRejectsInvalidSelectionsWithoutServing() {
        Game game = new Game("Tester", 15L);
        Customer first = game.currentCustomer();
        assertThrows(IllegalArgumentException.class, () -> game.serve(null));
        assertThrows(IllegalArgumentException.class, () -> game.serve(List.of("Unknown")));
        assertThrows(IllegalArgumentException.class, () -> game.serve(List.of(
            game.starterRecipe().get(0), game.starterRecipe().get(1), "Unknown")));
        assertSame(first, game.currentCustomer());
        assertEquals(50, game.getReputation());
        assertEquals(1, game.getDiscoveredPotionCount());
    }
}

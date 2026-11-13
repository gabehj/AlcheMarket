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
    public void modelServesSevenCustomersWithoutAView() {
        Game game = new Game("Tester", 15L);
        Customer first = game.currentCustomer();
        List<String> recipe = game.starterRecipe();
        List<String> reversed = List.of(recipe.get(2), recipe.get(1), recipe.get(0));
        assertEquals(Game.Outcome.CORRECT, game.serve(reversed));
        assertEquals(65, game.getReputation());
        assertNotSame(first, game.currentCustomer());
        for (List<Integer> order : List.of(List.of(1, 4), List.of(1, 2, 3, 5),
            List.of(1, 2, 3, 4, 5), List.of(1, 2, 4, 5), List.of(1, 3, 5, 8),
            List.of(1, 2, 4, 5))) {
            List<String> available = game.getAvailableIngredients();
            List<String> selection = new java.util.ArrayList<>();
            for (int index : order) {
                selection.add(available.get(index - 1));
            }
            assertEquals(Game.Outcome.CORRECT, game.serve(selection));
        }
        assertTrue(game.isOver());
        assertEquals(155, game.getReputation());
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
            "brew 1,2,3,4,5,6", "brew -1,2,3", "brew 999,2,3",
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
        for (String order : List.of(brew, "brew 1,4", "brew 1,2,3,5",
            "brew 1,2,3,4,5", "brew 1,2,4,5", "brew 1,3,5,8")) {
            assertTrue(controller.processLine(order));
        }
        assertFalse(controller.processLine("brew 1,2,4,5"));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Quest complete"));
        output.reset();
        assertFalse(controller.processLine(brew));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("No customers"));
        assertEquals(155, game.getReputation());
    }

    @Test
    public void poisonEndsGameAndFurtherServiceLeavesStateUnchanged() {
        Game game = new Game("Tester", 0L);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream modelOutput = new ByteArrayOutputStream();
        ConsoleView view = new ConsoleView(new Scanner(""),
            new PrintStream(output, true, StandardCharsets.UTF_8));
        Validation controller = new Validation(game, view);

        PrintStream original = System.out;
        try (PrintStream captured = new PrintStream(modelOutput, true, StandardCharsets.UTF_8)) {
            System.setOut(captured);
            assertFalse(controller.processLine("brew 3,4,5"));
        }
        finally {
            System.setOut(original);
        }
        assertEquals(0, modelOutput.size());
        String result = output.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("The brew was poison."));
        assertTrue(result.contains("Game over."));
        assertTrue(game.isOver());
        assertEquals(0, game.getReputation());

        Customer next = game.currentCustomer();
        int discoveries = game.getDiscoveredPotionCount();
        output.reset();
        assertThrows(IllegalStateException.class, () -> game.serve(game.starterRecipe()));
        assertFalse(controller.processLine("brew 1,2,3"));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("The game has ended."));
        assertSame(next, game.currentCustomer());
        assertEquals(0, game.getReputation());
        assertEquals(discoveries, game.getDiscoveredPotionCount());
    }

    @Test
    public void modelRejectsInvalidSelectionsWithoutServing() {
        Game game = new Game("Tester", 15L);
        Customer first = game.currentCustomer();
        assertThrows(IllegalArgumentException.class, () -> game.serve(null));
        assertThrows(IllegalArgumentException.class, () -> game.serve(List.of("Unknown")));
        assertThrows(IllegalArgumentException.class, () ->
            game.serve(java.util.Collections.nCopies(6, game.starterRecipe().get(0))));
        assertThrows(IllegalArgumentException.class, () -> game.serve(List.of(
            game.starterRecipe().get(0), game.starterRecipe().get(1), "Unknown")));
        assertSame(first, game.currentCustomer());
        assertEquals(50, game.getReputation());
        assertEquals(1, game.getDiscoveredPotionCount());
    }
}

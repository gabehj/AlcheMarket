import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import org.junit.Test;

public class CustomerDialogueTest {
    @Test
    public void selectsOneIngredientAtATimeAndOnlyServesOnBrew() {
        Game game = new Game("Tester");
        Customer first = game.currentCustomer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConsoleView view = new ConsoleView(new Scanner(
            "1\n1\n2\nundo\nreset\n1\n2\nbrew\n3\nundo\n3\nbrew\nquit\n"),
            new PrintStream(output, true, StandardCharsets.UTF_8));

        new Validation(game, view).run();

        String transcript = output.toString(StandardCharsets.UTF_8);
        assertTrue(transcript.contains("Cauldron  [1] + [ ] + [ ]"));
        assertTrue(transcript.contains("Cauldron  [1] + [2] + [3]"));
        assertTrue(transcript.contains("Cauldron cleared."));
        assertTrue(transcript.contains("already in the cauldron"));
        assertTrue(transcript.contains("Select 3 ingredients before brewing."));
        assertNotSame(first, game.currentCustomer());
        assertEquals(65, game.getReputation());
        int nextOrder = transcript.lastIndexOf("Order   Healing");
        assertTrue(nextOrder >= 0);
        assertTrue(transcript.substring(nextOrder).contains("Cauldron  [ ] + [ ] + [ ]"));
    }

    @Test
    public void everyPoolContainsFiveDistinctRenderableLines() {
        CustomerDialogue dialogue = new CustomerDialogue();
        for (CustomerType type : CustomerType.values()) {
            for (CustomerDialogue.Event event : CustomerDialogue.Event.values()) {
                java.util.Set<String> lines = new java.util.HashSet<>();
                for (int index = 0; index < 5; index++) {
                    final int choice = index;
                    String line = dialogue.draw(type, event, "Healing", "Adrenaline",
                        new Random() {
                            @Override
                            public int nextInt(int bound) {
                                assertTrue(bound >= 5);
                                return choice;
                            }
                        });
                    assertFalse(line.contains("{"));
                    if (event == CustomerDialogue.Event.GREETING) {
                        assertTrue(line.contains("Healing"));
                    }
                    lines.add(line);
                }
                assertEquals(5, lines.size());
            }
        }
    }

    @Test
    public void redisplayReusesGreetingAndWrongPotionNamesBothOrders() {
        Game game = new Game("Tester");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConsoleView view = new ConsoleView(new Scanner(""),
            new PrintStream(output, true, StandardCharsets.UTF_8));
        Validation controller = new Validation(game, view);
        Customer first = game.currentCustomer();
        view.showTurn(first, game.getAvailableIngredients(), game.getNumIngredients(),
            List.of(), game.getReputation());
        String firstDisplay = output.toString(StandardCharsets.UTF_8);
        output.reset();
        controller.processLine("brew 999,2,3");
        assertFalse(output.toString(StandardCharsets.UTF_8).contains(first.getName() + ": "));
        output.reset();
        view.showTurn(first, game.getAvailableIngredients(), game.getNumIngredients(),
            List.of(), game.getReputation());
        assertEquals(firstDisplay, output.toString(StandardCharsets.UTF_8));

        controller.processLine("brew 1,2,3");
        controller.processLine("brew 1,2,3");
        Customer third = game.currentCustomer();
        output.reset();
        assertFalse(controller.processLine("brew 1,2,3"));
        String wrong = output.toString(StandardCharsets.UTF_8);
        assertTrue(wrong.contains(third.getName() + ": "));
        assertTrue(wrong.contains("Ordered: Luck of a Leprechaun"));
        assertTrue(wrong.contains("Delivered: Healing"));
    }
}

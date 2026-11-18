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
        Game game = new Game("Tester", 15L);
        Customer first = game.currentCustomer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConsoleView view = new ConsoleView(new Scanner(
            "1\n1\n2\nundo\nreset\n1\nbrew\n2\n3\nundo\n3\nbrew\nquit\n"),
            new PrintStream(output, true, StandardCharsets.UTF_8));

        new Validation(game, view).run();

        String transcript = output.toString(StandardCharsets.UTF_8);
        assertTrue(transcript.contains("Cauldron  [1] + [ ] + [ ]"));
        assertTrue(transcript.contains("Cauldron  [1] + [2] + [3]"));
        assertTrue(transcript.contains("Cauldron cleared."));
        assertTrue(transcript.contains("already in the cauldron"));
        assertTrue(transcript.contains("Select 3 ingredients."));
        assertNotSame(first, game.currentCustomer());
        assertEquals(65, game.getReputation());
        int nextOrder = transcript.lastIndexOf(game.currentCustomer().getName() + " / ");
        assertTrue(nextOrder >= 0);
        assertTrue(transcript.substring(nextOrder).contains("Cauldron  [ ] + [ ] + [ ]"));
        assertTrue(transcript.contains("WAITING  [1 Traveler]  [2 Merchant]"));
        assertTrue(transcript.substring(nextOrder).contains("WAITING  [1 Merchant]"));
    }

    @Test
    public void counterFitsWideAndNarrowTerminals() {
        Game game = new Game("Tester", 15L);
        for (int width : List.of(60, 100)) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ConsoleView view = new ConsoleView(new Scanner(""),
                new PrintStream(output, true, StandardCharsets.UTF_8), width);
            view.showTurn(game.currentCustomer(), game.getAvailableIngredients(), List.of(),
                game.getReputation(), game.waitingCustomerNames(), null, "");
            List<String> lines = output.toString(StandardCharsets.UTF_8).lines()
                .collect(java.util.stream.Collectors.toList());
            assertTrue(lines.stream().allMatch(line -> line.length() == width));
            assertTrue(lines.get(1).endsWith("Reputation 50 |"));
            boolean sideBySide = lines.stream().anyMatch(line -> line.contains("BREWING TABLE")
                && line.contains("CURRENT CUSTOMER") && line.contains("INGREDIENTS"));
            assertEquals(width == 100, sideBySide);
        }
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
        Game game = new Game("Tester", 15L);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ConsoleView view = new ConsoleView(new Scanner(""),
            new PrintStream(output, true, StandardCharsets.UTF_8));
        Validation controller = new Validation(game, view);
        Customer first = game.currentCustomer();
        String greeting = view.greetingFor(first);
        view.showTurn(first, game.getAvailableIngredients(), List.of(), game.getReputation(),
            game.waitingCustomerNames(), greeting, "");
        String firstDisplay = output.toString(StandardCharsets.UTF_8);
        output.reset();
        controller.processLine("brew 999,2,3");
        assertFalse(output.toString(StandardCharsets.UTF_8).contains(first.getName() + ": "));
        output.reset();
        view.showTurn(first, game.getAvailableIngredients(), List.of(), game.getReputation(),
            game.waitingCustomerNames(), greeting, "");
        assertEquals(firstDisplay, output.toString(StandardCharsets.UTF_8));

        controller.processLine("brew 1,2,3");
        controller.processLine("brew 1,2,3");
        Customer second = game.currentCustomer();
        output.reset();
        assertTrue(controller.processLine("brew 1,2,3"));
        String wrong = output.toString(StandardCharsets.UTF_8);
        assertTrue(wrong.contains(second.getName() + ": "));
        assertTrue(wrong.contains("Ordered: " + second.getDesiredPotion()));
        assertTrue(wrong.contains("Delivered: Healing"));
    }
}

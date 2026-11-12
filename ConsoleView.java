import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.StringJoiner;

public class ConsoleView {
    private static final String DIVIDER = "----------------------------------------------------------------";
    private final Scanner input;
    private final PrintStream output;
    private final boolean interactiveTerminal;
    private final CustomerDialogue dialogue = new CustomerDialogue();
    private final Random dialogueRandom = new Random();
    private Customer greetedCustomer;
    private String greeting;

    public ConsoleView(Scanner input, PrintStream output) {
        this.input = input;
        this.output = output;
        String term = System.getenv("TERM");
        interactiveTerminal = output == System.out && System.console() != null
            && (System.getenv("WT_SESSION") != null || (term != null && !term.equals("dumb")));
    }

    public String readLine() {
        return input.hasNextLine() ? input.nextLine() : null;
    }

    public void showMessage(String message) {
        output.println(message);
    }

    public void refresh() {
        if (interactiveTerminal) {
            output.print("\033[2J\033[H");
        }
        else {
            output.println();
        }
    }

    public void prompt() {
        output.print("apothecary> ");
        output.flush();
    }

    public void showTurn(Customer customer, List<String> ingredients, int count,
        List<String> selected, int reputation) {
        if (greetedCustomer != customer) {
            greeting = dialogue.draw(customer.getType(), CustomerDialogue.Event.GREETING,
                customer.getDesiredPotion(), "", dialogueRandom);
            greetedCustomer = customer;
        }
        showMessage(DIVIDER);
        showMessage("  ALCHEMARKET                                  Reputation " + reputation);
        showMessage(DIVIDER);
        showMessage("Customer   " + customer.getName() + " / " + customer.getType());
        showMessage("Order   " + customer.getDesiredPotion());
        showMessage("");
        showDialogue(customer, greeting);
        if (!customer.getClue().isEmpty()) {
            showMessage("");
            showMessage("Recipe clue");
            customer.getClue().forEach(ingredient -> showMessage("  - " + ingredient));
        }

        showMessage("");
        showMessage("Ingredients");
        for (int index = 0; index < ingredients.size(); index++) {
            String ingredient = ingredients.get(index);
            output.printf("  %2d. %s %s%n", index + 1,
                selected.contains(ingredient) ? "[x]" : "[ ]", ingredient);
        }
        StringJoiner slots = new StringJoiner(" + ");
        for (int index = 0; index < count; index++) {
            slots.add(index < selected.size()
                ? "[" + (ingredients.indexOf(selected.get(index)) + 1) + "]" : "[ ]");
        }
        showMessage("");
        showMessage("Cauldron  " + slots);
        showMessage(selected.size() == count ? "Ready. Type brew to serve."
            : "Type an ingredient number to fill the next slot.");
        showMessage("undo  Last pick    reset  Clear    recipes  Book    quit  Exit");
        showMessage(DIVIDER);
    }

    public void showOutcome(Customer customer, Game.ServiceResult result, int reputation) {
        showMessage("");
        showMessage("Result");
        showMessage(DIVIDER);
        switch (result.getOutcome()) {
            case CORRECT:
                showReaction(customer, CustomerDialogue.Event.CORRECT, result.getBrewedPotion());
                showMessage("The customer received the requested potion. A material was unlocked.");
                break;
            case WRONG_POTION:
                showReaction(customer, CustomerDialogue.Event.WRONG, result.getBrewedPotion());
                showMessage("Ordered: " + customer.getDesiredPotion()
                    + " | Delivered: " + result.getBrewedPotion());
                break;
            case DUD:
                showMessage("The ingredients produced no potion.");
                break;
            case POISON:
                showMessage("The brew was poison.");
                break;
        }
        showMessage("Reputation: " + reputation);
    }

    private void showReaction(Customer customer, CustomerDialogue.Event event, String brewed) {
        showDialogue(customer, dialogue.draw(customer.getType(), event,
            customer.getDesiredPotion(), brewed, dialogueRandom));
    }

    private void showDialogue(Customer customer, String text) {
        String line = customer.getName() + ":";
        for (String word : text.split(" ")) {
            if (line.length() + word.length() + 1 > DIVIDER.length()) {
                showMessage(line);
                line = " ";
            }
            line += " " + word;
        }
        showMessage(line);
    }

    public void showRecipeBook(Map<String, List<List<String>>> recipes) {
        showMessage("");
        showMessage("Recipe book");
        showMessage(DIVIDER);
        recipes.forEach((potion, combinations) -> {
            showMessage(potion);
            for (List<String> recipe : combinations) {
                recipe.forEach(ingredient -> showMessage("  - " + ingredient));
                showMessage("");
            }
        });
        showMessage(DIVIDER);
    }

    public void showScore(int reputation, int discoveries, int score) {
        showMessage("");
        showMessage(DIVIDER);
        showMessage(reputation > 0 ? "Quest complete!" : "Game over.");
        showMessage("Reputation: " + reputation);
        showMessage("Potion types discovered: " + discoveries);
        showMessage("Score: " + score);
    }
}

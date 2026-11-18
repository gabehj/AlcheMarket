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

    public void showTurn(Customer customer, List<String> ingredients,
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
        for (int index = 0; index < Game.RECIPE_SIZE; index++) {
            slots.add(index < selected.size()
                ? "[" + (ingredients.indexOf(selected.get(index)) + 1) + "]" : "[ ]");
        }
        showMessage("");
        showMessage("Cauldron  " + slots);
        showMessage("Choose 3 distinct ingredients.");
        showMessage(selected.size() == Game.RECIPE_SIZE ? "Ready. Type brew to serve."
            : "Type an ingredient number to fill the next slot.");
        showMessage("undo  Last pick    reset  Clear    recipes  Book    quit  Exit");
        showMessage(DIVIDER);
    }

    public void showOutcome(Customer customer, Game.ServiceResult result, int reputation) {
        showMessage("");
        
        // Dramatic Pause
        output.print("Brewing");
        try {
            for (int i = 0; i < 3; i++) {
                output.print(" .");
                output.flush();
                Thread.sleep(300);
            }
            output.println(" Complete!");
            Thread.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }


        switch (result.getOutcome()) {
            case CORRECT:
                output.println("You crafted: Potion of "+result.getBrewedPotion()+"!");
                output.println("Sell to "+customer.getName()+" -> ");
                input.nextLine();
                showReaction(customer, CustomerDialogue.Event.CORRECT, result.getBrewedPotion());
                
                try {
                    Thread.sleep(700);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                break;
            case WRONG_POTION:
                output.println("You crafted: Potion of "+result.getBrewedPotion()+"!");
                output.println("Sell to "+customer.getName()+"... maybe they won't notice? -> ");
                input.nextLine();
                showReaction(customer, CustomerDialogue.Event.WRONG, result.getBrewedPotion());
                showMessage("Ordered: " + customer.getDesiredPotion()
                    + " | Delivered: " + result.getBrewedPotion());
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                
                break;
            case DUD:
                showMessage("It was a DUD. The ingredients produced no potion.");
                output.println("Tell the "+customer.getName()+" that it can only be brewed during a blood moon -> ");
                input.nextLine();
                break;
            case POISON:
                showMessage("You crafted: "+result.getBrewedPotion());
                output.println("Sell to "+customer.getName()+". It's probably harmless -> ");
                input.nextLine();
                AsciiArt.printPoisoned(output);
                showMessage("The "+customer.getName()+" begins to writh on the ground, the flesh sloughing off their skin.");
                showMessage("They let out a tortuos howl, which is cut short only as the foul brew dissolves their throat. ");
                showMessage("Oops. Maybe later you'll brew a potion to bring them back. In the meantime, back to business!");
                input.nextLine();
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

    public void showScore(int reputation, int discoveries) {

        showMessage("");
        showMessage(DIVIDER);
        String success = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ V I C T O R Y ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
        success   +=   "\nQuest complete, you become the most renowned alchemist's of all the reals!";
        success   +=   "\nEnvied by rivals, admired by customers, and respected by kingdoms,";
        success   +=   "\nyour potion-making skills are truly impressive!";

        String fail = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ E X E C U T I O N ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";
        fail +=     "\nThe villagers have had enough with your charaltan snake oil. Soon, an angry ";
        fail +=     "\nmob crashes through your laboratory and drags you into the town square.";
        fail +=     "\nThere, you are burned at the stake, while jeered at from the crowd.";

        showMessage(reputation > 0 ? success : fail);
        showMessage("Reputation: " + reputation);
        showMessage("Potion types discovered: " + discoveries);
        
        if (reputation < 0) {
            int score = discoveries;
            showMessage("Score: 1 * Discoveries = " + score);
        } else {
            int score = reputation * discoveries;
            showMessage("Score: Reputation * Discoveries = " + score);
        }
    }
}

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public class ConsoleView {
    private final Scanner input;
    private final PrintStream output;

    public ConsoleView(Scanner input, PrintStream output) {
        this.input = input;
        this.output = output;
    }

    public String readLine() {
        return input.hasNextLine() ? input.nextLine() : null;
    }

    public void showMessage(String message) {
        output.println(message);
    }

    public void showTurn(Customer customer, List<String> ingredients, int count) {
        showMessage(customer.getName() + " requests " + customer.getDesiredPotion() + ".");
        showMessage("Recipe clue: " + String.join(", ", customer.getClue()));
        for (int index = 0; index < ingredients.size(); index++) {
            showMessage((index + 1) + ". " + ingredients.get(index));
        }
        showMessage("Enter brew followed by " + count
            + " comma-separated numbers or names, recipes, or quit.");
    }

    public void showOutcome(Game.Outcome outcome, int reputation) {
        switch (outcome) {
            case CORRECT:
                showMessage("The customer received the requested potion. A material was unlocked.");
                break;
            case WRONG_POTION:
                showMessage("The customer wanted a different potion.");
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

    public void showScore(int reputation, int discoveries, int score) {
        showMessage(reputation > 0 ? "Quest complete!" : "Game over.");
        showMessage("Reputation: " + reputation);
        showMessage("Potion types discovered: " + discoveries);
        showMessage("Score: " + score);
    }
}

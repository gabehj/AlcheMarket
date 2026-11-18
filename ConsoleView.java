import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.function.Function;

public class ConsoleView {
    private static final String DIVIDER = "----------------------------------------------------------------";
    private static final String GOLD = "1;33";
    private static final String CYAN = "1;36";
    private static final String GREEN = "1;32";
    private static final String RED = "1;31";
    private static final String MUTED = "90";
    private final Scanner input;
    private final PrintStream output;
    private final boolean interactiveTerminal;
    private final boolean colorEnabled;
    private final CustomerDialogue dialogue = new CustomerDialogue();
    private final Random dialogueRandom = new Random();
    private final int width;

    public ConsoleView(Scanner input, PrintStream output) {
        this(input, output, terminalWidth(System.getenv("COLUMNS")));
    }

    ConsoleView(Scanner input, PrintStream output, int width) {
        this(input, output, width, isInteractive(output));
    }

    ConsoleView(Scanner input, PrintStream output, int width, boolean interactiveTerminal) {
        this.width = Math.max(40, width);
        this.input = input;
        this.output = output;
        this.interactiveTerminal = interactiveTerminal;
        colorEnabled = interactiveTerminal && System.getenv("NO_COLOR") == null;
    }

    private static boolean isInteractive(PrintStream output) {
        String term = System.getenv("TERM");
        return output == System.out && System.console() != null
            && ("vscode".equalsIgnoreCase(System.getenv("TERM_PROGRAM")) || System.getenv("WT_SESSION") != null || (term != null && !term.equals("dumb")));
    }

    private static int terminalWidth(String columns) {
        try {
            return Integer.parseInt(columns);
        }
        catch (NumberFormatException error) {
            return 100;
        }
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
        output.print(color("apothecary> ", CYAN));
        output.flush();
    }

    public String greetingFor(Customer customer) {
        return dialogue.draw(customer.getType(), CustomerDialogue.Event.GREETING,
            customer.getDesiredPotion(), "", dialogueRandom);
    }

    public void showTurn(Customer customer, List<String> ingredients,
        List<String> selected, int reputation, List<String> waiting, String greeting,
        String feedback) {
        int inner = width - 4;
        String title = "ALCHEMARKET";
        String status = "Reputation " + reputation;
        border(width);
        showMessage(edge("| ") + color(title, GOLD)
            + " ".repeat(inner - title.length() - status.length())
            + color(status, reputation <= 0 ? RED : GOLD) + edge(" |"));
        border(width);

        List<String> order = new ArrayList<>();
        order.add("CURRENT CUSTOMER");
        order.add("");
        order.add(customer.getName() + " / " + customer.getType());
        order.add("Order   " + customer.getDesiredPotion());
        if (greeting != null) {
            order.add("");
            order.add(customer.getName() + ": " + greeting);
        }
        if (!customer.getClue().isEmpty()) {
            order.add("");
            order.add("Recipe clue");
            customer.getClue().forEach(ingredient -> order.add("- " + ingredient));
        }

        List<String> table = new ArrayList<>();
        table.add("BREWING TABLE");
        table.add("");
        StringJoiner slots = new StringJoiner(" + ");
        StringJoiner boxes = new StringJoiner("   ");
        for (int index = 0; index < Game.RECIPE_SIZE; index++) {
            String number = index < selected.size()
                ? Integer.toString(ingredients.indexOf(selected.get(index)) + 1) : " ";
            slots.add("[" + number + "]");
            boxes.add(String.format("| %2s |", number));
        }
        table.add("+----+   +----+   +----+");
        table.add(boxes.toString());
        table.add("+----+   +----+   +----+");
        table.add("+=====================+");
        table.add("  ||               ||");
        table.add("");
        table.add("Cauldron  " + slots);
        table.add(selected.size() == Game.RECIPE_SIZE ? "Ready. Press B to brew."
            : "Select " + (Game.RECIPE_SIZE - selected.size()) + " more.");

        List<String> shelf = new ArrayList<>();
        shelf.add("INGREDIENTS");
        shelf.add("");
        for (int index = 0; index < ingredients.size(); index++) {
            String ingredient = ingredients.get(index);
            shelf.add(String.format("%2d. %s %s", index + 1,
                selected.contains(ingredient) ? "[x]" : "[ ]", ingredient));
        }

        Function<String, String> orderColor = line -> {
            if (line.startsWith("Order   ")) {
                return GOLD;
            }
            if (line.equals("CURRENT CUSTOMER") || line.equals("Recipe clue")) {
                return CYAN;
            }
            return "";
        };
        Function<String, String> tableColor = line -> {
            if (line.startsWith("Ready.") || (!selected.isEmpty()
                && (line.startsWith("Cauldron") || line.startsWith("| ")))) {
                return GREEN;
            }
            if (line.equals("BREWING TABLE")) {
                return CYAN;
            }
            return "";
        };
        Function<String, String> shelfColor = line -> {
            if (line.contains("[x]")) {
                return GREEN;
            }
            return line.equals("INGREDIENTS") ? CYAN : "";
        };
        if (width >= 82) {
            int left = (width - 10) / 3;
            int center = left;
            int right = width - 10 - left - center;
            String divider = "+" + "-".repeat(left + 2) + "+"
                + "-".repeat(center + 2) + "+" + "-".repeat(right + 2) + "+";
            List<String> leftLines = panel(order, left, orderColor);
            List<String> centerLines = panel(table, center, tableColor);
            List<String> rightLines = panel(shelf, right, shelfColor);
            int height = Math.max(leftLines.size(), Math.max(centerLines.size(), rightLines.size()));
            for (int index = 0; index < height; index++) {
                showMessage(edge("| ") + lineAt(leftLines, index, left)
                    + edge(" | ") + lineAt(centerLines, index, center)
                    + edge(" | ") + lineAt(rightLines, index, right) + edge(" |"));
            }
            showMessage(edge(divider));
        }
        else {
            showPanel(order, inner, orderColor);
            showPanel(table, inner, tableColor);
            showPanel(shelf, inner, shelfColor);
        }

        showMessage(edge("| ") + panel(List.of(waitingLine(waiting, inner)), inner,
            line -> CYAN).get(0) + edge(" |"));
        border(width);
        if (!feedback.isBlank()) {
            panel(List.of(feedback), inner, ConsoleView::feedbackColor)
                .forEach(line -> showMessage(edge("| ") + line + edge(" |")));
        }
        wrap(List.of("Number Select  U Undo  X Clear  B Brew  R Recipes  Q Quit"), inner)
            .forEach(line -> row(line, inner));
        border(width);
    }

    private static String waitingLine(List<String> waiting, int limit) {
        String line = "WAITING";
        if (waiting.isEmpty()) {
            return line + "  [none]";
        }
        for (int index = 0; index < waiting.size(); index++) {
            String card = "  [" + (index + 1) + " " + waiting.get(index) + "]";
            int remaining = waiting.size() - index - 1;
            String overflow = remaining > 0 ? "  [+" + remaining + " more]" : "";
            if (line.length() + card.length() + overflow.length() > limit) {
                return line + "  [+" + (remaining + 1) + " more]";
            }
            line += card;
        }
        return line;
    }

    private static List<String> wrap(List<String> lines, int limit) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            while (line.length() > limit) {
                int end = line.lastIndexOf(' ', limit);
                if (end <= 0) {
                    end = limit;
                }
                result.add(line.substring(0, end));
                line = line.substring(end).stripLeading();
            }
            result.add(line);
        }
        return result;
    }

    private List<String> panel(List<String> lines, int columns, Function<String, String> style) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            for (String part : wrap(List.of(line), columns)) {
                result.add(color(part, style.apply(line)) + " ".repeat(columns - part.length()));
            }
        }
        return result;
    }

    private void showPanel(List<String> lines, int columns, Function<String, String> style) {
        panel(lines, columns, style)
            .forEach(line -> showMessage(edge("| ") + line + edge(" |")));
        border(width);
    }

    private static String lineAt(List<String> lines, int index, int columns) {
        return index < lines.size() ? lines.get(index) : " ".repeat(columns);
    }

    private void row(String line, int innerWidth) {
        showMessage(edge("| ") + line + " ".repeat(innerWidth - line.length()) + edge(" |"));
    }

    private void border(int frameWidth) {
        showMessage(edge("+" + "-".repeat(frameWidth - 2) + "+"));
    }

    private String edge(String text) {
        return color(text, MUTED);
    }

    private String color(String text, String style) {
        return colorEnabled && !style.isEmpty() && !text.isEmpty()
            ? "\033[" + style + "m" + text + "\033[0m" : text;
    }

    private static String feedbackColor(String feedback) {
        if (feedback.startsWith("Delivered:")) {
            return GREEN;
        }
        if (feedback.startsWith("Invalid selection.") || feedback.startsWith("The brew was poison.")) {
            return RED;
        }
        if (feedback.startsWith("Ordered:") || feedback.startsWith("The ingredients produced no potion.")) {
            return GOLD;
        }
        return "";
    }

    public void showFeedback(String feedback) {
        showMessage(color(feedback, feedbackColor(feedback)));
    }

    public String describeOutcome(Customer customer, Game.ServiceResult result) {
        String brewed = result.getBrewedPotion();
        switch (result.getOutcome()) {
            case CORRECT:
                return "Delivered: " + brewed + ". "
                    + reaction(customer, CustomerDialogue.Event.CORRECT, brewed);
            case WRONG_POTION:
                return "Ordered: " + customer.getDesiredPotion() + " | Delivered: " + brewed
                    + ". " + reaction(customer, CustomerDialogue.Event.WRONG, brewed);
            case DUD:
                return "The ingredients produced no potion.";
            case POISON:
                return "The brew was poison. " + customer.getName() + " died.";
            default:
                throw new IllegalStateException("Unexpected brewing result");
        }
    }

    private String reaction(Customer customer, CustomerDialogue.Event event, String brewed) {
        return customer.getName() + ": " + dialogue.draw(customer.getType(), event,
            customer.getDesiredPotion(), brewed, dialogueRandom);
    }

    public void showPoisonScene() {
        AsciiArt.printPoisoned(output);
    }


    public void showRecipeBook(Map<String, List<List<String>>> recipes) {
        showMessage("");
        showMessage(color("Recipe book", CYAN));
        showMessage(DIVIDER);
        recipes.forEach((potion, combinations) -> {
            showMessage(color(potion, GOLD));
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

        showMessage(reputation > 0 ? color(success, GREEN) : color(fail, RED));
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

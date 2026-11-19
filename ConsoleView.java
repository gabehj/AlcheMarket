import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class ConsoleView {
    private static final String DIVIDER = "----------------------------------------------------------------";
    private static final String GOLD = "1;33";
    private static final String CYAN = "1;36";
    private static final String GREEN = "1;32";
    private static final String RED = "1;31";
    private static final String PURPLE = "1;35";
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

    public void prompt(String playerName) {
        output.print(color(playerName + "> ", CYAN));
        output.flush();
    }

    public String greetingFor(Customer customer) {
        if (customer instanceof SpecialCustomer) {
            return "Princess Debian needs a Lunar Remedy. I will pay the market price.";
        }
        return dialogue.draw(customer.getType(), CustomerDialogue.Event.GREETING,
            customer.getDesiredPotion(), "", dialogueRandom);
    }

    public void showRook(RookVisit rook, int day, int gold, int reputation,
        boolean story, String feedback) {
        int inner = width - 4;
        border(width);
        int visit = Math.min(rook.getNumber(), 3);
        List<String> header = List.of("SPECIAL  ROOK THE FERRYMAN  /  VISIT " + visit,
            "Day " + day + "  Gold " + gold + "  Reputation " + reputation);
        showPanel(header, inner, line -> line.startsWith("SPECIAL") ? PURPLE : GOLD);
        showPanel(List.of(rookLine(rook, story)), inner, line -> "");
        List<String> table = new ArrayList<>();
        table.add(rook.getNumber() == 4 ? "FERRY CHARTER" : "HIGH CARD");
        if (rook.getNumber() == 4) {
            table.add("Due today: 2 Moonpetal");
            table.add("collect  Take repayment");
        }
        else {
            String yours = rook.hasDrawn() ? cardName(rook.getPlayerCard()) : "?";
            String his = rook.hasDrawn() ? cardName(rook.getRookCard()) : "?";
            table.add("Your card [" + yours + "]    Rook's card [" + his + "]");
            table.add("Stake " + rook.getStake() + "g  |  Win: gold or 1 Moonpetal");
            if (!rook.hasDrawn()) {
                table.add("A Draw cards    D Decline    T Ask about the ferry");
                if (rook.getNumber() == 3) {
                    table.add("loan  Lend 10g today for 2 Moonpetal tomorrow");
                }
            }
            else if (rook.getPlayerCard() > rook.getRookCard()) {
                table.add("gold  Take winnings    herb  Take Moonpetal");
            }
            else {
                table.add("Enter  Continue");
            }
        }
        table.add("Q Quit");
        showPanel(table, inner, line -> line.equals("HIGH CARD") || line.equals("FERRY CHARTER")
            ? PURPLE : "");
        if (!feedback.isBlank()) {
            showPanel(List.of(feedback), inner, ConsoleView::feedbackColor);
        }
    }

    public void showAdvancedOrder(AdvancedOrder order, int day, int gold, int reputation,
        List<String> ingredients, List<String> selected, ToIntFunction<String> stock,
        boolean story, int haggle, String feedback) {
        int inner = width - 4;
        border(width);
        showPanel(List.of((order.isRoyalVisit() ? "SPECIAL  " : "TIER II  ") + order.getName(),
            "Day " + day + "  Gold " + gold + "  Reputation " + reputation), inner, line -> PURPLE);
        int progress = order.getPreparedCount();
        if (order.isRoyalVisit()) {
            String speech;
            if (story) {
                speech = "A royal courier finished his report before collapsing. I expect the same discipline here.";
            }
            else if (progress == 0) {
                speech = "Magister Vale. His Majesty's physician. Which of these vessels was cleaned this morning?";
            }
            else if (progress == 1) {
                speech = "Seal it. We can discuss your workmanship when there are two bottles on the counter.";
            }
            else {
                speech = "Both bottles are present. You may combine them now.";
            }
            showPanel(List.of("Vale: " + speech), inner, line -> "");
        }
        List<String> bench = new ArrayList<>();
        bench.add("ORDER  " + order.getDesiredPotion() + "  [TIER II]");
        for (int index = 0; index < order.getComponents().size(); index++) {
            bench.add((index < progress ? "[READY] " : "[     ] ") + order.getComponents().get(index));
        }
        if (progress < order.getComponents().size()) {
            bench.add("");
            bench.add("Next formula");
            for (String herb : order.getFormulas().get(progress)) {
                int index = ingredients.indexOf(herb) + 1;
                String marker = selected.contains(herb) ? "[x]" : "[ ]";
                bench.add(String.format("%2d. %s %s  x%d", index, marker, herb, stock.applyAsInt(herb)));
            }
            bench.add("Cauldron  " + selected.stream().map(herb -> Integer.toString(ingredients.indexOf(herb) + 1))
                .collect(java.util.stream.Collectors.joining(" + ")));
        }
        else {
            bench.add("Both bottles are ready. Type combine to deliver.");
        }
        if (haggle != 0) {
            bench.add(String.format("Haggling: %+d%% price, %+d rep", haggle, -haggle / 5));
        }
        showPanel(bench, inner, line -> line.startsWith("[READY]") ? GREEN
            : line.startsWith("ORDER") ? PURPLE : "");
        String controls = progress < order.getComponents().size()
            ? "Number Select  B Brew  U Undo  X Clear  R Recipes" : "combine  Deliver";
        showPanel(List.of(controls + "  D Decline  Q Quit"
            + (order.isRoyalVisit() ? "  T Story" : "")), inner, line -> CYAN);
        if (!feedback.isBlank()) {
            showPanel(List.of(feedback), inner, ConsoleView::feedbackColor);
        }
    }

    private static String rookLine(RookVisit rook, boolean story) {
        if (rook.getNumber() == 4) {
            return "Rook: \"Got the charter. Here's your share. You can tell the tollkeeper I said hello.\"";
        }
        if (rook.hasDrawn()) {
            int result = Integer.compare(rook.getPlayerCard(), rook.getRookCard());
            if (result > 0) {
                return "Rook: \"Well, bugger me. Gold or moonpetal? Pick while I'm still feeling honest.\"";
            }
            return result == 0 ? "Rook: \"Same face. Looks like the ropemaker can wait.\""
                : "Rook: \"Higher. Slide it over. I've got a rope to buy.\"";
        }
        if (story) {
            if (rook.getNumber() == 1) {
                return "Rook: \"The flood took my landing. Then some fat bastard paid his fare with "
                    + "a flower. The ropemaker asked if I wanted a rope made of petals.\"";
            }
            if (rook.getNumber() == 2) {
                return "Rook: \"First crossing back, a goose shat on the deck and bit me. "
                    + "Still my best customer all week.\"";
            }
            return "Rook: \"The tollkeeper wants folk to pay twice to cross. I told him to come back "
                + "when he'd learned to swim. I've got till tomorrow to buy the charter myself.\"";
        }
        if (rook.getNumber() == 1) {
            return "Rook: \"Wipe a spot. The flood took my landing, and these cards are still bloody damp.\"";
        }
        if (rook.getNumber() == 3) {
            return "Rook: \"I've got a proper purse today. Or you can help me buy the charter. "
                + "That second one's a deal, even I know the difference.\"";
        }
        switch (rook.getPreviousResult()) {
            case LOST:
                return "Rook: \"See this bit of rope? That's what I cut off the end. "
                    + "Thought you ought to see what your money bought.\"";
            case WON:
                return "Rook: \"Got my rope secondhand. Fellow swore it'd never held more than a wash line. "
                    + "It's got a boat tied to it now.\"";
            case TIED:
                return "Rook: \"No money changed hands last time. Bought the rope on credit. "
                    + "Care to improve my situation?\"";
            default:
                return "Rook: \"You passed last time. Boat's running again all the same. "
                    + "See if the dry cards tempt you.\"";
        }
    }

    private static String cardName(int rank) {
        switch (rank) {
            case 11: return "J";
            case 12: return "Q";
            case 13: return "K";
            case 14: return "A";
            default: return Integer.toString(rank);
        }
    }

    public void showTurn(Customer customer, List<String> ingredients,
        List<String> selected, int reputation, List<String> waiting, String greeting,
        String feedback) {
        showTurn(customer, ingredients, selected, reputation, waiting, greeting, feedback,
            1, 30, 0, List.of(), false, herb -> 0, herb -> 0, offer -> false,
            List.of(), List.of(), 0, 0);
    }

    public void showTurn(Customer customer, List<String> ingredients, List<String> selected,
        int reputation, List<String> waiting, String greeting, String feedback,
        int day, int gold, int dailySales, List<String> offers, boolean rareUnlocked,
        ToIntFunction<String> stock, ToIntFunction<String> price, Predicate<String> needsLockedHerbs,
        List<List<String>> knownRecipes, List<String> rememberedTip, int haggle, int saleQuote) {
        int inner = width - 4;
        boolean wide = width >= 82;
        int left = wide ? (width - 10) / 3 : inner;
        int center = left;
        int right = wide ? width - 10 - left - center : inner;
        String title = "ALCHEMARKET";
        String status = "Day " + day + "  Gold " + gold + "  Reputation " + reputation;
        boolean vendor = customer instanceof HerbVendor;
        boolean dealer = customer instanceof RecipeDealer;
        boolean special = customer instanceof SpecialCustomer;
        border(width);
        if (title.length() + status.length() + 2 <= inner) {
            showMessage(edge("| ") + color(title, GOLD)
                + " ".repeat(inner - title.length() - status.length())
                + color("Day " + day, CYAN) + "  " + color("Gold " + gold, GOLD) + "  "
                + color("Reputation " + reputation, reputation <= 0 ? RED : GREEN) + edge(" |"));
        }
        else {
            showPanel(List.of(title, status), inner, line -> GOLD);
        }
        border(width);

        List<String> order = new ArrayList<>();
        order.add(vendor ? "HERB SUPPLIER" : dealer ? "RECIPE DEALER"
            : special ? "SPECIAL CUSTOMER" : "CURRENT CUSTOMER");
        order.add("");
        order.add(customer.getName() + " / " + customer.getType());
        order.add(vendor ? "Supplies for your shop" : dealer ? "Formulas for your recipe book"
            : "Order   " + customer.getDesiredPotion());
        order.add("Correct orders today: " + dailySales);
        if (vendor && offers.contains(Game.RARE_HERB)) {
            order.add("");
            order.add(rareUnlocked ? "Moonpetal available" : "Moonpetal locked");
            order.add("Requires " + Game.RARE_REPUTATION + " reputation");
            order.add("and " + Game.RARE_DAILY_SALES + " correct orders today.");
        }
        if (greeting != null) {
            order.add("");
            order.add(customer.getName() + ": " + greeting);
        }
        if (!customer.getClue().isEmpty()) {
            order.add("");
            order.add("Recipe clue");
            customer.getClue().forEach(ingredient -> order.add("- " + ingredient));
        }
        if (!vendor && !dealer) {
            order.add("");
            if (!knownRecipes.isEmpty()) {
                order.add("Recipe book: known formula");
                for (String herb : knownRecipes.get(0)) {
                    int number = ingredients.indexOf(herb) + 1;
                    order.add(number > 0 ? number + ". " + herb : "- " + herb);
                }
                if (knownRecipes.size() > 1) {
                    order.add("R: see other formulas");
                }
            }
            else if (!rememberedTip.isEmpty()) {
                order.add("Recipe book: saved tip");
                rememberedTip.forEach(herb -> order.add("- " + herb));
                order.add("One ingredient unknown.");
            }
            else {
                order.add("Recipe book: no entry");
            }
        }

        List<String> table = new ArrayList<>();
        table.add(vendor ? "HERBS FOR SALE" : dealer ? "FORMULAS FOR SALE" : "BREWING TABLE");
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
        if (vendor || dealer) {
            table.subList(2, table.size()).clear();
            if (dealer && offers.isEmpty()) {
                table.add("All formulas learned.");
            }
            else if (dealer && offers.stream().anyMatch(needsLockedHerbs)) {
                table.add("* Requires locked herbs");
                table.add("");
            }
        }

        Function<String, String> orderColor = line -> {
            if (line.startsWith("Moonpetal") || line.startsWith("Requires ") || line.startsWith("and ")) {
                return PURPLE;
            }
            if (line.startsWith("Order   ") || line.equals("SPECIAL CUSTOMER")) {
                return special ? PURPLE : GOLD;
            }
            if (line.equals("CURRENT CUSTOMER") || line.equals("Recipe clue")
                || line.equals("HERB SUPPLIER") || line.equals("RECIPE DEALER")
                || line.startsWith("Recipe book:")) {
                return CYAN;
            }
            return "";
        };
        Function<String, String> tableColor = line -> {
            if (line.startsWith("Ready.") || (!selected.isEmpty()
                && (line.startsWith("Cauldron") || line.startsWith("| ")))) {
                return GREEN;
            }
            if (line.contains(Game.RARE_HERB)) {
                return PURPLE;
            }
            if (line.equals("BREWING TABLE") || line.equals("HERBS FOR SALE")
                || line.equals("FORMULAS FOR SALE")) {
                return CYAN;
            }
            return "";
        };
        List<String> leftLines = panel(order, left, orderColor);
        List<String> centerLines = panel(table, center, tableColor);
        List<String> rightLines = panel(List.of("INGREDIENTS", ""), right, line -> CYAN);
        if (vendor || dealer) {
            for (int index = 0; index < offers.size(); index++) {
                String herb = offers.get(index);
                boolean rare = herb.equals(Game.RARE_HERB);
                String amount = price.applyAsInt(herb) + "g";
                String label = dealer && needsLockedHerbs.test(herb) ? herb + " *" : herb;
                addItemRows(centerLines, center, index + 1, "", label, amount,
                    rare ? PURPLE : "", rare ? PURPLE : GOLD);
            }
        }
        for (int index = 0; index < ingredients.size(); index++) {
            String herb = ingredients.get(index);
            boolean picked = selected.contains(herb);
            int quantity = stock.applyAsInt(herb);
            addItemRows(rightLines, right, index + 1, picked ? "[x] " : "[ ] ",
                herb, "x" + quantity, picked ? GREEN : "", quantity == 0 ? RED : GREEN);
        }
        if (wide) {
            String divider = "+" + "-".repeat(left + 2) + "+"
                + "-".repeat(center + 2) + "+" + "-".repeat(right + 2) + "+";
            int height = Math.max(leftLines.size(), Math.max(centerLines.size(), rightLines.size()));
            for (int index = 0; index < height; index++) {
                showMessage(edge("| ") + lineAt(leftLines, index, left)
                    + edge(" | ") + lineAt(centerLines, index, center)
                    + edge(" | ") + lineAt(rightLines, index, right) + edge(" |"));
            }
            showMessage(edge(divider));
        }
        else {
            for (List<String> section : List.of(leftLines, centerLines, rightLines)) {
                section.forEach(line -> showMessage(edge("| ") + line + edge(" |")));
                border(width);
            }
        }

        showMessage(edge("| ") + waitingLine(waiting, inner) + edge(" |"));
        border(width);
        boolean buying = vendor || dealer;
        String market = buying ? "BUY" : "SELL";
        String standing = reputation >= Game.TRUSTED_REPUTATION
            ? (buying ? " | Trusted: buy 5% less" : " | Trusted: sell 5% more")
            : " | Trusted rates at " + Game.TRUSTED_REPUTATION + " rep";
        String adjustment = haggle == 0 ? "default" : String.format("%+d%% price, %+d rep",
            buying ? -haggle : haggle, -haggle / 5);
        String quote = saleQuote > 0 ? " | Correct delivery: " + saleQuote + "g" : "";
        for (String line : panel(List.of("HAGGLING / " + market + "  " + adjustment + standing + quote,
            "haggle 5/15/20/50 for a better deal  |  negative to give ground  |  0 reset"),
            inner, line -> line.startsWith("HAGGLING") ? PURPLE : "")) {
            showMessage(edge("| ") + line + edge(" |"));
        }
        border(width);
        if (!feedback.isBlank()) {
            panel(List.of(feedback), inner, ConsoleView::feedbackColor)
                .forEach(line -> showMessage(edge("| ") + line + edge(" |")));
        }
        String controls = vendor ? "buy <offer> <quantity>  D Move on  Q Quit"
            : dealer ? "buy <recipe>  D Move on  Q Quit"
            : "Number Select  B Brew  A Accept  D Decline  U Undo  X Clear  R Recipes  Q Quit";
        wrap(List.of(controls), inner)
            .forEach(line -> row(line, inner));
        border(width);
    }

    private void addItemRows(List<String> rows, int columns, int index, String marker,
        String name, String quantity, String nameColor, String quantityColor) {
        String number = String.format("%2d. ", index);
        int indent = number.length() + marker.length();
        int nameWidth = columns - indent - quantity.length() - 2;
        List<String> parts = wrap(List.of(name), nameWidth);
        for (int line = 0; line < parts.size(); line++) {
            String part = parts.get(line);
            boolean first = line == 0;
            String prefix = first ? color(number, CYAN) + color(marker, nameColor)
                : " ".repeat(indent);
            String value = first ? color(quantity, quantityColor) : " ".repeat(quantity.length());
            rows.add(prefix + color(part, nameColor)
                + " ".repeat(nameWidth - part.length() + 2) + value);
        }
    }

    private String waitingLine(List<String> waiting, int limit) {
        StringBuilder line = new StringBuilder(edge("WAITING"));
        int used = "WAITING".length();
        if (waiting.isEmpty()) {
            return line + edge("  [none]") + " ".repeat(limit - used - "  [none]".length());
        }
        for (int index = 0; index < waiting.size(); index++) {
            String name = waiting.get(index);
            String card = "[" + (index + 1) + " " + name + "]";
            int remaining = waiting.size() - index - 1;
            String overflow = remaining > 0 ? "  [+" + remaining + " more]" : "";
            if (used + 2 + card.length() + overflow.length() > limit) {
                String hidden = "  [+" + (remaining + 1) + " more]";
                line.append(edge(hidden));
                used += hidden.length();
                break;
            }
            String style = CYAN;
            if (name.startsWith("SPECIAL ")) {
                style = PURPLE;
            }
            else if (name.startsWith("Herbalist ") || name.equals("Recipe Dealer")) {
                style = GOLD;
            }
            line.append("  ").append(color(card, style));
            used += 2 + card.length();
        }
        return line + " ".repeat(limit - used);
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
        if (feedback.startsWith("Delivered:") || feedback.startsWith("Purchased ")) {
            return GREEN;
        }
        if (feedback.startsWith("Invalid selection.") || feedback.startsWith("The brew was poison.")) {
            return RED;
        }
        if (feedback.startsWith("Ordered:") || feedback.startsWith("The ingredients produced no potion.")) {
            return GOLD;
        }
        if (feedback.equals("Visitor moved on.")) {
            return PURPLE;
        }
        if (feedback.startsWith("Added ") || feedback.startsWith("Order accepted.")
            || feedback.equals("Trade terms updated.")
            || feedback.startsWith("Cauldron ") || feedback.startsWith("Last ingredient")) {
            return CYAN;
        }
        if (feedback.equals("The cauldron is already empty.")) {
            return GOLD;
        }
        return "";
    }

    public void showFeedback(String feedback) {
        showMessage(color(feedback, feedbackColor(feedback)));
    }

    public void showDailySummary(int day, int gold, int sales, int netGold, int reputation) {
        showMessage(color("DAY " + day + " CLOSED", GOLD));
        showMessage("Correct orders: " + sales + "  |  Net gold: " + netGold
            + "  |  Balance: " + gold + "  |  Reputation: " + reputation);
        showMessage("Press Enter for the next day, or Q to quit.");
    }

    public String describeOutcome(Customer customer, Game.ServiceResult result) {
        String brewed = result.getBrewedPotion();
        switch (result.getOutcome()) {
            case CORRECT:
                return "Delivered: " + brewed + ". +" + result.getGoldEarned() + " gold."
                    + (customer instanceof AdvancedOrder && ((AdvancedOrder) customer).isRoyalVisit()
                        ? " Vale: Acceptable. Put your name on the invoice."
                        : customer instanceof SpecialCustomer ? ""
                        : " " + reaction(customer, CustomerDialogue.Event.CORRECT, brewed));
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


    public void showRecipeBook(Map<String, List<List<String>>> recipes, Map<String, List<String>> tips,
        Map<String, List<String>> advanced) {
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
        showMessage(color("Customer tips", CYAN));
        new java.util.TreeMap<>(tips).forEach((potion, ingredients) ->
            showMessage(potion + ": " + String.join(", ", ingredients) + " + unknown ingredient"));
        if (!advanced.isEmpty()) {
            showMessage(color("Tier II formulas", PURPLE));
            new java.util.TreeMap<>(advanced).forEach((potion, components) ->
                showMessage(potion + " = " + String.join(" + ", components)));
        }
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

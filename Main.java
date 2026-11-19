/**
 * // -------------------------------------------------------------------------
/**
 *  Main class that creates and runs a new game?
 * 
 *  @author Sarah Beatty
 *  @version Nov 11, 2026
 */

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Long seed = null;
        if (args.length != 0) {
            if (args.length != 2 || !args[0].equals("--seed")) {
                System.err.println("Usage: java Main [--seed <integer>]");
                return;
            }
            try {
                seed = Long.parseLong(args[1]);
            }
            catch (NumberFormatException error) {
                System.err.println("Seed must be a whole number.");
                return;
            }
        }

        AsciiArt.printMain();
        
        ConsoleView view = new ConsoleView(new Scanner(System.in), System.out);
        view.showMessage("Opening AlcheMarket...");
        view.showMessage("Enter your name:");
        String name = view.readLine();
        while (name != null && name.isBlank()) {
            view.showMessage("Invalid name. Enter your name:");
            name = view.readLine();
        }
        if (name != null) {
            Game game = seed == null ? new Game(name.strip()) : new Game(name.strip(), seed);
            new Validation(game, view).run();
        }
    }
}

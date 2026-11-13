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
            new Validation(new Game(name.strip()), view).run();
        }
    }
}

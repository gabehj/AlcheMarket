/**
 * // -------------------------------------------------------------------------
/**
 *  Main class that creates and runs a new game?
 * 
 *  @author Sarah Beatty
 *  @version Nov 11, 2026
 */
public class Main {
    
    private String name;
    private Game newGame;
    private Double score;
    
    
    public static void main(String[] args) {
        newGame = new Game(name, score);
        newGame.play();
    }
}

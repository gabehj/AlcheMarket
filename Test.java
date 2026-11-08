import java.util.ArrayList;

public class Test {
    public static void main(String[] args) {
        
        testGenerateRecipeCombos();
    
    }
    
    
    public static void testGenerateRecipeCombos() {
        Game game = new Game();
        int combos = 1;
        for (int i = 0; i < game.getNumIngredients(); i++) {
            combos *= (game.getIngredientNames().size() - i); // Calc permutations
            combos /= (i+1); // Calc combinations
        }
        System.out.println("Combos is correct length: "+ (game.getCombos().size()) + " = " + combos);
    }
}
import java.util.ArrayList;

public class Test {

    private static Game game = new Game();
    public static void main(String[] args) {

        testGenerateRecipeCombos();

        testGeneratePotions();
    
    }
    
    
    public static void testGenerateRecipeCombos() {
        int combos = 1;
        for (int i = 0; i < game.getNumIngredients(); i++) {
            combos *= (game.getIngredientNames().size() - i); // Calc permutations
            combos /= (i+1); // Calc combinations
        }
        System.out.println("Number of ingredients per potion: " + game.getNumIngredients());
        System.out.println("Number of ingredient names: " + game.getIngredientNames().size());
        System.out.println("Combos is correct length: "+ (game.getCombos().size()) + " = " + combos);
    }

    public static void testGeneratePotions() {
        System.out.println("Potions: \n" + game.getPotions());
    }
}
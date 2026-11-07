import java.util.ArrayList;
import java.util.Queue;
import java.util.List;

public class Game {
    
    double recipe_coverage = 0.75; // Fraction of recipes that don't result in duds

    private int num_ingredients = 3;  // Number of ingredients for every recipe
        
    private ArrayList<ArrayList<String>> combos = new ArrayList<>();

    private ArrayList<String> ingredientNames;
    private ArrayList<String> potionNames;


    public Game() {

        ingredientNames = new ArrayList<String>(List.of(
            "Mandrake Root", 
            "Unicorn Hair", 
            "Dragon Scale", 
            "Phoenix Feather", 
            "Wolfsbane")
        );
        potionNames = new ArrayList<String>(List.of(
            "Healing",
            "Invisibility", 
            "Strength", 
            "Speed", 
            "Luck",
            "Poison: Mamba Venom",
            "Poison: Troll Dung")
        );
        
        generateRecipeCombos(0, new ArrayList<String>());
    }

    public void generateRecipeCombos(int last_index, ArrayList<String> combo) {
        
        if (combo.size() == num_ingredients) combos.add(combo);
        
        else {
            for (int i=last_index; i < ingredientNames.size() - num_ingredients - combo.size() + 1; i++) {
                ArrayList<String> newCombo = new ArrayList<>(combo);
                newCombo.add(ingredientNames.get(i));
                generateRecipeCombos(i, newCombo);
            }
        }
    }

    public int getNumIngredients() {
        return num_ingredients;
    }

    public ArrayList<ArrayList<String>> getCombos() {
        return combos;
    }
}
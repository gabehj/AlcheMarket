import java.util.ArrayList;
import java.util.Queue;
import java.util.List;

public class Game {
    
    public Game() {
        ArrayList<String> ingredientNames = new ArrayList<String>(List.of(
            "Mandrake Root", 
            "Unicorn Hair", 
            "Dragon Scale", 
            "Phoenix Feather", 
            "Wolfsbane")
        );
        ArrayList<String> potionNames = new ArrayList<String>(List.of(
            "Healing",
            "Invisibility", 
            "Strength", 
            "Speed", 
            "Luck",
            "Poison: Mamba Venom",
            "Poison: Troll Dung")
        );
        
        int num_ingredients = 3;  // Number of ingredients for every recipe
        double recipe_coverage = 0.75; // Percent of recipes that don't result in duds
        
        int combinations = 1;
        
        for (int i=0; i < num_ingredients; i++) {
            combinations *= (ingredientNames.size() - i);
        }

        


        System.out.println("Total combinations: " + combinations);
        for (String potionName : potionNames) {
            int num_recipes = (int)(combinations * recipe_coverage / potionNames.size());
            
            ArrayList<String> recipe;
            for (int j=0; j < num_recipes; j++) {
                recipe = new ArrayList<>();
                for (int k)

            }
        }
    }


}

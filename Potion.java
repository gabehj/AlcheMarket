import java.util.ArrayList;

public class Potion {
    private String name;
    private ArrayList<ArrayList<String>> recipes;

    public Potion(String name, ArrayList<ArrayList<String>> recipes) {
        this.name = name;
        this.recipes = recipes;
    }

    public String getName() {
        return name;
    }

    public ArrayList<ArrayList<String>> getRecipes() {
        return recipes;
    }

    public boolean checkRecipe(ArrayList<String> ingredients) {
        boolean match = false;
        for (ArrayList<String> recipe: recipes) {
            if (recipe.equals(ingredients)) match = true;
        }
        return match;                
    }
}

import java.util.ArrayList;

public class Potion {
    private String name;
    private ArrayList<ArrayList<String>> recipes;
    private boolean poison;

    // CONSTRUCTORS

    public Potion(String name) {
        this.name = name;
        this.recipes = new ArrayList<>();
        this.poison = false;
    }

    public Potion(String name, boolean poison) {
        this.name = name;
        this.recipes = new ArrayList<>();
        this.poison = poison;
    }
    
    public Potion(String name, ArrayList<ArrayList<String>> recipes) {
        this.name = name;
        this.recipes = recipes;
        this.poison = false;
    }

    public Potion(String name, ArrayList<ArrayList<String>> recipes, boolean poison) {
        this.name = name;
        this.recipes = recipes;
        this.poison = poison;
    }

    // METHODS

    public void addRecipe(ArrayList<String> recipe) {
        recipes.add(recipe);
    }
    
    public boolean checkRecipe(ArrayList<String> ingredients) {
        boolean match = false;
        for (ArrayList<String> recipe: recipes) {
            if (recipe.equals(ingredients)) match = true;
        }
        return match;                
    }

    public String toString() {
        String m = "Name: " + getName();
        m += "\nisPoison: " + isPoison();
        for (ArrayList<String> recipe: getRecipes()) {
            m += "\nRecipe: " + recipe;
        }
        return m + "\n";
    }

    public boolean equals(Potion other) {
        return (getName().equals(other.getName()));
    }

    // GETTERS

    public boolean isPoison() {
        return poison;
    }

    public String getName() {
        return name;
    }

    public ArrayList<ArrayList<String>> getRecipes() {
        return recipes;
    }
}

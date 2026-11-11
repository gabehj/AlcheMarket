import java.util.HashMap;
import java.util.ArrayList;
public class Player {
    private String name;
    private int reputation = 0;
    private HashMap<String, ArrayList<ArrayList<String>>> savedRecipes = new HashMap<>();

    Player(String name) {
        this.name = name;
        this.reputation = 0;
    }

    // Resets recipe knowledge of player at start of new game
    public  void forgetRecipes() {
        savedRecipes = new HashMap<>();
    }

    // Adds recipe to player's history
    public void saveRecipe(String potionName, ArrayList<String> recipe) {
        // Get old recipes for given potion
        ArrayList<ArrayList<String>> oldRecipes = savedRecipes.get(potionName);
        if (oldRecipes == null) oldRecipes = new ArrayList<>();

        // Add new recipe and update saved recipes
        oldRecipes.add(recipe);
        savedRecipes.put(potionName, oldRecipes);
    }

    // GETTERS

    public String getName() {
        return name;
    }
    
    public int getReputation() {
        return reputation;
    }

    // SETTERS

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public void increaseReputation(int amount) {
        this.reputation += amount;
    }

    public void decreaseReputation(int amount) {
        this.reputation -= amount;
    }
}

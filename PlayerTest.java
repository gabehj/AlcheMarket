import static org.junit.Assert.*;
 
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class PlayerTest {
        private Player player;
 
    @Before
    public void setUp() {
        player = new Player("Tester");
    }
 
    private static ArrayList<String> recipe(String... ingredients) {
        return new ArrayList<>(List.of(ingredients));
    }
 
    @Test
    public void constructorSetsNameZeroReputationAndNoRecipes() {
        assertEquals("Tester", player.getName());
        assertEquals(0, player.getReputation());
        assertTrue(player.getSavedRecipes().isEmpty());
    }
 
    @Test
    public void setReputationReplacesValue() {
        player.setReputation(50);
        assertEquals(50, player.getReputation());
        player.setReputation(-5);
        assertEquals(-5, player.getReputation());
    }
 
    @Test
    public void increaseReputationAddsAmount() {
        player.setReputation(50);
        player.increaseReputation(15);
        assertEquals(65, player.getReputation());
    }
 
    @Test
    public void decreaseReputationSubtractsAmountAndCanGoNegative() {
        player.setReputation(10);
        player.decreaseReputation(3);
        assertEquals(7, player.getReputation());
        player.decreaseReputation(50);
        assertEquals(-43, player.getReputation());
    }
 
    @Test
    public void saveRecipeStoresNewRecipeUnderPotion() {
        ArrayList<String> r = recipe("A", "B", "C");
        player.saveRecipe("Healing", r);
 
        assertEquals(1, player.getSavedRecipes().size());
        assertEquals(1, player.getSavedRecipes().get("Healing").size());
        assertEquals(r, player.getSavedRecipes().get("Healing").get(0));
    }
 
    @Test
    public void saveRecipeDoesNotAddDuplicate() {
        player.saveRecipe("Healing", recipe("A", "B", "C"));
        player.saveRecipe("Healing", recipe("A", "B", "C"));
 
        assertEquals(1, player.getSavedRecipes().get("Healing").size());
    }
 
    @Test
    public void saveRecipeKeepsMultipleDifferentRecipesForSamePotion() {
        player.saveRecipe("Healing", recipe("A", "B", "C"));
        player.saveRecipe("Healing", recipe("A", "B", "D"));
 
        assertEquals(1, player.getSavedRecipes().size());
        assertEquals(2, player.getSavedRecipes().get("Healing").size());
    }
 
    @Test
    public void saveRecipeTracksDifferentPotionsSeparately() {
        player.saveRecipe("Healing", recipe("A", "B", "C"));
        player.saveRecipe("Invisibility", recipe("A", "B", "C"));
 
        assertEquals(2, player.getSavedRecipes().size());
        assertTrue(player.getSavedRecipes().containsKey("Healing"));
        assertTrue(player.getSavedRecipes().containsKey("Invisibility"));
    }
 
    @Test
    public void ingredientOrderMattersForDuplicateCheck() {
        player.saveRecipe("Healing", recipe("A", "B", "C"));
        player.saveRecipe("Healing", recipe("C", "B", "A"));
 
        assertEquals(2, player.getSavedRecipes().get("Healing").size());
    }
 
    @Test
    public void forgetRecipesClearsSavedRecipes() {
        player.saveRecipe("Healing", recipe("A", "B", "C"));
        player.forgetRecipes();
 
        assertTrue(player.getSavedRecipes().isEmpty());
    }
 
    @Test
    public void forgetRecipesDoesNotChangeReputation() {
        player.setReputation(42);
        player.saveRecipe("Healing", recipe("A", "B", "C"));
        player.forgetRecipes();
 
        assertEquals(42, player.getReputation());
    }
 
    @Test
    public void saveRecipeWorksAgainAfterForgetting() {
        player.saveRecipe("Healing", recipe("A", "B", "C"));
        player.forgetRecipes();
        player.saveRecipe("Healing", recipe("A", "B", "C"));
 
        assertEquals(1, player.getSavedRecipes().get("Healing").size());
    }
}

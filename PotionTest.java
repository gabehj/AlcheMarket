import static org.junit.Assert.*;
 
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class PotionTest {
     private static ArrayList<String> recipe(String... ingredients) {
        return new ArrayList<>(List.of(ingredients));
    }
 
    @Test
    public void nameOnlyConstructorIsNonPoisonWithNoRecipes() {
        Potion potion = new Potion("Healing");
 
        assertEquals("Healing", potion.getName());
        assertFalse(potion.isPoison());
        assertTrue(potion.getRecipes().isEmpty());
    }
 
    @Test
    public void poisonConstructorSetsPoisonFlag() {
        Potion poison = new Potion("Poison: Throat-Melter", true);
        Potion notPoison = new Potion("Healing", false);
 
        assertTrue(poison.isPoison());
        assertFalse(notPoison.isPoison());
        assertTrue(poison.getRecipes().isEmpty());
    }
 
    @Test
    public void recipesConstructorStoresRecipesAndIsNotPoison() {
        ArrayList<ArrayList<String>> recipes = new ArrayList<>();
        recipes.add(recipe("A", "B", "C"));
        Potion potion = new Potion("Healing", recipes);
 
        assertEquals("Healing", potion.getName());
        assertFalse(potion.isPoison());
        assertEquals(1, potion.getRecipes().size());
        assertEquals(recipe("A", "B", "C"), potion.getRecipes().get(0));
    }
 
    @Test
    public void fullConstructorStoresRecipesAndPoisonFlag() {
        ArrayList<ArrayList<String>> recipes = new ArrayList<>();
        recipes.add(recipe("A", "B", "C"));
        Potion potion = new Potion("Poison: Gamma Explosion", recipes, true);
 
        assertEquals("Poison: Gamma Explosion", potion.getName());
        assertTrue(potion.isPoison());
        assertEquals(1, potion.getRecipes().size());
    }
 
    @Test
    public void addRecipeAppendsToRecipes() {
        Potion potion = new Potion("Healing");
        potion.addRecipe(recipe("A", "B", "C"));
        potion.addRecipe(recipe("A", "B", "D"));
 
        assertEquals(2, potion.getRecipes().size());
        assertEquals(recipe("A", "B", "D"), potion.getRecipes().get(1));
    }
 
    @Test
    public void checkRecipeFindsAddedRecipe() {
        Potion potion = new Potion("Healing");
        potion.addRecipe(recipe("A", "B", "C"));
 
        assertTrue(potion.checkRecipe(recipe("A", "B", "C")));
    }
 
    @Test
    public void checkRecipeRejectsUnknownRecipe() {
        Potion potion = new Potion("Healing");
        potion.addRecipe(recipe("A", "B", "C"));
 
        assertFalse(potion.checkRecipe(recipe("A", "B", "D")));
        assertFalse(potion.checkRecipe(recipe("A", "B")));
    }
 
    @Test
    public void checkRecipeIsFalseWhenPotionHasNoRecipes() {
        assertFalse(new Potion("Healing").checkRecipe(recipe("A", "B", "C")));
    }
 
    @Test
    public void checkRecipeMatchesAnyOfSeveralRecipes() {
        Potion potion = new Potion("Healing");
        potion.addRecipe(recipe("A", "B", "C"));
        potion.addRecipe(recipe("D", "E", "F"));
 
        assertTrue(potion.checkRecipe(recipe("D", "E", "F")));
    }
 
    @Test
    public void checkRecipeIsOrderSensitive() {
        Potion potion = new Potion("Healing");
        potion.addRecipe(recipe("A", "B", "C"));
 
        // Game sorts recipes before lookup, so order is normalized there.
        assertFalse(potion.checkRecipe(recipe("C", "B", "A")));
    }
 
    @Test
    public void toStringShowsNamePoisonFlagAndRecipes() {
        Potion potion = new Potion("Healing");
        potion.addRecipe(recipe("A", "B", "C"));
 
        assertEquals("Name: Healing\nisPoison: false\nRecipe: [A, B, C]\n", potion.toString());
    }
 
    @Test
    public void toStringWithNoRecipesHasNoRecipeLines() {
        assertEquals("Name: Healing\nisPoison: false\n", new Potion("Healing").toString());
    }
 
    @Test
    public void equalsPotionComparesByNameOnly() {
        Potion a = new Potion("Healing");
        Potion b = new Potion("Healing", true);
        Potion c = new Potion("Invisibility");
 
        assertTrue(a.equals(b));
        assertFalse(a.equals(c));
    }
 
    @Test
    public void equalsObjectStillUsesIdentity() {
        Object a = new Potion("Healing");
        Object b = new Potion("Healing");
 
        assertNotEquals(a, b);
    }
}

import static org.junit.Assert.*;
 
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
 
public class CustomerTest {
    @Test
    public void gettersReturnConstructorValues() {
        Customer customer = new NormalCustomer("Villager", CustomerType.TOWNSPERSON,
            "Healing", List.of("Mandrake Root", "Nightshade"));
 
        assertEquals("Villager", customer.getName());
        assertEquals(CustomerType.TOWNSPERSON, customer.getType());
        assertEquals("Healing", customer.getDesiredPotion());
        assertEquals(List.of("Mandrake Root", "Nightshade"), customer.getClue());
    }
 
    @Test
    public void normalCustomerIsACustomer() {
        Object customer = new NormalCustomer("Priest", CustomerType.ELF, "Invisibility", List.of());
 
        assertTrue(customer instanceof Customer);
    }
 
    @Test
    public void everyCustomerTypeCanBeUsed() {
        for (CustomerType type : CustomerType.values()) {
            Customer customer = new NormalCustomer("Test", type, "Healing", List.of());
            assertEquals(type, customer.getType());
        }
    }
 
    @Test
    public void clueIsDefensivelyCopied() {
        List<String> original = new ArrayList<>(List.of("Mandrake Root", "Nightshade"));
        Customer customer = new NormalCustomer("Villager", CustomerType.TOWNSPERSON,
            "Healing", original);
 
        original.add("Wolfsbane");
        original.set(0, "Changed");
 
        assertEquals(List.of("Mandrake Root", "Nightshade"), customer.getClue());
    }
 
    @Test
    public void clueCannotBeModifiedThroughGetter() {
        Customer customer = new NormalCustomer("Villager", CustomerType.TOWNSPERSON,
            "Healing", List.of("Mandrake Root", "Nightshade"));
 
        assertThrows(UnsupportedOperationException.class,
            () -> customer.getClue().add("Wolfsbane"));
        assertThrows(UnsupportedOperationException.class,
            () -> customer.getClue().remove(0));
    }
 
    @Test
    public void emptyClueIsAllowed() {
        Customer customer = new NormalCustomer("Soldier", CustomerType.DWARF,
            "Adrenaline", List.of());
 
        assertTrue(customer.getClue().isEmpty());
    }
 
    @Test
    public void nullClueIsRejected() {
        assertThrows(NullPointerException.class, () -> new NormalCustomer("Soldier",
            CustomerType.DWARF, "Adrenaline", null));
    }
}

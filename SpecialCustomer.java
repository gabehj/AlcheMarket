import java.util.List;

public class SpecialCustomer extends Customer {
    public SpecialCustomer(String name, String potion, List<String> clue) {
        super(name, CustomerType.TOWNSPERSON, potion, clue);
    }
}

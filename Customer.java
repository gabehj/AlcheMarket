import java.util.List;

public class Customer {
    private final String name;
    private final String desiredPotion;
    private final List<String> clue;

    public Customer(String name, String desiredPotion, List<String> clue) {
        this.name = name;
        this.desiredPotion = desiredPotion;
        this.clue = List.copyOf(clue);
    }

    public String getName() {
        return name;
    }

    public String getDesiredPotion() {
        return desiredPotion;
    }

    public List<String> getClue() {
        return clue;
    }
}


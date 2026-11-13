import java.util.List;

public abstract class Customer {
    private final String name;
    private final CustomerType type;
    private final String desiredPotion;
    private final List<String> clue;

    protected Customer(String name, CustomerType type, String desiredPotion, List<String> clue) {
        this.name = name;
        this.type = type;
        this.desiredPotion = desiredPotion;
        this.clue = List.copyOf(clue);
    }

    public String getName() {
        return name;
    }

    public CustomerType getType() {
        return type;
    }

    public String getDesiredPotion() {
        return desiredPotion;
    }

    public List<String> getClue() {
        return clue;
    }
}


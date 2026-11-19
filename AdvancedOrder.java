import java.util.ArrayList;
import java.util.List;

public final class AdvancedOrder extends Customer {
    private final boolean royalVisit;
    private final List<String> components;
    private final List<List<String>> formulas;
    private final List<Integer> componentCosts = new ArrayList<>();

    AdvancedOrder(String name, CustomerType type, boolean royalVisit, String potion,
        List<String> components, List<List<String>> formulas) {
        super(name, type, potion, List.of());
        this.royalVisit = royalVisit;
        this.components = List.copyOf(components);
        this.formulas = List.copyOf(formulas);
    }

    public boolean isRoyalVisit() {
        return royalVisit;
    }

    public List<String> getComponents() {
        return components;
    }

    public List<List<String>> getFormulas() {
        return formulas;
    }

    public int getPreparedCount() {
        return componentCosts.size();
    }

    void prepare(int cost) {
        componentCosts.add(cost);
    }

    int ingredientCost() {
        return componentCosts.stream().mapToInt(Integer::intValue).sum();
    }
}

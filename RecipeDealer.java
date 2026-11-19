import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RecipeDealer extends Customer {
    private Map<String, List<String>> offers = Map.of();

    public RecipeDealer() {
        super("Recipe Dealer", CustomerType.TOWNSPERSON, "", List.of());
    }

    void setOffers(Map<String, List<String>> offers) {
        this.offers = java.util.Collections.unmodifiableMap(new TreeMap<>(offers));
    }

    public Map<String, List<String>> getOffers() {
        return offers;
    }
}

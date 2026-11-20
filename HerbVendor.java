import java.util.List;

public class HerbVendor extends Customer {
    private final List<String> offers;

    public HerbVendor(String name, List<String> offers) {
        super(name, CustomerType.TOWNSPERSON, "", List.of());
        this.offers = List.copyOf(offers);
    }

    public List<String> getOffers() {
        return offers;
    }
}

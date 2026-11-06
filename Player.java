public class Player {
    private String name;
    private int reputation = 0;

    Player(String name) {
        this.name = name;
        this.reputation = 0;
    }

    public String getName() {
        return name;
    }
    
    public int getReputation() {
        return reputation;
    }

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

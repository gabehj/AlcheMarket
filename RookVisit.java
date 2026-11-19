import java.util.List;

public final class RookVisit extends Customer {
    public enum Result {
        NONE, WON, LOST, TIED, DECLINED, LOANED
    }

    private final int number;
    private final Result previousResult;
    private int playerCard;
    private int rookCard;

    RookVisit(int number, Result previousResult) {
        super("Rook the Ferryman", CustomerType.TOWNSPERSON, "", List.of());
        this.number = number;
        this.previousResult = previousResult;
    }

    public int getNumber() {
        return number;
    }

    public Result getPreviousResult() {
        return previousResult;
    }

    public int getStake() {
        return number == 3 ? 10 : 5;
    }

    public boolean hasDrawn() {
        return playerCard != 0;
    }

    public int getPlayerCard() {
        return playerCard;
    }

    public int getRookCard() {
        return rookCard;
    }

    void revealCards(int playerCard, int rookCard) {
        this.playerCard = playerCard;
        this.rookCard = rookCard;
    }
}

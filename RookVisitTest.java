import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.Test;

public class RookVisitTest {
    @Test
    public void lossChargesOnceAndCarriesIntoTheNextVisit() {
        Game game = new Game("Tester", 0L);
        assertThrows(IllegalStateException.class, game::drawWithRook);
        RookVisit first = nextVisit(game);
        assertThrows(IllegalStateException.class, () -> game.finishRookRound(false));
        assertThrows(IllegalStateException.class, game::lendToRook);
        game.drawWithRook();
        assertTrue(first.getPlayerCard() < first.getRookCard());
        assertEquals(25, game.getGold());
        assertThrows(IllegalStateException.class, game::drawWithRook);
        assertThrows(IllegalArgumentException.class, () -> game.finishRookRound(true));
        game.finishRookRound(false);
        assertEquals(25, game.getGold());
        assertEquals(RookVisit.Result.LOST, nextVisit(game).getPreviousResult());
    }

    @Test
    public void winnerCanClaimHerbAndTieDoesNotCharge() {
        Game winner = new Game("Tester", 5L);
        RookVisit winningVisit = nextVisit(winner);
        winner.drawWithRook();
        assertTrue(winningVisit.getPlayerCard() > winningVisit.getRookCard());
        winner.finishRookRound(true);
        assertEquals(30, winner.getGold());
        assertEquals(1, winner.stockOf(Game.RARE_HERB));
        assertThrows(java.util.NoSuchElementException.class, () -> winner.finishRookRound(true));

        Game tied = new Game("Tester", 8L);
        RookVisit tiedVisit = nextVisit(tied);
        tied.drawWithRook();
        assertEquals(tiedVisit.getPlayerCard(), tiedVisit.getRookCard());
        tied.finishRookRound(false);
        assertEquals(30, tied.getGold());
        assertEquals(RookVisit.Result.TIED, nextVisit(tied).getPreviousResult());
    }

    @Test
    public void controllerShowsOptionalStoryAndLetsRookRepayTheLoan() {
        Game game = new Game("Tester", 0L);
        nextVisit(game);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Validation controller = new Validation(game, new ConsoleView(new Scanner(""),
            new PrintStream(output, true, StandardCharsets.UTF_8)));
        assertTrue(controller.processLine("t"));
        String frame = output.toString(StandardCharsets.UTF_8);
        assertTrue(frame.contains("rope made of petals"));
        assertTrue(frame.contains("Stake 5g"));
        assertFalse(frame.contains("Wipe a spot"));
        assertTrue(controller.processLine("d"));
        assertEquals(RookVisit.Result.DECLINED, nextVisit(game).getPreviousResult());
        assertTrue(controller.processLine("d"));
        assertEquals(3, nextVisit(game).getNumber());
        assertTrue(controller.processLine("loan"));
        assertEquals(20, game.getGold());
        assertEquals(4, nextVisit(game).getNumber());
        assertTrue(controller.processLine("collect"));
        assertEquals(2, game.stockOf(Game.RARE_HERB));
        assertTrue(game.isDayClosed());
        assertThrows(java.util.NoSuchElementException.class, game::collectRookRepayment);
    }

    private RookVisit nextVisit(Game game) {
        assertTrue(game.getDay() < 5);
        while (!game.isDayClosed()) {
            game.declineCustomer();
        }
        game.startNextDay();
        while (!game.isDayClosed() && !(game.currentCustomer() instanceof RookVisit)) {
            game.declineCustomer();
        }
        assertFalse("Rook should visit today", game.isDayClosed());
        return (RookVisit) game.currentCustomer();
    }
}

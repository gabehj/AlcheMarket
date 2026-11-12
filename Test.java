public class Test {
    public static void main(String[] args) {
        Game game = new Game("Paracelsus", 15L);
        Customer first = game.currentCustomer();
        Game.Outcome outcome = game.serve(game.starterRecipe());
        if (outcome != Game.Outcome.CORRECT || game.getReputation() != 65
            || game.currentCustomer() == first) {
            throw new AssertionError("Serving the head customer did not advance the game.");
        }
        System.out.println("Served " + first.getName() + "; reputation is " + game.getReputation());
        System.out.println("Next customer: " + game.currentCustomer().getName());
    }
}

# AlcheMarket

| Person | Task                                                                                       | GenAI |
| ------ | ------------------------------------------------------------------------------------------ | ----- |
| Gabe   | Programmed Game Class, Potion Class, and Player Class. Made algorithm for potion mappings. | None  |
|        |                                                                                            |       |

## Model Works

`Game` holds the recipes, reputation, and customer queue. It uses a singly linked `LinkedQueue<Customer>` to serve customers in arrival order. The model accepts ingredient selections and returns a service result without reading from or writing to the console.

- **Run the model by itself.** Open `Test.java` and run its `main` method. In VS Code, click **Run** above `main`. In Eclipse, choose **Run As → Java Application**. The driver serves the first customer, checks that reputation rises from 50 to 65, and prints the next customer. It requires no keyboard input or dialogue file.
- **Run the JUnit 4 tests.** Open the `AlcheMarket` project folder in VS Code and select **Testing → Run All Tests**, or import it as an existing Eclipse project and choose **Run As → JUnit Test**. `LinkedQueueTest` checks FIFO order, `peek` without removal, empty-queue behavior, rejection of null entries, and reuse after the queue is drained. `PrototypeTest` also serves all three customers directly through the model.

## Thin View

Run `Main.java` as a Java application in the VS Code integrated terminal or the Eclipse Console. Keep `customer-dialogue.tsv` in the project root so the console can load it as a resource.

Enter a player name, read the order and recipe clue, then enter one ingredient number at a time. Each selection updates the three cauldron slots. Use `undo` to remove the last selection, `reset` to clear the cauldron, and `recipes` to open the recipe book. Once all three slots are full, enter `brew` to brew and serve the potion. Enter `quit` to leave the game.

`Main` connects `ConsoleView`, the `Validation` controller, and `Game`. `Validation` turns commands into model calls and passes the results to `ConsoleView`, which displays the order, dialogue, and score.

## Runs Without Crashing

The prototype can run from the first order through the final score. To try a complete round, enter `1`, `2`, and `3` on separate lines and then enter `brew`. Repeat for the next two customers. The first order is guaranteed to match this recipe. Later orders may require a different combination, so use their clues if you want to serve the requested potion.

The JUnit tests cover blank and unknown commands, missing selections, duplicate selections, misspelled or locked ingredients, negative and out-of-range indexes, and oversized or nonnumeric input. Invalid selections leave the current customer and reputation unchanged. Tests also verify that the model rejects service after the queue is empty and that `undo` and `reset` work during ingredient selection. The current suite has eight tests across `LinkedQueueTest`, `PrototypeTest`, and `CustomerDialogueTest`.

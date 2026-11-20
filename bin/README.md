# AlcheMarket

## Model Works

`Game` holds the recipes, reputation, and customer queue. It uses a singly linked `LinkedQueue<Customer>` to serve customers in arrival order. The model accepts ingredient selections and returns a service result without reading from or writing to the console.

- **Run the model by itself.** Open `Test.java` and run its `main` method. In VS Code, click **Run** above `main`. In Eclipse, choose **Run As → Java Application**. The driver serves the first customer, checks that reputation rises from 50 to 65, and prints the next customer. It requires no keyboard input or dialogue file.
- **Run the JUnit 4 tests.** Open the `AlcheMarket` project folder in VS Code and select **Testing → Run All Tests**, or import it as an existing Eclipse project and choose **Run As → JUnit Test**. `LinkedQueueTest` checks FIFO order, `peek` without removal, empty-queue behavior, rejection of null entries, and reuse after the queue is drained. `PrototypeTest` also serves all seven customers directly through the model.

## Thin View

Run `Main.java` as a Java application in the VS Code integrated terminal or the Eclipse Console. Keep `customer-dialogue.tsv` in the project root so the console can load it as a resource.

Enter a player name, read the order and recipe clue, then enter one ingredient number at a time. The cauldron has five slots and accepts two to five distinct ingredients. Use `undo` to remove the last selection, `reset` to clear the cauldron, and `recipes` to open the recipe book. Enter `brew` when your selection is ready to brew and serve. Enter `quit` to leave the game.

`Main` connects `ConsoleView`, the `Validation` controller, and `Game`. `Validation` turns commands into model calls and passes the results to `ConsoleView`, which displays the order, dialogue, and score.

## Runs Without Crashing

The prototype starts with seven customers and shows the final score when the queue empties or reputation reaches zero. For the first order, enter `1`, `2`, and `3` on separate lines, then enter `brew`. Those three slots always make the starter potion. Later clues leave out one ingredient from a valid recipe. Check the recipe book with `recipes` for combinations you already know.

The JUnit tests cover blank and unknown commands, missing selections, duplicate selections, misspelled or locked ingredients, negative and out-of-range indexes, and oversized or nonnumeric input. Invalid selections leave the current customer and reputation unchanged. Tests also cover `undo`, `reset`, a complete seven-customer run, and rejected service both after the queue empties and after reputation reaches zero with customers still waiting. The suite has 35 JUnit tests across LinkedQueueTest, PrototypeTest, CustomerDialogueTest, PlayerTest, and PotionTest.

## Reflection

The specification described separate brewing and serving actions, with the model keeping a brewed potion until the player chose to serve it. For the first prototype, we combined those actions into one `brew` command so we could test a complete customer turn through the controller. The first console version required a comma-separated command. During a walkthrough, that was awkward to type and gave little feedback before submission. `Validation` now keeps the selections for the current turn and redraws the cauldron after each choice. Players can undo or reset their selections before committing the turn.

The first prototype accepted exactly three ingredients. To bring it closer to the specification, we expanded recipes to two through five distinct ingredients. The starter order still uses a known three-ingredient recipe, and five materials are unlocked at the start so all recipe sizes are playable. We assign additional recipes as materials unlock to avoid generating every possible five-ingredient combination at startup. Sorted combinations let players enter ingredients in any order. Later orders use recipes that can be made with the starting materials. The prototype starts with seven customers and ends when the queue empties or reputation reaches zero. The Scope described waiting for another arrival when the queue emptied. This version treats serving the last customer as the end of the run and rejects further service.

The Scope left open how to store recipes, vary them between games, and give customer types distinct personalities. We use a map from sorted combinations to potions. The seven-customer queue gives players more turns to try different recipe lengths, unlock materials, and see different customer dialogue. Expanding from three to seven customers exposed tests that assumed particular orders and a three-customer ending. The model now accepts a fixed seed for tests, while normal launches still use a fresh seed. The console loads customer lines from a TSV file so the team can revise dialogue without editing Java. Each ordinary customer type has separate greeting, thanks, and wrong-potion pools. Customer type currently determines dialogue, and type-specific order lists remain open.

The model runs independently through `Test.java` and the direct model tests. When ASCII scenes were added, the poison branch initially printed from `Game`. We moved that call to `ConsoleView` after seeing the scene appear during model testing. `Game` now returns the outcome and potion name, and the controller passes the result to the view. The poison regression test also checks that the next customer and reputation stay unchanged if the player tries to serve again after losing.

Separate brewing and serving, repeated ingredients within one recipe, and special customer behavior remain open. The Scope's insufficient-funds case applied to purchases. The current game uses reputation and has no purchase command or player balance, so that input case does not apply to this version.

## Next Steps

For Deliverable 4, we plan to finish the recipe book first, then add a special customer and test both changes together. The assignments below are proposed based on each member's work on the prototype.

| Order | Task | Proposed owner | Verification |
| --- | --- | --- | --- |
| 1 | Make the recipe book easier to scan. Group recipes by potion, number each combination, and show the ingredient count and current ingredient numbers. Keep the current cauldron selection when the book is opened. | Sarah leads the controller and display changes. Gabe checks that the recipe data already exposes what the view needs. | Verify that a potion with multiple recipes shows each combination once and that opening the book preserves the selected ingredients, current customer, and reputation. |
| 2 | Add Duke Lancaster as a special customer with a unique request, dialogue, and a one-time reward for correct delivery. | Greek defines the customer and dialogue. Gabe integrates the order and reward into the game model. Sarah connects the reward display. | Verify that the order can be brewed using available materials, correct delivery awards the bonus once, and a wrong potion or dud earns no bonus. |
| 3 | Run the queue, controller, and game regression tests after both changes are integrated. | Kyra leads verification, with Greek and Sarah reviewing the new customer and console tests. | Verify that the special customer follows FIFO order, the original brew controls still work, and invalid commands and service after game over leave game state unchanged. |

**Change feature.** We will add `SpecialCustomer` as a subclass of `Customer`. The `Game(String, long)` constructor will enqueue Duke Lancaster at a defined position. `Game.serveWithDetails()` will apply the reward on correct delivery and expose it through `Game.ServiceResult`. `ConsoleView.showTurn()` will show the Duke's request, and `ConsoleView.showOutcome()` will show the reward. We expect `LinkedQueue.enqueue()`, `LinkedQueue.dequeue()`, and the `brew` branch of `Validation.processLine()` to work with the existing interface.

For recipe-book improvements, the `recipes` branch of `Validation.processLine()` will pass the available ingredients to `ConsoleView.showRecipeBook()` so it can display ingredient numbers. We expect to reuse `Game.getDiscoveredRecipes()` for the recipe data.

**Stretch goals.** We plan to implement one special-customer encounter. Multi-stage missions, a separate event queue, and recipe autofill are outside the Deliverable 4 plan.

## Attribution

| Class or test file | Contributor(s) | Work performed |
| --- | --- | --- |
| `AsciiArt.java` | Gabe | Added ASCII scenes. |
| `ConsoleView.java` | Greek | Added customer dialogue and the cauldron display. |
| `Customer.java` | Greek | Defined customer identity, type, order, and clue. |
| `CustomerDialogue.java` | Greek | Added loading and selection for customer dialogue. |
| `CustomerDialogueTest.java` | Greek, Kyra | Greek tested dialogue pools, wrong-order feedback, and ingredient selection. Kyra contributed customer testing. |
| `CustomerType.java` | Greek | Defined the six ordinary customer types. |
| `Game.java` | Gabe | Implemented the original game logic and potion mapping algorithm. |
| `LinkedQueue.java` | Kyra | Drafted and refined the FIFO queue. |
| `LinkedQueueTest.java` | Kyra | Drafted and refined the queue tests. |
| `Main.java` | Sarah, Gabe | Sarah drafted the entry point. Gabe added the opening scene. |
| `NormalCustomer.java` | Greek | Added the ordinary customer implementation. |
| `Player.java` | Gabe | Implemented the original player class. |
| `PlayerTest.java` | Kyra | Tested reputation updates and recipe history. |
| `Potion.java` | Gabe | Implemented the original potion class. |
| `PotionTest.java` | Kyra | Tested potion construction, poison flags, and recipe matching. |
| `PrototypeTest.java` | Greek | Tested the model, invalid input, and both game endings. |
| `Test.java` | Greek | Added the standalone model driver. |
| `Validation.java` | Sarah, Greek | Sarah drafted the controller. Greek added keyboard brewing controls. |

## GenAI Note

Greek used GenAI to help improve the customer dialogue system and streamline the game's controls. Greek also used GenAI to review the test classes, troubleshoot failing tests, and address gaps in test coverage. Gabe used Sonnet to help create the ASCII scenes. Kyra used GenAI to review what to add or remove in the tests.

# AlcheMarket

## Model Works

`Game` holds the recipes, reputation, and customer queue. It uses a singly linked `LinkedQueue<Customer>` to serve customers in arrival order. The model accepts ingredient selections and returns a service result without reading from or writing to the console.

- **Run the model by itself.** Open `Test.java` and run its `main` method. In VS Code, click **Run** above `main`. In Eclipse, choose **Run As → Java Application**. The driver serves the first customer, checks that reputation rises from 50 to 65, and prints the next customer. It requires no keyboard input or dialogue file.
- **Run the JUnit 4 tests.** Open the `AlcheMarket` project folder in VS Code and select **Testing → Run All Tests**, or import it as an existing Eclipse project and choose **Run As → JUnit Test**. `LinkedQueueTest` checks FIFO order, `peek` without removal, empty-queue behavior, rejection of null entries, and reuse after the queue is drained. `PrototypeTest` also serves customers and closes a day directly through the model.

## Thin View

Run `Main.java` as a Java application in the VS Code integrated terminal or the Eclipse Console. Keep `customer-dialogue.tsv` in the project root so the console can load it as a resource.

Enter a player name, read the order and recipe clue, then enter one ingredient number at a time. The cauldron has three slots and requires three distinct ingredients. Use `undo` to remove the last selection, `reset` to clear the cauldron, and `recipes` to open the recipe book. Enter `brew` when your selection is ready to brew and serve. Enter `quit` to leave the game.

`Main` connects `ConsoleView`, the `Validation` controller, and `Game`. `Validation` turns commands into model calls and passes the results to `ConsoleView`, which displays the order, dialogue, and score.

## Runs Without Crashing

The first order uses ingredients `1`, `2`, and `3`. Enter them on separate lines, then enter `brew`. Each brew uses one unit of each selected ingredient. Ordinary orders are accepted by default. Enter `D` to decline before brewing.

When an herbalist arrives, enter `buy <offer> <quantity>`, such as `buy 1 2`. Enter `D` when you are done shopping. Ingredient prices vary by herb and day. A correct delivery earns the current market value of the three ingredients plus 15%, rounded up to a whole gold piece. Moonpetal requires at least 65 reputation and two correct orders that day. The first Moonpetal purchase of the day queues Duke Lancaster. His order is marked `SPECIAL` in purple.

The counter looks up the current order in the recipe book. It shows a known formula, a saved partial tip, or a message that the book has no entry. Customer tips and successfully brewed formulas are kept across days. A recipe dealer sells complete formulas with `buy <recipe>`. The base price of a formula is half the market price of one bottle, rounded up. Buying it adds an entry to the book.

Use `haggle 5`, `haggle 15`, `haggle 20`, or `haggle 50` to buy at a discount or ask more for a potion. Use a negative number such as `haggle -15` to pay more or offer a cheaper potion, and `haggle 0` to reset. An accepted trade changes reputation by one point per five percentage points negotiated. At 80 reputation, the base rate automatically becomes 5% cheaper when buying and 5% higher when selling. Final prices are rounded to whole gold pieces, with a minimum of 1.

When the queue is empty, the shop shows the day's correct orders, net gold, balance, and reputation. Press Enter to open the next day or `Q` to quit. Gold, stock, and reputation carry over. Reputation at zero ends the game.

Rook the Ferryman visits on Days 2, 3, and 4. Press `T` for a line about the ferry, `A` to draw, or `D` to decline. High card wins and equal ranks keep both stakes with their owners. If you win, enter `gold` or `herb` to choose your prize. On Day 4, `loan` lends Rook 10 gold for his ferry charter. When he returns on Day 5, enter `collect` to receive two Moonpetal.

Herbalist 1 starts carrying Silverleaf, Emberroot, and Frostmint on Day 2. Magister Vale arrives on Day 3 and orders a Royal Restorative. Stock Silverleaf for his Fever Tonic. For a Tier II order, select the three ingredients shown for the next bottle and enter `brew`. Repeat for the second bottle, then enter `combine` to deliver. The individual brews consume stock, and the final delivery pays once. After Vale leaves, ordinary buyers can occasionally order a Royal Restorative or Winterward Elixir. The recipe book lists their component potions.

JUnit covers queue operations, malformed selections, purchasing limits, daily settlement, and the special order. Invalid selections and failed purchases leave the current transaction unchanged.

## Build and Test from PowerShell

From the folder containing Main.java, use Java 11 or newer. Set the two JAR paths to your local JUnit 4.13.2 and Hamcrest Core installations.

~~~powershell
$junit = 'C:\path\to\junit-4.13.2.jar'
$hamcrest = 'C:\path\to\hamcrest-core-1.3.jar'
New-Item -ItemType Directory -Force .d4-build
$sources = (Get-ChildItem -File -Filter '*.java').Name
javac --release 11 -encoding UTF-8 -cp "$junit;$hamcrest" -d .d4-build $sources
java -cp .d4-build Test
Copy-Item customer-dialogue.tsv .d4-build
$tests = Get-ChildItem -File -Filter '*Test.java' |
    Where-Object BaseName -ne 'Test' | Select-Object -ExpandProperty BaseName
java -cp ".d4-build;$junit;$hamcrest" org.junit.runner.JUnitCore $tests
java -cp .d4-build Main
~~~

The standalone driver and the model and queue tests exercise the rules independently of the console. Separate controller tests cover command handling. The last verified suite has 62 passing tests.

## Reflection

Our D3 change feature was Duke Lancaster as a special customer in the existing FIFO queue. D4 adds `SpecialCustomer`, a named order for Lunar Remedy, and a payout for correct delivery. As predicted, `Game.serveWithDetails()` resolves the sale, `Game.ServiceResult` carries the result, and `ConsoleView` displays the special order and payout. We initially planned to enqueue Duke at a fixed position in the constructor. In the current game, purchasing Moonpetal after meeting its requirements adds him to the tail. That connects his arrival to something the player does and preserves earlier arrivals.

Duke uses the same three-item `brew` command as other buyers. The algorithms in `LinkedQueue.enqueue()` and `dequeue()` stayed the same. The queue class gained `snapshot()` for the waiting-list display. `Validation` added command routes for trading, Rook's wager, and Vale's two-stage order. The single-customer D3 prediction did not account for those additions. The actual feature touches `SpecialCustomer`, `Game`, and `ConsoleView`, with additional integration in `Validation` and model tests in `EconomyTest`.

`Game` owns inventory, currency, orders, and encounter results. `Validation` keeps the current input and passes model data to `ConsoleView` for each frame. The view does not keep an inventory, balance, or queue of its own. Adding Rook and Vale required the view and controller to understand the new encounter types and available actions. Their game rules are tested independently of the console.

We added finite stock, variable prices, and daily settlement so today's purchases affect tomorrow's stock. The recipe book now remembers tips and complete formulas. The current order displays shelf numbers for a known formula, while the full book groups combinations by potion and lists their ingredients. The D3 plan also called for numbering every formula in the full book. That work remains open, along with separate brewing and serving for ordinary orders and the remaining proposed encounters. Vale introduces occasional Tier II orders after his first visit.

For the next encounter, we would move each encounter's available actions and results behind a shared contract. The controller could forward the player's choice and the view could render the available actions with fewer visitor-type branches.

## Next Steps

The proposed owners follow the team's existing areas of responsibility.

| Order | Remaining work | Owner | Done when |
| --- | --- | --- | --- |
| 1 | Number all recipes in the book and mark ingredients already in stock. | Sarah | Players can compare two formulas for the same potion and choose one from the book's information. |
| 2 | Tune stock and special-order payouts. | Gabe | Ordinary sales can fund the ingredients in a Day 1 through Day 3 playthrough. |
| 3 | Polish Rook and Vale's prompts. | Greek | Players can finish both encounters using the commands shown at the counter. |
| 4 | Run the full suite and rehearse the demo. | Kyra, with the presenters | All tests pass and the complete demo fits under three minutes. |

**Stretch goals.** Keep haggling, distinct suppliers, Rook's returning visits, and Vale's Tier II orders. Leave recipe autofill, a separate event queue, and the Elspeth, Brann, and Mara encounters outside the final build.

### Three-minute demo

Compile before presenting. Run `java -cp .d4-build Main --seed 0` for the fixed Day 1 queue below. A scripted terminal rehearsal verified the 16 inputs, including the player name and quit command. This live plan budgets 2:45, leaving 15 seconds of buffer.

| Time | Live steps |
| --- | --- |
| 0:00–0:30 | Enter `Demo`. Point out the starter recipe, waiting queue, and balance. Enter `brew 1,2,3` to complete the first sale. |
| 0:30–1:05 | At Herbalist 1, try `buy 4 1` to show why Moonpetal is blocked. Enter `d` three times to reach Buyer 5, then enter `brew 1,3,4`. |
| 1:05–1:40 | Enter `d` five times to reach Herbalist 6. Enter `buy 3 1` to buy Moonpetal and show Duke join the queue. Enter `d` to reach him. |
| 1:40–2:10 | Show the special order and clue. Enter `brew 1,2,7`. Show the Lunar Remedy payout and the Day 1 summary. |
| 2:10–2:45 | Explain the input path through controller and model. Show the passing interleaved FIFO and special-order tests. |
| 2:45–3:00 | Buffer. |

## Attribution

| Class or test file | Contributor(s) | Work performed |
| --- | --- | --- |
| `AdvancedOrder.java` | Greek | Added Tier II orders and component progress. |
| `AdvancedOrderTest.java` | Greek | Tested component brewing, combining, and later orders. |
| `AsciiArt.java` | Gabe | Added and revised the ASCII scenes. |
| `ConsoleView.java` | Greek, Gabe | Greek added dialogue, the colored counter, and encounter panels. Gabe refined the UI and score display. |
| `Customer.java` | Greek | Defined customer identity, type, order, and clue. |
| `CustomerDialogue.java` | Greek | Added dialogue loading and selection. |
| `CustomerDialogueTest.java` | Greek, Kyra | Greek tested dialogue, feedback, and selection. Kyra contributed customer testing. |
| `CustomerTest.java` | Kyra | Tested customer construction, types, and clues. |
| `CustomerType.java` | Greek | Defined the ordinary customer types. |
| `EconomyTest.java` | Greek | Tested stock, purchases, pricing, haggling, daily turnover, and Duke's order. |
| `Game.java` | Gabe, Greek, Sarah | Gabe wrote the original game and potion mapping. Greek added daily turnover, the economy, and special encounters. Sarah added to system for determining what ingredients sellers offer and updated to take into account demand of customers for that day.|
| `HerbVendor.java` | Greek | Added fixed offers for individual herbalists. |
| `LinkedQueue.java` | Kyra, Greek | Kyra drafted and refined the FIFO queue. Greek added the waiting-list snapshot. |
| `LinkedQueueTest.java` | Kyra, Greek | Kyra drafted the queue tests. Greek added interleaved FIFO and snapshot checks. |
| `Main.java` | Sarah, Gabe, Greek | Sarah drafted the entry point. Gabe added the opening scene. Greek added the demo seed option. |
| `NormalCustomer.java` | Greek | Added the ordinary customer implementation. |
| `Player.java` | Gabe, Sarah | Implemented the original player class. Added decrease to reputation when order is declined|
| `PlayerTest.java` | Kyra | Tested reputation updates and recipe history. |
| `Potion.java` | Gabe | Implemented the original potion class. |
| `PotionTest.java` | Kyra | Tested potion construction and recipe matching. |
| `PrototypeTest.java` | Greek | Tested the model, invalid inputs, daily settlement, and game over. |
| `RecipeDealer.java` | Greek | Added the formula seller and its offers. |
| `RookVisit.java` | Greek | Defined returning visits, previous results, and card state. |
| `RookVisitTest.java` | Greek | Tested the wager, rejected stakes, loans, and repayment. |
| `SpecialCustomer.java` | Greek | Added Duke Lancaster's special order type. |
| `Test.java` | Greek | Added the standalone model driver. |
| `Validation.java` | Sarah, Greek | Sarah drafted the controller. Greek added keyboard brewing, trading, and encounter actions. |

## GenAI Note

Greek used Codex to help design and implement the counter, economy, recipe lookup, and special encounters. Greek also used Codex to draft and run tests, investigate bad-input handling, and document the D4 changes and demo. Gabe used Sonnet to help create the ASCII scenes. Kyra used GenAI to review what to add or remove in the tests.

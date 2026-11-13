import java.util.*;

/**
 * ALCHEMARKET - a 256-colour ANSI alchemist's lab (112 x 44 cells).
 * Compile: javac AlcheMarket.java
 * Run:     java AlcheMarket      (terminal must be at least 112 cols x 44 rows)
 * Grimoire: java AlcheMarket grimoire   (small recipe-book picture)
 * Scale:    java AlcheMarket scale       (balance scale weighing overflowing gold)
 * Poison:   java AlcheMarket poison      (an unlucky customer drinks the wrong potion)
 */
public class AsciiArt {
    static final int W = 112, H = 44;
    static final char[][] ch = new char[H][W];
    static final int[][] fg = new int[H][W], bg = new int[H][W];
    static final Random rnd = new Random(11);

    // ---------------------------------------------------------------- drawing helpers
    static void put(int x, int y, String s, int f, int b) {
        if (y < 0 || y >= H) return;
        for (int i = 0; i < s.length(); i++) {
            int xx = x + i;
            if (xx < 0 || xx >= W) continue;
            char c = s.charAt(i);
            if (c == ' ' && b < 0) continue;          // spaces are transparent unless a background is given
            ch[y][xx] = c;
            fg[y][xx] = f;
            if (b >= 0) bg[y][xx] = b;
        }
    }
    static void put(int x, int y, String s, int f) { put(x, y, s, f, -1); }
    static void put(int x, int y, char c, int f, int b) { put(x, y, String.valueOf(c), f, b); }
    static void put(int x, int y, char c, int f) { put(x, y, String.valueOf(c), f, -1); }

    /** Colour map: cm(key, displayChar, fg, bg, key, displayChar, fg, bg, ...) ; bg -1 = keep existing. */
    static Map<Character, int[]> cm(Object... a) {
        Map<Character, int[]> m = new HashMap<>();
        for (int i = 0; i < a.length; i += 4)
            m.put((Character) a[i], new int[]{(Character) a[i + 1], (Integer) a[i + 2], (Integer) a[i + 3]});
        return m;
    }

    static void spr(int x, int y, String[] lines, int f, Map<Character, int[]> cmap, int b) {
        for (int j = 0; j < lines.length; j++)
            for (int i = 0; i < lines[j].length(); i++) {
                char c = lines[j].charAt(i);
                int[] m = cmap == null ? null : cmap.get(c);
                if (m != null) put(x + i, y + j, (char) m[0], m[1], m[2]);
                else put(x + i, y + j, c, f, b);
            }
    }
    static void spr(int x, int y, String[] lines, int f, Map<Character, int[]> cmap) { spr(x, y, lines, f, cmap, -1); }

    static void fill(int x, int y, int w, int h, char c, int f, int b) {
        for (int yy = y; yy < y + h; yy++)
            for (int xx = x; xx < x + w; xx++)
                if (xx >= 0 && xx < W && yy >= 0 && yy < H) { ch[yy][xx] = c; fg[yy][xx] = f; bg[yy][xx] = b; }
    }

    static void line(int x0, int y0, int x1, int y1, char c, int f) {
        int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, err = dx + dy;
        while (true) {
            put(x0, y0, c, f);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    static int pick(int... a) { return a[rnd.nextInt(a.length)]; }
    static char pickc(String s) { return s.charAt(rnd.nextInt(s.length())); }
    static String rep(String s, int n) { return s.repeat(n); }

    // ---------------------------------------------------------------- window geometry
    static final int cx = 21, yc = 8, rx = 8, ry = 4, yb = 15;
    static boolean inside(int x, int y, double R, double RY, int bot) {
        if (y > bot) return false;
        if (y <= yc) { double a = (x - cx) / R, b = (y - yc) / RY; return a * a + b * b <= 1; }
        return Math.abs(x - cx) <= R;
    }

    // ---------------------------------------------------------------- sprites
    static final String[] BOTTLE = {" = ", "|_|", "/#\\", "|#|", "\\_/"};
    static final String[] VIAL = {" = ", "|#|", "|#|", "'-'"};
    static final String[] FLASK = {"  _  ", " | | ", "/###\\", "\\###/"};
    static final String[] JAR = {"[===]", "|o o|", "|#o#|", "'---'"};
    static final String[] SKULL = {" .-. ", "(o.o)", " \\_/ "};
    static final String[] BOOKS = {" _____ ", "|=====|", "|_[=]_|"};
    static final String[] HOURG = {"=====", " \\:/ ", "  X  ", " /:\\ ", "====="};

    static void item(int x, int by, String[] lines, int col, int f, Map<Character, int[]> extra) {
        Map<Character, int[]> m = cm('#', '#', col, -1, '=', '=', 94, -1, 'o', 'o', 255, -1, ':', ':', 220, -1);
        if (extra != null) m.putAll(extra);
        spr(x, by - lines.length, lines, f, m);
    }
    static void item(int x, int by, String[] lines, int col) { item(x, by, lines, col, 252, null); }

    static void herb(int x, int top, int len) {
        for (int y = top; y < top + len; y++) put(x, y, '|', 94);
        int y = top + len;
        String[] shape = {" \\|/ ", "/\\|/\\", "\\ | /", " \\|/ ", "  v  "};
        for (int j = 0; j < shape.length; j++)
            for (int i = 0; i < shape[j].length(); i++) {
                char c = shape[j].charAt(i);
                if (c == ' ') continue;
                if (rnd.nextDouble() < 0.12) put(x - 2 + i, y + j, 'o', pick(213, 220, 141));
                else put(x - 2 + i, y + j, c, pick(28, 34, 70, 106));
            }
    }

    // ---------------------------------------------------------------- rendering
    static void clearGrid() {
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++) { ch[y][x] = ' '; fg[y][x] = 7; bg[y][x] = 0; }
    }

    /** Turns a rectangle of the canvas into a string of ANSI escape codes. */
    static String render(int x0, int y0, int w, int h) {
        StringBuilder sb = new StringBuilder();
        for (int y = y0; y < y0 + h; y++) {
            int cf = -1, cb = -1;
            for (int x = x0; x < x0 + w; x++) {
                if (fg[y][x] != cf) { sb.append("\u001b[38;5;").append(fg[y][x]).append('m'); cf = fg[y][x]; }
                if (bg[y][x] != cb) { sb.append("\u001b[48;5;").append(bg[y][x]).append('m'); cb = bg[y][x]; }
                sb.append(ch[y][x]);
            }
            sb.append("\u001b[0m\n");
        }
        return sb.toString();
    }

    static void center(int x, int w, int y, String s, int f, int b) {
        put(x + (w - s.length()) / 2, y, s, f, b);
    }

    /**
     * Prints a small (46 x 19 cell) picture of the AlcheMarket recipe grimoire:
     * a leather-bound book lying open on a recipe page, with a ribbon bookmark.
     * Call it from game code, or run:  java AlcheMarket grimoire
     */
    public static void printGrimoire() {
        clearGrid();
        fill(0, 0, 46, 17, '#', 94, 52);                              // leather cover
        for (int y = 1; y < 16; y++) for (int x = 0; x < 46; x++)
            if (rnd.nextDouble() < 0.12) put(x, y, '%', 130);
        fill(0, 0, 46, 1, '=', 180, 52);
        fill(0, 16, 46, 1, '=', 180, 52);
        put(10, 0, " GRIMOIRE OF ALCHEMARKET ", 220, 52);
        for (int[] c : new int[][]{{0, 0}, {45, 0}, {0, 16}, {45, 16}}) put(c[0], c[1], '+', 220, 52);

        fill(2, 1, 20, 14, ' ', 52, 223);                              // parchment pages
        fill(24, 1, 20, 14, ' ', 52, 223);
        for (int y = 1; y < 15; y++) { put(22, y, ')', 94, 223); put(23, y, '(', 94, 223); }

        // left page: ingredients and a potion
        center(2, 20, 2, "Elixir of Vigor", 124, 223);
        center(2, 20, 3, rep("~", 16), 94, 223);
        String[] ingredients = {"2 sprigs Moonpetal", "1 vial Wyrm Blood", "3 drops Mercury", "1 pinch Bone Dust"};
        for (int i = 0; i < ingredients.length; i++) {
            put(3, 5 + i, ingredients[i].substring(0, 1), 124);
            put(4, 5 + i, ingredients[i].substring(1), 52);
        }
        spr(9, 10, FLASK, 240, cm('#', '#', 201, -1));

        // right page: method, warning, runes and a wax seal
        center(24, 20, 2, "Method", 124, 223);
        center(24, 20, 3, rep("~", 16), 94, 223);
        String[] steps = {"Grind petals", "Warm the blood", "Add mercury", "Stir thrice", "Bottle at dusk"};
        for (int i = 0; i < steps.length; i++) {
            put(25, 5 + i, (i + 1) + ".", 124);
            put(28, 5 + i, steps[i], 52);
        }
        center(24, 20, 11, "Do NOT drink raw!", 196, 223);
        put(26, 13, "+ x # & % @ $ *", 135);
        put(39, 12, "(@)", 196);

        put(22, 17, "||", 196);                                        // ribbon bookmark
        put(22, 18, "\\/", 196);
        System.out.print(render(0, 0, 46, 19));
    }

    /**
     * Prints a small (50 x 22 cell) picture of a brass balance scale: the left pan is heaped
     * with overflowing gold (so the beam tips down), coins spilling onto the table, while the
     * right pan holds a single 1g weight. Call it from game code, or run:  java AlcheMarket scale
     */
    public static void printScale() {
        clearGrid();
        fill(0, 19, 50, 1, '=', 180, 94);                              // table
        fill(0, 20, 50, 1, '#', 94, 52);
        put(19, 18, "/___________\\", 178);                            // pedestal and pillar
        for (int y = 6; y < 18; y++) put(24, y, "[#]", 178);
        put(25, 3, '^', 220); put(25, 4, '|', 220);
        line(7, 8, 43, 2, '=', 178);                                   // beam, tipped toward the gold
        put(24, 5, "(@)", 220);
        line(7, 8, 1, 14, '/', 245); line(7, 8, 13, 14, '\\', 245);   // chains
        line(43, 2, 37, 8, '/', 245); line(43, 2, 49, 8, '\\', 245);
        put(1, 14, "\\___________/", 178);                            // heavy (left) pan
        put(2, 15, "\\_________/", 178);
        put(3, 16, "'-------'", 136);
        put(37, 8, "\\___________/", 178);                            // light (right) pan
        put(38, 9, "\\_________/", 178);
        put(39, 10, "'-------'", 136);
        put(41, 7, "[1g]", 245); put(46, 7, '~', 250);                  // one small weight and a feather

        int[][] pile = {{9, 3}, {10, 5}, {11, 7}, {12, 9}, {13, 11}};  // {row, width}: the gold heap
        for (int[] r : pile)
            for (int i = 0; i < r[1]; i++)
                put(7 - r[1] / 2 + i, r[0], pickc("@O0$o"), pick(220, 226, 178, 214, 184), 94);
        put(1, 13, 'o', 220); put(13, 13, '@', 226);                   // coins on the rim
        for (int[] sp : new int[][]{{6, 10}, {9, 12}, {4, 12}, {7, 9}, {11, 11}}) put(sp[0], sp[1], '*', 230, 94);
        for (int[] f : new int[][]{{14, 12}, {16, 14}, {15, 16}, {17, 17}}) put(f[0], f[1], 'o', 220);  // falling coins
        for (int x = 6; x <= 17; x++) {                                // coins spilled across the table
            if (rnd.nextDouble() < 0.75) put(x, 18, pickc("o@0"), pick(220, 226, 178));
            if (x >= 11 && x <= 16 && rnd.nextDouble() < 0.6) put(x, 17, pickc("o@0"), pick(220, 226, 178));
        }
        for (int[] sp : new int[][]{{0, 8}, {2, 6}, {12, 9}, {15, 11}, {9, 7}}) put(sp[0], sp[1], '+', 230);
        center(0, 50, 21, "~ ALCHEMARKET ASSAY SCALE ~", 220, -1);
        System.out.print(render(0, 0, 50, 22));
    }

    static void puddle(int pcx, int pcy, double prx, double pry, String chars, int c1, int c2, int c3) {
        for (int y = 16; y <= 19; y++)
            for (int x = pcx - (int) prx - 1; x <= pcx + (int) prx + 1; x++) {
                double a = (x - pcx) / prx, b = (y - pcy) / pry;
                if (a * a + b * b <= 1) put(x, y, pickc(chars), pick(c1, c2, c3), 22);
            }
    }

    /**
     * Prints a small (50 x 22 cell) horror-comedy "game over" picture: a customer on his knees,
     * X-eyed, bleeding from the eyes, foaming and vomiting green after drinking a poisoned potion,
     * one hand clawing his throat and the other reaching for the spilled flask while a ghost
     * skull rises from the fumes. Call it from game code, or run:  java AlcheMarket poison
     */
    public static void printPoisoned() {
        printPoisoned(System.out);
    }

    public static void printPoisoned(java.io.PrintStream output) {
        clearGrid();
        for (int y = 0; y < 16; y++)                                   // wall, lit green by the spill
            for (int x = 0; x < 50; x++) {
                double d = Math.hypot((x - 38) / 2.2, y - 14);
                ch[y][x] = ((x + 4 * (y % 2)) % 9 == 0) ? '|' : '_';
                fg[y][x] = d < 6 ? 29 : d < 11 ? 23 : 238;
                bg[y][x] = d < 6 ? 22 : 233;
            }
        for (int y = 16; y < 22; y++)                                  // stone floor
            for (int x = 0; x < 50; x++) {
                ch[y][x] = y == 16 ? '=' : ((x + y * 5) % 17 == 0 ? '|' : '-');
                fg[y][x] = y == 16 ? 94 : 58;
                bg[y][x] = 52;
            }

        // kneeling customer: robe, then head
        String[] robe = {
                "    ,---'   '---.    ",
                "  ,'   |     |   '.  ",
                " /     |     |     \\ ",
                "|      |     |      |",
                "|========[#]========|",
                "|      |     |      |",
                "|______|_____|______|"};
        spr(7, 9, robe, 88, cm('=', '=', 220, -1, '[', '[', 220, -1, '#', '#', 220, -1, ']', ']', 220, -1), 52);
        String[] head = {
                "   .-'''''-.   ",
                "  /         \\  ",
                " |  X     X  | ",
                " |     ^     | ",
                " |  .-vvv-.  | ",
                "  \\ '-ooo-' /  ",
                "   '-.___.-'   "};
        for (int r = 1; r <= 5; r++) fill(12, 2 + r, 11, 1, ' ', 7, 151);   // sickly green skin
        spr(10, 2, head, 94, cm('X', 'X', 232, 151, 'v', 'v', 118, 22, 'o', 'o', 255, 22));
        put(14, 5, ':', 196); put(20, 5, ':', 196);                    // blood tears
        put(14, 6, '|', 196); put(20, 6, '|', 196);
        put(13, 3, '~', 28); put(21, 3, '~', 28); put(12, 4, '/', 28); put(22, 4, '\\', 28);   // green veins
        put(17, 8, '|', 46);                                           // dribbling drool

        // hand clawing the throat, other arm reaching for the flask
        spr(15, 9, new String[]{"\\\\|//", "(___)"}, 223, null);
        line(14, 10, 11, 13, '=', 223); put(10, 13, 'o', 223);
        line(27, 11, 34, 14, '=', 88); line(27, 12, 34, 15, '=', 124);
        put(35, 14, "-,", 223); put(35, 15, "-'", 223);

        // the fallen poison flask
        String[] flask = {
                "   .-------. ",
                "==(  #####  )",
                "]=(  #(x)#  )",
                "   '-------' "};
        fill(40, 13, 9, 2, ' ', 7, 22);
        spr(37, 12, flask, 250, cm('#', '#', 46, 22, 'x', 'x', 231, 22, '=', '=', 178, -1, ']', ']', 94, -1));
        put(38, 15, 'o', 46);

        puddle(38, 17, 9, 1.3, "~~~o'", 46, 40, 82);                   // spilled poison
        puddle(17, 17, 9, 1.4, "~~ov%", 118, 70, 106);                 // vomit with chunks
        for (int k = 0; k < 14; k++) put(3 + rnd.nextInt(28), 16 + rnd.nextInt(3), pickc(".:'"), pick(124, 196));

        // ghost skull rising from the fumes
        spr(35, 2, new String[]{" .-. ", "(x.x)", " \\v/ "}, 118, null);
        for (int y = 11; y >= 5; y--)
            put(37 + (int) Math.round(Math.sin(y) * 2), y, pickc("~)(~"), pick(118, 71, 35));

        center(0, 50, 20, "~ Potion No. 7 was NOT a healing potion ~", 244, 52);
        center(0, 50, 21, "Y O U   D I E D", 196, 52);
        output.print(render(0, 0, 50, 22));
    }

    // ---------------------------------------------------------------- scene
    static public void printMain() {
        clearGrid();

        // stone wall lit by the fire
        final int FX = 52, FY = 32;
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++) {
                double d = Math.hypot((x - FX) / 2.2, y - FY);
                int f, b;
                if (d < 7) { f = 172; b = 52; }
                else if (d < 12) { f = 130; b = 52; }
                else if (d < 17) { f = 94; b = 235; }
                else if (d < 24) { f = 241; b = 234; }
                else { f = 238; b = 233; }
                char c = ((x + 4 * (y % 2)) % 9 == 0) ? '|' : '_';
                if (rnd.nextDouble() < 0.04) c = pickc(".,'`:");
                ch[y][x] = c; fg[y][x] = f; bg[y][x] = b;
            }

        // roof beam and posts
        fill(0, 0, W, 1, '=', 180, 94);
        fill(0, 1, W, 1, '#', 94, 52);
        for (int x = 0; x < W; x++) if (rnd.nextDouble() < 0.15) put(x, 1, '%', 130, 52);
        fill(0, 2, W, 1, '=', 58, 52);
        for (int px : new int[]{0, 110}) {
            fill(px, 3, 2, 32, '#', 94, 52);
            for (int y = 3; y < 35; y++) put(px + (px == 0 ? 1 : 0), y, '|', 130, 52);
        }

        // arched moonlit window
        for (int y = 3; y <= yb + 1; y++)
            for (int x = cx - rx - 3; x <= cx + rx + 3; x++)
                if (inside(x, y, rx + 2, ry + 1.8, yb + 1)) { ch[y][x] = pickc("#%#"); fg[y][x] = 246; bg[y][x] = 240; }
        for (int y = 3; y <= yb; y++)
            for (int x = cx - rx - 3; x <= cx + rx + 3; x++)
                if (inside(x, y, rx, ry, yb)) {
                    int b = y < 7 ? 16 : y < 11 ? 17 : 18;
                    if (rnd.nextDouble() < 0.06) { ch[y][x] = pickc(".*+"); fg[y][x] = pick(255, 229, 153); }
                    else { ch[y][x] = ' '; fg[y][x] = 7; }
                    bg[y][x] = b;
                }
        for (int y = 4; y <= yb; y++) if (inside(cx, y, rx, ry, yb)) put(cx, y, '|', 245);
        for (int x = cx - rx; x <= cx + rx; x++) put(x, 11, '-', 245);
        put(cx, 11, '+', 250);
        fill(cx + 2, 6, 5, 3, ' ', 230, 230);                        // moon
        int[][] corners = {{cx + 2, 6}, {cx + 6, 6}, {cx + 2, 8}, {cx + 6, 8}};
        for (int[] c : corners) { ch[c[1]][c[0]] = ' '; fg[c[1]][c[0]] = 7; bg[c[1]][c[0]] = c[1] < 7 ? 16 : 17; }
        put(cx + 3, 6, 'o', 223, 230); put(cx + 5, 7, '.', 223, 230); put(cx + 3, 8, '.', 223, 230);
        for (int y = 12; y <= 15; y++)                               // castle silhouette
            for (int i = 0; i < 17; i++) {
                boolean on = y >= 14
                        || (y == 13 && ((i >= 3 && i <= 7) || (i >= 9 && i <= 13) || i == 15 || i == 16))
                        || (y == 12 && (i == 4 || i == 6 || i == 10 || i == 12));
                int x = cx - rx + i;
                if (on && inside(x, y, rx, ry, yb)) put(x, y, ' ', 7, 16);
            }
        put(cx - 3, 13, '.', 220, 16); put(cx + 2, 13, '.', 220, 16);
        put(cx - rx - 3, 17, rep("=", 22), 250, 244);

        // chimney hood, mantel, hearth
        final int hx = 52;
        for (int y = 3; y < 16; y++) {
            int half = 3 + (int) ((y - 3) / 12.0 * 11);
            for (int x = hx - half; x <= hx + half; x++) {
                ch[y][x] = ((x + 4 * (y % 2)) % 7 == 0) ? '|' : '_'; fg[y][x] = 247; bg[y][x] = 239;
            }
            put(hx - half, y, '/', 252, 239);
            put(hx + half, y, '\\', 252, 239);
        }
        fill(35, 16, 35, 1, '=', 180, 94);
        fill(36, 17, 33, 1, '#', 94, 52);
        for (int y = 18; y < 35; y++)
            for (int x = 38; x < 67; x++) {
                if (x >= 41 && x <= 63 && y >= 21) {
                    double d = Math.hypot((x - FX) / 2.0, y - 32);
                    ch[y][x] = ' '; fg[y][x] = 7; bg[y][x] = d < 5 ? 88 : d < 9 ? 52 : 232;
                } else {
                    ch[y][x] = ((x + 4 * (y % 2)) % 6 == 0) ? '|' : '_'; fg[y][x] = 250; bg[y][x] = 241;
                }
            }
        put(50, 18, "<[ ]>", 220, 241);

        // cauldron
        final int c2 = 52;
        put(c2 - 10, 24, ",-" + rep("'", 17) + "-,", 250);
        for (int i = -9; i < 10; i++) put(c2 + i, 25, pickc("~~~~~oO.~"), pick(46, 40, 82, 118), 22);
        put(c2 - 10, 25, '(', 250, 22); put(c2 + 10, 25, ')', 250, 22);
        int[][] body = {{26, 21}, {27, 21}, {28, 19}, {29, 15}, {30, 9}};
        for (int[] r : body) {
            int y = r[0], w = r[1], x0 = c2 - w / 2;
            put(x0, y, rep("#", w), 237, 233);
            put(x0, y, w == 21 ? '(' : '\\', 250, 233);
            put(x0 + w - 1, y, w == 21 ? ')' : '/', 250, 233);
            put(x0 + 3, y, ':', 244, 233);
        }
        put(c2 - 2, 28, "<*>", 46, 233);
        put(c2 - 8, 31, '/', 240); put(c2 + 8, 31, '\\', 240);
        put(c2 - 9, 32, '/', 240); put(c2 + 9, 32, '\\', 240); put(c2, 31, '|', 240);
        for (int y = 23; y >= 21; y--)
            for (int k = 0; k < 5; k++) put(c2 - 7 + rnd.nextInt(15), y, pickc("~)(,."), pick(252, 250, 247));
        put(c2 - 11, 34, rep("=", 23), 130, 94);
        for (int k = 0; k < 10; k++) put(c2 - 11 + rnd.nextInt(23), 34, rnd.nextBoolean() ? '*' : '.', 208, 88);
        for (int x = c2 - 12; x <= c2 + 12; x++) {
            int h = (int) (5 - Math.abs(x - c2) / 12.0 * 3 + rnd.nextDouble() * 3);
            for (int k = 0; k < h; k++) {
                double r = k / (double) Math.max(h - 1, 1);
                int f; char c;
                if (r < 0.3) { f = pick(226, 220); c = pickc("@#"); }
                else if (r < 0.7) { f = 208; c = pickc("()"); }
                else { f = 196; c = pickc("^`'"); }
                put(x, 33 - k, c, f, 52);
            }
        }

        // mantel candles and skull
        int[][] candles = {{42, 2}, {46, 3}, {58, 2}, {62, 3}};
        for (int[] cd : candles) {
            for (int i = 0; i < cd[1]; i++) put(cd[0], 15 - i, '|', 230);
            put(cd[0], 15 - cd[1], '^', 226);
            put(cd[0] - 1, 15 - cd[1], '.', 214); put(cd[0] + 1, 15 - cd[1], '.', 214);
        }
        spr(50, 13, SKULL, 255, cm('o', 'O', 196, -1));
        put(52, 12, '^', 226);

        // hanging herbs
        herb(36, 3, 4); herb(42, 3, 8); herb(64, 3, 3); herb(67, 3, 7); herb(34, 3, 9);

        // shelves of potions
        for (int by : new int[]{9, 15, 21}) { fill(70, by, 39, 1, '=', 180, 94); fill(70, by + 1, 39, 1, '#', 94, 52); }
        Map<Character, int[]> skullEyes = cm('o', 'O', 196, -1);
        Map<Character, int[]> pages = cm('=', '=', 180, -1);
        item(72, 9, BOTTLE, 46); item(77, 9, VIAL, 201); item(81, 9, FLASK, 39);
        item(88, 9, SKULL, 255, 255, skullEyes);
        item(94, 9, HOURG, 220); item(101, 9, BOTTLE, 208); item(106, 9, VIAL, 226);
        item(72, 15, BOOKS, 7, 124, pages); item(80, 15, JAR, 118);
        item(87, 15, BOTTLE, 196); item(92, 15, FLASK, 226); item(99, 15, JAR, 93); item(106, 15, VIAL, 51);
        item(72, 21, BOTTLE, 33); item(77, 21, BOTTLE, 171);
        item(83, 21, SKULL, 255, 255, skullEyes);
        item(90, 21, BOOKS, 7, 22, pages); item(99, 21, VIAL, 46); item(103, 21, FLASK, 208);

        // hanging ALCHEMARKET shop sign
        for (int sx : new int[]{79, 87}) { put(sx, 23, '|', 245); put(sx, 24, '|', 245); }
        put(77, 25, "+-----------+", 180, 94);
        put(77, 26, "|", 180, 94); put(78, 26, "ALCHEMARKET", 226, 52); put(89, 26, "|", 180, 94);
        put(77, 27, "+-----------+", 180, 94);

        // barrels and chest
        String[] barrel = {" .-----. ", "/=======\\", "|  ELIX |", "|=======|", "\\=======/", " '-----' "};
        Map<Character, int[]> bm = cm('=', '=', 245, -1, 'E', 'E', 220, -1, 'L', 'L', 220, -1, 'I', 'I', 220, -1, 'X', 'X', 220, -1);
        spr(90, 29, barrel, 130, bm); spr(99, 29, barrel, 130, bm);
        String[] chest = {" ________ ", "/________\\", "|==[##]==|", "|________|"};
        spr(79, 31, chest, 130, cm('=', '=', 220, -1, '#', '#', 226, -1, '[', '[', 220, -1, ']', ']', 220, -1));

        // workbench with alembic
        fill(2, 28, 36, 1, '=', 180, 94);
        fill(3, 29, 34, 1, '#', 94, 52);
        for (int lx : new int[]{4, 34}) for (int y = 30; y < 35; y++) put(lx, y, "|#", 130, 52);
        put(6, 32, rep("=", 28), 94);
        String[] alembic = {
                "     .--.     ",
                "    /    \\    ",
                "    |    |    ",
                "    \\    /    ",
                "  ,-'    '-.  ",
                " / ~~~~~~~~ \\ ",
                "| ~LLLLLLLL~ |",
                "\\ ~LLLLLLLL~ /",
                " '-.______.-' ",
                "   [=fFFf=]   "};
        spr(4, 18, alembic, 252, cm('L', '#', 201, 53, '~', '~', 207, -1, 'F', '^', 226, -1, 'f', '^', 208, -1,
                '[', '[', 240, -1, ']', ']', 240, -1, '=', '=', 240, -1));
        put(14, 19, rep("_", 12), 252); put(26, 19, '.', 252);
        for (int y = 20; y < 23; y++) put(26, y, '|', 252);
        String[] recv = {"  ,-+-.  ", " /     \\ ", "|:::::::|", "|:::::::|", "'-.___.-'"};
        spr(22, 23, recv, 252, cm(':', ':', 51, 23));
        String[] bk = {" _____ ", "|=====|", "|_[=]_|", "|=====|"};
        spr(31, 24, bk, 124, pages);
        put(34, 23, '|', 230); put(34, 22, '|', 230); put(34, 21, '^', 226);
        put(33, 21, '.', 214); put(35, 21, '.', 214);

        // wooden floor
        for (int y = 35; y < 42; y++)
            for (int x = 0; x < W; x++) {
                double d = Math.hypot((x - FX) / 2.2, y - FY);
                int f = d < 14 ? 130 : d < 26 ? 94 : 58, b = d < 14 ? 94 : 52;
                char c = ((x + y * 5) % 19 == 0) ? '|' : '-';
                if (rnd.nextDouble() < 0.05) c = pickc("~.'");
                ch[y][x] = c; fg[y][x] = f; bg[y][x] = b;
            }
        fill(0, 35, W, 1, '=', 94, 52);

        // ritual circle with pentagram
        double pcx = 56, pcy = 38.5, prx = 26, pry = 2.8;
        for (int t = 0; t < 720; t++) {
            double a = t * Math.PI / 360;
            put((int) Math.round(pcx + prx * Math.cos(a)), (int) Math.round(pcy + pry * Math.sin(a)), '*', 135);
            put((int) Math.round(pcx + (prx - 3) * Math.cos(a)), (int) Math.round(pcy + (pry - 1) * Math.sin(a)), 'o', 99);
        }
        int[][] pts = new int[5][2];
        for (int k = 0; k < 5; k++) {
            double a = Math.toRadians(-90 + 72 * k);
            pts[k][0] = (int) Math.round(pcx + (prx - 1) * Math.cos(a));
            pts[k][1] = (int) Math.round(pcy + (pry - 0.3) * Math.sin(a));
        }
        for (int k = 0; k < 5; k++) line(pts[k][0], pts[k][1], pts[(k + 2) % 5][0], pts[(k + 2) % 5][1], '.', 177);
        String runes = "+x#&%@$*";
        for (int k = 0; k < runes.length(); k++) {
            double a = k * Math.PI / 4 + 0.2;
            put((int) Math.round(pcx + prx * Math.cos(a)), (int) Math.round(pcy + pry * Math.sin(a)), runes.charAt(k), 213);
        }

        // black cat
        spr(16, 36, new String[]{" /\\_/\\  ", "( o.o ) ", " > ^ <~ "}, 240, cm('o', 'o', 226, -1));

        // title plate
        fill(0, 42, W, 1, '=', 94, 52);
        String title = "*  A L C H E M A R K E T  *   -   Potions, Reagents & Rare Curios";
        fill(0, 43, W, 1, ' ', 220, 0);
        put((W - title.length()) / 2, 43, title, 220, 0);

        System.out.print(render(0, 0, W, H));
    }

    public static void main(String[] args) {
        return;
    }
}

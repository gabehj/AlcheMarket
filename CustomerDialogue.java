import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CustomerDialogue {
    public enum Event {
        GREETING, CORRECT, WRONG
    }

    private static final String RESOURCE = "/customer-dialogue.tsv";
    private final Map<CustomerType, Map<Event, List<String>>> pools =
        new EnumMap<>(CustomerType.class);

    public CustomerDialogue() {
        InputStream stream = CustomerDialogue.class.getResourceAsStream(RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Missing resource " + RESOURCE);
        }
        for (CustomerType type : CustomerType.values()) {
            Map<Event, List<String>> events = new EnumMap<>(Event.class);
            for (Event event : Event.values()) {
                events.put(event, new ArrayList<>());
            }
            pools.put(type, events);
        }
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            if (!"customerType\tevent\ttext".equals(reader.readLine())) {
                throw new IllegalStateException(RESOURCE + ": invalid header");
            }
            int lineNumber = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split("\t", -1);
                if (fields.length != 3 || fields[2].isBlank()) {
                    throw new IllegalStateException(RESOURCE + ": invalid row " + lineNumber);
                }
                try {
                    pools.get(CustomerType.valueOf(fields[0]))
                        .get(Event.valueOf(fields[1])).add(fields[2]);
                }
                catch (IllegalArgumentException error) {
                    throw new IllegalStateException(RESOURCE + ": invalid type or event on row "
                        + lineNumber, error);
                }
            }
        }
        catch (IOException error) {
            throw new UncheckedIOException("Could not read " + RESOURCE, error);
        }
        for (CustomerType type : CustomerType.values()) {
            for (Event event : Event.values()) {
                if (pools.get(type).get(event).stream().distinct().count() < 5) {
                    throw new IllegalStateException(RESOURCE + ": " + type + " " + event
                        + " needs at least five distinct lines");
                }
            }
        }
    }

    public String draw(CustomerType type, Event event, String requested, String brewed,
        Random random) {
        List<String> pool = pools.get(type).get(event);
        return pool.get(random.nextInt(pool.size())).replace("{requestedPotion}", requested)
            .replace("{brewedPotion}", brewed);
    }
}

package probabilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

final class HistoryStore {
    static final int LIMIT = 100;
    private static final Set<String> TYPES = Set.of("COMPLEMENT", "JOINT", "CONDITIONAL", "BAYES", "BINOMIAL", "POISSON", "NORMAL");
    record Entry(Instant time, CalculationRequest request, String result) { }
    record State(boolean remember, String language, List<Entry> entries) {
        State { entries = List.copyOf(entries); }
    }
    private final Path file;

    HistoryStore(Path file) { this.file = file; }

    State load() throws IOException {
        if (!Files.exists(file)) return new State(true, "DE", List.of());
        if (Files.size(file) > 8 * 1024 * 1024) throw new IOException("History file is too large.");
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) { properties.loadFromXML(input); }
        try {
            if (!"1".equals(properties.getProperty("version"))) throw new IllegalArgumentException("Unsupported history version.");
            String rememberValue = required(properties, "remember", 5);
            if (!Set.of("true", "false").contains(rememberValue)) throw new IllegalArgumentException("Invalid setting.");
            String language = required(properties, "language", 2);
            if (!Set.of("DE", "EN").contains(language)) throw new IllegalArgumentException("Invalid language.");
            int count = boundedInt(properties, "count", LIMIT);
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String prefix = "entry." + i + ".";
                String analysis = required(properties, prefix + "analysis", 20);
                if (!TYPES.contains(analysis)) throw new IllegalArgumentException("Unknown calculation.");
                int fields = boundedInt(properties, prefix + "fields", 16);
                LinkedHashMap<String, String> inputs = new LinkedHashMap<>();
                for (int j = 0; j < fields; j++) {
                    String key = required(properties, prefix + "key." + j, 32);
                    if (inputs.put(key, required(properties, prefix + "value." + j, 8192)) != null) throw new IllegalArgumentException("Duplicate input.");
                }
                entries.add(new Entry(Instant.parse(required(properties, prefix + "time", 40)),
                        new CalculationRequest(analysis, inputs), required(properties, prefix + "result", 32768)));
            }
            boolean remember = Boolean.parseBoolean(rememberValue);
            return new State(remember, language, remember ? entries : List.of());
        } catch (RuntimeException exception) {
            throw new IOException("Invalid history file.", exception);
        }
    }

    void save(State state) throws IOException {
        if (state.entries().size() > LIMIT) throw new IOException("Too many history entries.");
        Properties properties = new Properties();
        properties.setProperty("version", "1");
        properties.setProperty("remember", Boolean.toString(state.remember()));
        properties.setProperty("language", state.language());
        List<Entry> entries = state.remember() ? state.entries() : List.of();
        properties.setProperty("count", Integer.toString(entries.size()));
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            String prefix = "entry." + i + ".";
            properties.setProperty(prefix + "analysis", entry.request().analysis());
            properties.setProperty(prefix + "time", entry.time().toString());
            properties.setProperty(prefix + "result", entry.result());
            properties.setProperty(prefix + "fields", Integer.toString(entry.request().inputs().size()));
            int j = 0;
            for (var field : entry.request().inputs().entrySet()) {
                properties.setProperty(prefix + "key." + j, field.getKey());
                properties.setProperty(prefix + "value." + j++, field.getValue());
            }
        }
        Path directory = file.toAbsolutePath().getParent();
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "history-", ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporary)) {
                properties.storeToXML(output, "Probability Calculator: local history", "UTF-8");
            }
            try { Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException exception) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }

    private static String required(Properties properties, String key, int length) {
        String value = properties.getProperty(key);
        if (value == null || value.length() > length) throw new IllegalArgumentException("Invalid property: " + key);
        return value;
    }

    private static int boundedInt(Properties properties, String key, int max) {
        int value = Integer.parseInt(required(properties, key, 8));
        if (value < 0 || value > max) throw new IllegalArgumentException("Invalid count.");
        return value;
    }
}

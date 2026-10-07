package probabilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

final class HistoryStore {
    static final int RECENT_LIMIT = 100;
    static final int FAVORITE_LIMIT = 100;
    static final int LIMIT = RECENT_LIMIT + FAVORITE_LIMIT;
    private static final Set<String> TYPES = Set.of("COMPLEMENT", "JOINT", "CONDITIONAL", "BAYES", "BINOMIAL", "POISSON", "NORMAL");
    record Entry(String id, Instant time, CalculationRequest request, String result, String name, boolean favorite) {
        Entry(Instant time, CalculationRequest request, String result) {
            this(java.util.UUID.randomUUID().toString(), time, request, result, "", false);
        }
        Entry withName(String name) {
            if (name.length() > 80) throw new ProbabilityException("Der Name darf hoechstens 80 Zeichen lang sein.", "Names must not exceed 80 characters.");
            return new Entry(id, time, request, result, name.strip(), favorite);
        }
        Entry withFavorite(boolean value) { return new Entry(id, time, request, result, name, value); }
    }
    record State(boolean remember, String language, List<Entry> entries) {
        State { entries = List.copyOf(entries); }
    }
    private final Path file;
    private FileChannel sessionChannel;
    private FileLock sessionLock;

    HistoryStore(Path file) { this.file = file; }

    synchronized boolean acquireSessionLock() throws IOException {
        if (sessionLock != null) return true;
        Files.createDirectories(file.toAbsolutePath().getParent());
        FileChannel channel = FileChannel.open(file.resolveSibling("history.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            FileLock lock;
            try { lock = channel.tryLock(); }
            catch (OverlappingFileLockException exception) { lock = null; }
            if (lock == null) { channel.close(); return false; }
            sessionChannel = channel;
            sessionLock = lock;
            return true;
        } catch (IOException exception) { channel.close(); throw exception; }
    }

    synchronized void releaseSessionLock() throws IOException {
        try { if (sessionLock != null) sessionLock.release(); }
        finally {
            sessionLock = null;
            if (sessionChannel != null) sessionChannel.close();
            sessionChannel = null;
        }
    }

    State load() throws IOException {
        if (!Files.exists(file)) return new State(true, "DE", List.of());
        if (Files.size(file) > 8 * 1024 * 1024) throw new IOException("History file is too large.");
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) { properties.loadFromXML(input); }
        try {
            String version = properties.getProperty("version");
            if (!Set.of("1", "2").contains(version)) throw new IllegalArgumentException("Unsupported history version.");
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
                String id = version.equals("1") ? java.util.UUID.randomUUID().toString() : required(properties, prefix + "id", 36);
                java.util.UUID.fromString(id);
                String name = version.equals("1") ? "" : required(properties, prefix + "name", 80);
                String favorite = version.equals("1") ? "false" : required(properties, prefix + "favorite", 5);
                if (!Set.of("true", "false").contains(favorite)) throw new IllegalArgumentException("Invalid favorite.");
                entries.add(new Entry(id, Instant.parse(required(properties, prefix + "time", 40)),
                        new CalculationRequest(analysis, inputs), required(properties, prefix + "result", 32768), name, Boolean.parseBoolean(favorite)));
            }
            if (entries.stream().map(Entry::id).distinct().count() != entries.size()) throw new IllegalArgumentException("Duplicate history ID.");
            validateEntries(entries);
            boolean remember = Boolean.parseBoolean(rememberValue);
            return new State(remember, language, remember ? entries : List.of());
        } catch (RuntimeException exception) {
            throw new IOException("Invalid history file.", exception);
        }
    }

    synchronized void save(State state) throws IOException {
        boolean temporaryLock = sessionLock == null;
        if (temporaryLock && !acquireSessionLock()) throw new IOException("History is in use by another instance.");
        try { write(state); }
        finally { if (temporaryLock) releaseSessionLock(); }
    }

    private void write(State state) throws IOException {
        validateEntries(state.entries());
        Properties properties = new Properties();
        properties.setProperty("version", "2");
        properties.setProperty("remember", Boolean.toString(state.remember()));
        properties.setProperty("language", state.language());
        List<Entry> entries = state.remember() ? state.entries() : List.of();
        properties.setProperty("count", Integer.toString(entries.size()));
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            String prefix = "entry." + i + ".";
            properties.setProperty(prefix + "id", entry.id());
            properties.setProperty(prefix + "name", entry.name());
            properties.setProperty(prefix + "favorite", Boolean.toString(entry.favorite()));
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

    static void trim(List<Entry> entries) {
        long recent = entries.stream().filter(entry -> !entry.favorite()).count();
        for (int i = entries.size() - 1; recent > RECENT_LIMIT && i >= 0; i--) {
            if (!entries.get(i).favorite()) { entries.remove(i); recent--; }
        }
    }

    private static void validateEntries(List<Entry> entries) throws IOException {
        long favorites = entries.stream().filter(Entry::favorite).count();
        if (favorites > FAVORITE_LIMIT || entries.size() - favorites > RECENT_LIMIT) throw new IOException("Too many history entries.");
        if (entries.stream().map(Entry::id).distinct().count() != entries.size()) throw new IOException("Duplicate history ID.");
        for (Entry entry : entries) {
            if (entry.name().length() > 80 || entry.result().length() > 32768) throw new IOException("History text is too long.");
        }
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

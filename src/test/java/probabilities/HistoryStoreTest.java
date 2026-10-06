package probabilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class HistoryStoreTest {
    @TempDir Path directory;
    private HistoryStore.Entry entry() {
        return new HistoryStore.Entry(Instant.parse("2026-10-06T12:00:00Z"),
                new CalculationRequest("JOINT", Map.of("a", "0,30", "b", "0.40", "intersection", "")), "Pr(A or B): 0.58\n");
    }

    @Test void preservesInputsResultsLanguageAndTimestampAcrossRestart() throws IOException {
        HistoryStore store = new HistoryStore(directory.resolve("nested/history.xml"));
        var state = new HistoryStore.State(true, "EN", List.of(entry()));
        store.save(state);
        assertEquals(state, store.load());
    }

    @Test void disablingPersistenceRemovesPreviouslyStoredCalculations() throws IOException {
        HistoryStore store = new HistoryStore(directory.resolve("history.xml"));
        store.save(new HistoryStore.State(true, "DE", List.of(entry())));
        store.save(new HistoryStore.State(false, "EN", List.of(entry())));
        assertEquals(new HistoryStore.State(false, "EN", List.of()), store.load());
        assertFalse(Files.readString(directory.resolve("history.xml")).contains("Pr(A or B)"));
    }

    @Test void rejectsCorruptionAndPreservesTheFile() throws IOException {
        Path file = directory.resolve("history.xml");
        Files.writeString(file, "broken xml");
        assertThrows(IOException.class, () -> new HistoryStore(file).load());
        assertEquals("broken xml", Files.readString(file));
    }

    @Test void rejectsInvalidTimestampAsRecoverableIoFailure() throws IOException {
        Path file = directory.resolve("history.xml");
        HistoryStore store = new HistoryStore(file);
        store.save(new HistoryStore.State(true, "DE", List.of(entry())));
        Properties properties = new Properties();
        try (var input = Files.newInputStream(file)) { properties.loadFromXML(input); }
        properties.setProperty("entry.0.time", "invalid timestamp");
        try (var output = Files.newOutputStream(file)) { properties.storeToXML(output, "Test"); }
        assertThrows(IOException.class, store::load);
    }

    @Test void capsStoredEntriesAndStartsWithEmptyHistory() throws IOException {
        HistoryStore store = new HistoryStore(directory.resolve("history.xml"));
        assertEquals(List.of(), store.load().entries());
        assertThrows(IOException.class, () -> store.save(new HistoryStore.State(true, "DE", java.util.Collections.nCopies(101, entry()))));
        store.save(new HistoryStore.State(true, "DE", java.util.Collections.nCopies(100, entry())));
        assertEquals(100, store.load().entries().size());
    }
}

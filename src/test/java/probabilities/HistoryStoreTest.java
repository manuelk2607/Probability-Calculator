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
        store.save(new HistoryStore.State(true, "DE", java.util.stream.IntStream.range(0, 100).mapToObj(i -> entry()).toList()));
        assertEquals(100, store.load().entries().size());
    }

    @Test void preservesNamedFavoritesAndTrimsOnlyOldRecentEntries() throws IOException {
        var favorite = entry().withName("Test, \"A\"\nB").withFavorite(true);
        var entries = new java.util.ArrayList<HistoryStore.Entry>();
        for (int i = 0; i < 110; i++) entries.add(entry());
        entries.add(favorite);
        HistoryStore.trim(entries);
        assertEquals(101, entries.size());
        assertEquals(favorite, entries.get(100));
        HistoryStore store = new HistoryStore(directory.resolve("history.xml"));
        var state = new HistoryStore.State(true, "EN", entries);
        store.save(state);
        assertEquals(state, store.load());
        assertThrows(IllegalArgumentException.class, () -> favorite.withName("x".repeat(81)));
    }

    @Test void preventsASecondInstanceFromOverwritingHistory() throws IOException {
        Path file = directory.resolve("history.xml");
        HistoryStore first = new HistoryStore(file), second = new HistoryStore(file);
        assertTrue(first.acquireSessionLock());
        try {
            first.save(new HistoryStore.State(true, "EN", List.of(entry())));
            String original = Files.readString(file);
            assertFalse(second.acquireSessionLock());
            assertThrows(IOException.class, () -> second.save(new HistoryStore.State(false, "DE", List.of())));
            assertEquals(original, Files.readString(file));
            assertEquals(1, second.load().entries().size());
        } finally { first.releaseSessionLock(); }
        assertTrue(second.acquireSessionLock());
        second.releaseSessionLock();
    }

    @Test void migratesVersionOneWithoutLosingCalculations() throws IOException {
        Path file = directory.resolve("history.xml");
        HistoryStore store = new HistoryStore(file);
        HistoryStore.Entry entry = entry();
        store.save(new HistoryStore.State(true, "DE", List.of(entry)));
        Properties properties = new Properties();
        try (var input = Files.newInputStream(file)) { properties.loadFromXML(input); }
        properties.setProperty("version", "1");
        properties.remove("entry.0.id"); properties.remove("entry.0.name"); properties.remove("entry.0.favorite");
        try (var output = Files.newOutputStream(file)) { properties.storeToXML(output, "Legacy history"); }
        var loaded = store.load();
        assertEquals(entry.request(), loaded.entries().get(0).request());
        assertEquals(entry.result(), loaded.entries().get(0).result());
        assertFalse(loaded.entries().get(0).favorite());
        store.save(loaded);
        assertEquals(loaded, store.load());
    }

    @Test void rejectsTooManyFavoritesAndPreservesExistingData() throws IOException {
        HistoryStore store = new HistoryStore(directory.resolve("history.xml"));
        var original = new HistoryStore.State(true, "DE", List.of(entry()));
        store.save(original);
        var entries = java.util.stream.IntStream.range(0, 101).mapToObj(i -> entry().withFavorite(true)).toList();
        assertThrows(IOException.class, () -> store.save(new HistoryStore.State(true, "DE", entries)));
        assertEquals(original, store.load());
    }
}

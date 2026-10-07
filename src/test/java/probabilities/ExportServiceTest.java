package probabilities;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import java.io.StringReader;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ExportServiceTest {
    private List<CSVRecord> parse(String csv) throws Exception {
        try (var parser = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get().parse(new StringReader(csv))) {
            return parser.getRecords();
        }
    }

    @Test void historyExportRoundTripsQuotesCommasAndMultilineText() throws Exception {
        var request = new CalculationRequest("COMPLEMENT", Map.of("a", "0,25", "b", "0.5"));
        var entry = new HistoryStore.Entry(Instant.parse("2026-10-07T10:00:00Z"), request, "A: 0,25\nB: \"0.5\"")
                .withName("Probe, \"Test\"").withFavorite(true);
        CSVRecord row = parse(ExportService.historyCsv(List.of(entry))).get(0);
        assertEquals(entry.name(), row.get("name"));
        assertEquals(entry.result(), row.get("result"));
        assertEquals("true", row.get("favorite"));
        assertTrue(row.get("inputs").contains("a=0,25"));
        assertEquals(entry.time().toString(), row.get("time"));
    }

    @Test void exportsProbabilitiesAndScalarsWithCorrectUnits() throws Exception {
        var request = new CalculationRequest("NORMAL", Map.of("mean", "0", "sd", "1", "lower", "-1", "upper", "1"));
        var result = CalculationService.calculate(request);
        var rows = parse(ExportService.resultsCsv(request, result.values()));
        assertEquals(4, rows.size());
        assertEquals(result.values().get(1).value(), Double.parseDouble(rows.get(1).get("value")), 0);
        assertEquals(100 * result.values().get(1).value(), Double.parseDouble(rows.get(1).get("percentage")), 0);
        assertEquals("", rows.get(3).get("percentage"));
    }

    @Test void neutralizesSpreadsheetFormulasInUserText() throws Exception {
        var request = new CalculationRequest("COMPLEMENT", Map.of("a", "0.25", "b", "0.5"));
        for (String name : List.of("=1+1", "+1", "-1", "@SUM(A1)", " \t=1+1")) {
            var entry = new HistoryStore.Entry(Instant.now(), request, "=1+1").withName(name);
            var row = parse(ExportService.historyCsv(List.of(entry))).get(0);
            assertTrue(row.get("name").startsWith("'"));
            assertEquals("'=1+1", row.get("result"));
        }
    }
}

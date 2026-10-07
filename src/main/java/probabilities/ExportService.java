package probabilities;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

final class ExportService {
    private ExportService() { }

    static String historyCsv(List<HistoryStore.Entry> entries) throws IOException {
        StringWriter text = new StringWriter();
        try (CSVPrinter csv = printer(text, "time", "name", "favorite", "analysis", "inputs", "result")) {
            for (HistoryStore.Entry entry : entries) {
                csv.printRecord(entry.time(), safeCell(entry.name()), entry.favorite(), entry.request().analysis(),
                        safeCell(inputs(entry.request())), safeCell(entry.result()));
            }
        }
        return text.toString();
    }

    static String resultsCsv(CalculationRequest request, List<CalculationService.Value> values) throws IOException {
        StringWriter text = new StringWriter();
        try (CSVPrinter csv = printer(text, "analysis", "inputs", "result", "value", "percentage")) {
            for (CalculationService.Value value : values) {
                csv.printRecord(request.analysis(), safeCell(inputs(request)), safeCell(value.label()),
                        Double.toString(value.value()), value.probability() ? Double.toString(100 * value.value()) : "");
            }
        }
        return text.toString();
    }

    private static CSVPrinter printer(StringWriter text, String... headers) throws IOException {
        return new CSVPrinter(text, CSVFormat.RFC4180.builder().setHeader(headers).setQuoteMode(QuoteMode.ALL).get());
    }

    private static String inputs(CalculationRequest request) {
        return request.inputs().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("; "));
    }

    private static String safeCell(String value) {
        String stripped = value.stripLeading();
        return !stripped.isEmpty() && "=+-@".indexOf(stripped.charAt(0)) >= 0 ? "'" + value : value;
    }
}

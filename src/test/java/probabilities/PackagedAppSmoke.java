package probabilities;

import java.util.Map;

/** Exercises the actual shaded JAR with the runtime bundled by jpackage. */
public final class PackagedAppSmoke {
    public static void main(String[] args) throws Exception {
        var request = new CalculationRequest("NORMAL", Map.of("mean", "0", "sd", "1", "lower", "-1", "upper", "1"));
        var result = CalculationService.calculate(request);
        if (Math.abs(result.values().get(1).value() - 0.6826894921370859) > 1e-14) throw new AssertionError("Normal calculation failed");
        if (!ExportService.resultsCsv(request, result.values()).contains("percentage")) throw new AssertionError("CSV dependencies missing");
        if (JointProbs.getProbAOrB(0.5, 0.6, 0.1) != 1) throw new AssertionError("Joint calculation failed");
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setSize(100, 100);
        System.out.println("Bundled runtime, Swing, statistics and CSV smoke tests passed.");
    }
}

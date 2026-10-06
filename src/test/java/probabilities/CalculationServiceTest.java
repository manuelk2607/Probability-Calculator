package probabilities;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CalculationServiceTest {
    private CalculationService.Result calculate(String analysis, Map<String, String> inputs) {
        return CalculationService.calculate(new CalculationRequest(analysis, inputs));
    }

    @Test void rejectsImpossibleConditionalIntersectionIncludingLowerBound() {
        assertThrows(IllegalArgumentException.class, () -> calculate("CONDITIONAL", Map.of("a", "0.2", "b", "0.3", "intersection", "0.4")));
        assertThrows(IllegalArgumentException.class, () -> calculate("CONDITIONAL", Map.of("a", "0.8", "b", "0.8", "intersection", "0.1")));
    }

    @Test void bayesChartIncludesTheComplementInsteadOfStretchingLastContribution() {
        var result = calculate("BAYES", Map.of("likelihoods", "0,9;0,2", "priors", "0,3;0,7", "index", "1"));
        assertEquals(0.27, result.shares().get(0).value(), 1e-12);
        assertEquals(0.14, result.shares().get(1).value(), 1e-12);
        assertEquals(0.59, result.shares().get(2).value(), 1e-12);
        assertEquals(1, result.shares().stream().mapToDouble(CalculationService.Share::value).sum(), 1e-12);
    }

    @Test void scalarResultsHaveNoPercentUnitAndTailsAreAvailable() {
        var result = calculate("BINOMIAL", Map.of("n", "10", "k", "3", "lower", "0", "upper", "3", "p", "0.5"));
        assertEquals(0.828125, result.values().stream().filter(value -> value.label().equals("Pr(X > k)")).findFirst().orElseThrow().value(), 1e-12);
        assertTrue(result.values().stream().filter(value -> !value.probability()).allMatch(value -> value.label().equals("E(X)") || value.label().equals("Var(X)") || value.label().equals("SD(X)")));
        assertEquals(3, result.values().stream().filter(value -> !value.probability()).count());
    }

    @Test void handlesLargeDistributionsWithBoundedCharts() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            var binomial = calculate("BINOMIAL", Map.of("n", "1000000000", "k", "500000000", "lower", "499990000", "upper", "500010000", "p", "0.5"));
            var poisson = calculate("POISSON", Map.of("lambda", "1000000000", "k", "1000000000", "lower", "999990000", "upper", "1000010000"));
            for (var result : new CalculationService.Result[]{binomial, poisson}) {
                assertTrue(result.points().size() <= 60);
                assertTrue(result.points().stream().mapToDouble(CalculationService.Point::value).sum() > 0.999);
            }
        });
    }

    @Test void supportsDegenerateBinomialAndCommaDecimals() {
        var result = calculate("BINOMIAL", Map.of("n", "0", "k", "0", "lower", "0", "upper", "0", "p", "0,5"));
        assertEquals(1, result.values().get(0).value(), 0);
        assertEquals(1, result.points().size());
        assertEquals(1, result.points().get(0).value(), 0);
    }

    @Test void rejectsMalformedNumbersAndInvalidBayesParameters() {
        assertThrows(ProbabilityException.class, () -> calculate("NORMAL", Map.of("mean", "NaN", "sd", "1", "lower", "0", "upper", "1")));
        assertThrows(ProbabilityException.class, () -> calculate("BAYES", Map.of("likelihoods", "0.9;", "priors", "1", "index", "1")));
        assertThrows(ProbabilityException.class, () -> calculate("BAYES", Map.of("likelihoods", "0;0", "priors", "0.5;0.5", "index", "1")));
    }
}

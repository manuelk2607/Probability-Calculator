package probabilities;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class NumericalBoundaryTest {
    @Test void acceptsDecimalRoundingAtFrechetBoundsWithoutAcceptingImpossibleIntersections() {
        assertEquals(1, JointProbs.getProbAOrB(0.5, 0.6, 0.1), 0);
        assertEquals(1, JointProbs.getProbNotAOrB(0.5, 0.6, 0.1), 1e-15);
        assertEquals(0.6, JointProbs.getProbAOrB(0.5, 0.6, Math.nextUp(0.5)), 1e-15);
        assertThrows(IllegalArgumentException.class, () -> JointProbs.getProbAOrB(0.5, 0.6, 0.099999));
        assertThrows(IllegalArgumentException.class, () -> JointProbs.getProbAOrB(0.5, 0.6, 0.500001));
    }

    @Test void normalizesAcceptedPriorRoundingInOutputsAndChart() {
        assertEquals(1, TotalBayesProbs.totalProbability(List.of(1.0, 1.0), List.of(0.5000000001, 0.5)), 0);
        var request = new CalculationRequest("BAYES", Map.of("likelihoods", "1;1", "priors", "0.5000000001;0.5", "index", "1"));
        var result = CalculationService.calculate(request);
        assertEquals(1, result.values().get(0).value(), 0);
        assertEquals(result.values().get(2).value(), result.values().get(1).value(), 1e-15);
        assertEquals(1, result.shares().stream().mapToDouble(CalculationService.Share::value).sum(), 1e-15);
        assertThrows(IllegalArgumentException.class, () -> TotalBayesProbs.totalProbability(List.of(1.0, 1.0), List.of(0.50001, 0.5)));
    }

    @Test void maintainsStrictRawInputValidationAndRelativeTinyDenominatorChecks() {
        assertThrows(IllegalArgumentException.class, () -> BaseProbabilities.getProbNotA(-1e-16));
        assertThrows(IllegalArgumentException.class, () -> ConditionalProbs.getProbAGivenB(2e-300, 1e-300));
        assertEquals(1, ConditionalProbs.getProbAGivenB(Math.nextUp(1e-300), 1e-300), 0);
    }

    @Test void avoidsOverflowWhenStandardizingExtremeFiniteNormalValues() {
        double expected = ProbabilityDistributions.normalSurvival(0, 1, 2);
        assertEquals(expected, ProbabilityDistributions.normalSurvival(-1e308, 1e308, 1e308), 1e-16);
        assertEquals(expected, ProbabilityDistributions.normalCumulative(1e308, 1e308, -1e308), 1e-16);
        assertEquals(0, ProbabilityDistributions.normalDensity(-1e308, 1, 1e308), 0);
        assertEquals(1 - 2 * expected, ProbabilityDistributions.normalInterval(0, 5e307, -1e308, 1e308), 1e-15);
    }
}

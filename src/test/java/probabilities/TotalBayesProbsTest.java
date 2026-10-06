package probabilities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static probabilities.TotalBayesProbs.calcBayes;
import static probabilities.TotalBayesProbs.totalProbability;
import static org.junit.jupiter.api.Assertions.*;

class TotalBayesProbsTest {
    @Test
    void calculatesTotalProbability() {
        double result = totalProbability(List.of(0.9, 0.2), List.of(0.3, 0.7));
        assertEquals(0.41, result, 0.0001);
    }

    @Test
    void calculatesBayes() {
        double probB = totalProbability(List.of(0.9, 0.2), List.of(0.3, 0.7));
        assertEquals(0.6585, calcBayes(0.9, 0.3, probB), 0.0001);
    }

    @Test
    void rejectsInvalidPriorDistribution() {
        assertThrows(IllegalArgumentException.class, () -> totalProbability(List.of(0.9, 0.2), List.of(0.3, 0.6)));
    }

    @Test void rejectsNullEntriesAndInconsistentBayesMarginals() {
        assertThrows(IllegalArgumentException.class, () -> totalProbability(java.util.Arrays.asList(0.9, null), List.of(0.3, 0.7)));
        assertThrows(IllegalArgumentException.class, () -> calcBayes(0.9, 0.8, 0.1));
    }
}

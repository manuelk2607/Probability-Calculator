package probabilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static probabilities.ProbabilityDistributions.*;

class ProbabilityDistributionsTest {
    @Test
    void calculatesBinomialProbabilities() {
        assertEquals(0.1171875, binomialProbability(10, 3, 0.5), 0.000001);
        assertEquals(0.3125, binomialCumulative(4, 1, 0.5), 0.000001);
        assertEquals(0.875, binomialInterval(4, 1, 3, 0.5), 0.000001);
    }

    @Test
    void calculatesPoissonProbabilities() {
        assertEquals(0.224042, poissonProbability(3.0, 2), 0.000001);
        assertEquals(0.423190, poissonCumulative(3.0, 2), 0.000001);
        assertEquals(0.616115, poissonInterval(3.0, 2, 4), 0.000001);
    }

    @Test
    void calculatesNormalProbabilities() {
        assertEquals(0.5, normalCumulative(0.0, 1.0, 0.0), 0.000001);
        assertEquals(0.682689, normalInterval(0.0, 1.0, -1.0, 1.0), 0.0002);
        assertEquals(0.398942, normalDensity(0.0, 1.0, 0.0), 0.000001);
    }

    @Test
    void rejectsInvalidDistributionParameters() {
        assertThrows(IllegalArgumentException.class, () -> binomialProbability(3, 4, 0.5));
        assertThrows(IllegalArgumentException.class, () -> poissonProbability(0.0, 1));
        assertThrows(IllegalArgumentException.class, () -> normalCumulative(0.0, 0.0, 1.0));
    }

    @Test void preservesTinyTailAndIntervalProbabilities() {
        assertEquals(6.220960574271784e-16, normalSurvival(0, 1, 8), 1e-29);
        assertEquals(6.21983198586583e-16, normalInterval(0, 1, 8, 9), 1e-28);
        assertEquals(Math.pow(0.5, 100), binomialInterval(100, 100, 100, 0.5), 1e-42);
    }

    @Test void validatesFiniteNormalParametersAndBinomialEdges() {
        assertThrows(IllegalArgumentException.class, () -> normalCumulative(Double.NaN, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> normalDensity(0, 1, Double.POSITIVE_INFINITY));
        assertEquals(1, binomialProbability(1000000000, 0, 0), 0);
        assertEquals(0, binomialProbability(1000000000, 1, 0), 0);
        assertEquals(1, binomialProbability(Integer.MAX_VALUE, Integer.MAX_VALUE, 1), 0);
    }
}

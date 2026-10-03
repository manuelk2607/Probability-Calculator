package probabilities;

import org.junit.jupiter.api.Test;

import static probabilities.ConditionalProbs.*;
import static org.junit.jupiter.api.Assertions.*;

class ConditionalProbsTest {
    @Test
    void calculatesConditionalProbabilitiesFromIntersection() {
        assertAll(
                () -> assertEquals(0.3, getProbAGivenB(0.12, 0.4), 0.0001),
                () -> assertEquals(0.4, getProbBGivenA(0.12, 0.3), 0.0001),
                () -> assertEquals(0.7, getProbNotAGivenB(0.28, 0.4), 0.0001),
                () -> assertEquals(0.6, getProbNotBGivenA(0.18, 0.3), 0.0001)
        );
    }

    @Test
    void calculatesConditionalProbabilitiesWithBayesTransform() {
        assertAll(
                () -> assertEquals(0.3, getProbAGivenB(0.4, 0.3, 0.4), 0.0001),
                () -> assertEquals(0.4, getProbBGivenA(0.3, 0.3, 0.4), 0.0001),
                () -> assertEquals(0.7, getProbNotAGivenB(0.4, 0.7, 0.4), 0.0001),
                () -> assertEquals(0.6, getProbAGivenNotB(0.6, 0.3, 0.3), 0.0001)
        );
    }

    @Test
    void rejectsZeroDenominator() {
        assertThrows(IllegalArgumentException.class, () -> getProbAGivenB(0.1, 0.0));
    }
}

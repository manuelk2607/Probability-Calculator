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

    @Test void rejectsProbabilitiesGreaterThanOneForEveryConditionalVariant() {
        assertThrows(IllegalArgumentException.class, () -> getProbAGivenB(0.6, 0.3));
        assertThrows(IllegalArgumentException.class, () -> getProbBGivenA(0.6, 0.3));
        assertThrows(IllegalArgumentException.class, () -> getProbNotAGivenB(0.6, 0.3));
        assertThrows(IllegalArgumentException.class, () -> getProbNotBGivenA(0.6, 0.3));
        assertThrows(IllegalArgumentException.class, () -> getProbAGivenNotB(0.6, 0.3));
        assertThrows(IllegalArgumentException.class, () -> getProbBGivenNotA(0.6, 0.3));
        assertThrows(IllegalArgumentException.class, () -> getProbAGivenB(0.9, 0.8, 0.1));
    }
}

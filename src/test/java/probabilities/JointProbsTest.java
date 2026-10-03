package probabilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static probabilities.JointProbs.*;

class JointProbsTest {
    double probA = 0.3;
    double probB = 0.4;
    double probAAndB = getProbAAndB(probA, probB);
    double probAOrB = getProbAOrB(probA, probB, probAAndB);

    @Test
    void testAnds() {
        assertAll(
                () -> assertEquals(0.12, getProbAAndB(probA, probB), "Sollte P (A ∩ B) sein"),
                () -> assertEquals(0.28, getProbNotAAndB(probA, probB), 0.001, "Sollte (1 - P(A)) * P(B) sein"),
                () -> assertEquals(0.18, getProbAAndNotB(probA, probB)),
                () -> assertEquals(0.42, getProbNotAAndNotB(probA, probB))
        );
    }

    @Test
    void testOrs() {
        double probNotAAndB = getProbNotAAndB(probA, probB);
        double probAAndNotB = getProbAAndNotB(probA, probB);
        assertAll(
                () -> assertEquals(0.58, getProbAOrB(probA, probB, probAAndB), "Sollte P (A ∪ B) sein"),
                () -> assertEquals(0.82, getProbNotAOrB(probA, probB, probNotAAndB), 0.01, "Sollte P(Ā ∪ B) sein"),
                () -> assertEquals(0.72, getProbAOrNotB(probA, probB, probAAndNotB), 0.01, "Sollte P(A ∪ B̄) sein"),
                () -> assertEquals(0.88, getProbNotAOrNotB(probAAndB))
        );
    }

    @Test
    void testComplements() {
        assertAll(
                () -> assertEquals(0.88, getProbNotAAndBComplement(probAAndB)),
                () -> assertEquals(0.42, getProbNotAOrBComplement(probAOrB), 0.0001, "Sollte 1 - P(A ∪ B) sein")
        );
    }

    @Test
    void testMinus() {
        assertAll(
                () -> assertEquals(0.18, getProbAMinusB(probA, probAAndB)),
                () -> assertEquals(0.28, getProbBMinusA(probB, probAAndB))
        );
    }

    @Test
    void rejectsImpossibleIntersection() {
        assertThrows(IllegalArgumentException.class, () -> getProbAOrB(0.2, 0.3, 0.4));
    }
}

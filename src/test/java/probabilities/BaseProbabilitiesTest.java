package probabilities;

import org.junit.jupiter.api.Test;

import static probabilities.BaseProbabilities.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseProbabilitiesTest {
    double probA = 0.3;
    double probNotA = 0.4;

    double probB = 0.4;
    double probNotB = 0.2;

    @Test
    void testA() {
        assertAll(
                () -> assertEquals(0.3, probA, "Sollte P (A) sein"),
                () -> assertEquals(0.7, getProbNotA(probA), "Sollte 1 - P (A) sein"),
                () -> assertEquals(0.6, getProbAFromNotA(probNotA), "Sollte 1 - P (A) sein")
        );
    }

    @Test
    void testB() {
        assertAll(
                () -> assertEquals(0.4, probB, "Sollte P (A) sein"),
                () -> assertEquals(0.6, getProbNotB(probB), "Sollte 1 - P (A) sein"),
                () -> assertEquals(0.8, getProbBFromNotB(probNotB), "Sollte 1 - P (A) sein")
        );
    }
}
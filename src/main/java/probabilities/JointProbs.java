package probabilities;

/**
 * Berechnet zusammengesetzte Wahrscheinlichkeiten
 *
 */

public class JointProbs extends BaseProbabilities {
    /**
     * Berechnet P(A und B) unter der Annahme, dass A und B unabhängig sind.
     */
    public static double getProbAAndB(double probA, double probB) {
        ProbabilityUtils.requireProbability(probA, "P(A)");
        ProbabilityUtils.requireProbability(probB, "P(B)");
        return probA * probB;
    }

    /**
     * Berechnet P(nicht A und B) unter der Annahme, dass A und B unabhängig sind.
     */
    public static double getProbNotAAndB(double probA, double probB) {
        ProbabilityUtils.requireProbability(probA, "P(A)");
        ProbabilityUtils.requireProbability(probB, "P(B)");
        return (1 - probA) * probB;
    }

    /**
     * Berechnet P(A und nicht B) unter der Annahme, dass A und B unabhängig sind.
     */
    public static double getProbAAndNotB(double probA, double probB) {
        ProbabilityUtils.requireProbability(probA, "P(A)");
        ProbabilityUtils.requireProbability(probB, "P(B)");
        return probA * (1 - probB);
    }

    /**
     * Berechnet P(nicht A und nicht B) unter der Annahme, dass A und B unabhängig sind.
     */
    public static double getProbNotAAndNotB(double probA, double probB) {
        ProbabilityUtils.requireProbability(probA, "P(A)");
        ProbabilityUtils.requireProbability(probB, "P(B)");
        return (1 - probA) * (1 - probB);
    }


    public static double getProbAOrB(double probA, double probB, double probAAndB) {
        ProbabilityUtils.requireProbability(probA, "P(A)");
        ProbabilityUtils.requireProbability(probB, "P(B)");
        probAAndB = ProbabilityUtils.requirePossibleIntersection(probAAndB, probA, probB, "P(A und B)");
        return ProbabilityUtils.computedProbability(probA + probB - probAAndB);
    }

    public static double getProbNotAOrB(double probA, double probB, double probNotAAndB) {
        double probNotA = getProbNotA(probA);
        ProbabilityUtils.requireProbability(probB, "P(B)");
        probNotAAndB = ProbabilityUtils.requirePossibleIntersection(probNotAAndB, probNotA, probB, "P(nicht A und B)");
        return ProbabilityUtils.computedProbability((1 - probA) + probB - probNotAAndB);
    }

    public static double getProbAOrNotB(double probA, double probB, double probAAndNotB) {
        double probNotB = getProbNotB(probB);
        ProbabilityUtils.requireProbability(probA, "P(A)");
        probAAndNotB = ProbabilityUtils.requirePossibleIntersection(probAAndNotB, probA, probNotB, "P(A und nicht B)");
        return ProbabilityUtils.computedProbability(probA + (1 - probB) - probAAndNotB);
    }

    public static double getProbNotAOrNotB(double probAAndB) {
        ProbabilityUtils.requireProbability(probAAndB, "P(A und B)");
        return 1 - probAAndB;
    }


    public static double getProbNotAAndBComplement(double probAAndB) {
        ProbabilityUtils.requireProbability(probAAndB, "P(A und B)");
        return 1 - probAAndB;
    }

    public static double getProbNotAOrBComplement(double probAOrB) {
        ProbabilityUtils.requireProbability(probAOrB, "P(A oder B)");
        return 1 - probAOrB;
    }


    public static double getProbAMinusB(double probA, double probAAndB) {
        ProbabilityUtils.requireProbability(probA, "P(A)");
        ProbabilityUtils.requireProbability(probAAndB, "P(A und B)");
        if (probAAndB > probA) {
            throw new IllegalArgumentException("P(A und B) darf nicht groesser als P(A) sein.");
        }
        return probA - probAAndB;
    }

    public static double getProbBMinusA(double probB, double probAAndB) {
        ProbabilityUtils.requireProbability(probB, "P(B)");
        ProbabilityUtils.requireProbability(probAAndB, "P(A und B)");
        if (probAAndB > probB) {
            throw new IllegalArgumentException("P(A und B) darf nicht groesser als P(B) sein.");
        }
        return probB - probAAndB;
    }

}

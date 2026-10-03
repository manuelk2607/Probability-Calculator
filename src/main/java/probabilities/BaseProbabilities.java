package probabilities;

public class BaseProbabilities {
    /**
     * Berechnet die negierte Wahrscheinlichkeit P(Ā)
     * @param probA P(A)
     * @return 1 - P(A)
     */
    public static double getProbNotA(double probA) {
        ProbabilityUtils.requireProbability(probA, "P(A)");
        return 1.0 - probA;
    }

    /**
     * Berechnet die negierte Wahrscheinlichkeit P(B̄)
     * @param probB P(A)
     * @return 1 - P(B)
     */
    public static double getProbNotB(double probB) {
        ProbabilityUtils.requireProbability(probB, "P(B)");
        return 1.0 - probB;
    }

    /**
     * Berechnet P(A) aus der negierten Wahrscheinlichkeit P(Ā)
     * @param probNotA P(Ā)
     * @return 1 - P(Ā)
     */
    public static double getProbAFromNotA(double probNotA) {
        ProbabilityUtils.requireProbability(probNotA, "P(nicht A)");
        return 1.0 - probNotA;
    }

    /**
     * Berechnet P(B) aus der negierten Wahrscheinlichkeit P(B̄)
     * @param probNotB P(B̄)
     * @return 1 - P(B̄)
     */
    public static double getProbBFromNotB(double probNotB) {
        ProbabilityUtils.requireProbability(probNotB, "P(nicht B)");
        return 1.0 - probNotB;
    }
}

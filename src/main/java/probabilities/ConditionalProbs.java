package probabilities;

public class ConditionalProbs extends BaseProbabilities {
    public static double getProbAGivenB(double probAAndB, double probB) {
        ProbabilityUtils.requirePositiveProbability(probB, "P(B)");
        ProbabilityUtils.requireProbability(probAAndB, "P(A und B)");
        return ProbabilityUtils.conditionalRatio(probAAndB, probB);
    }

    public static double getProbAGivenB(double probBGivenA, double probA, double probB) {
        ProbabilityUtils.requireProbability(probBGivenA, "P(B|A)");
        ProbabilityUtils.requireProbability(probA, "P(A)");
        ProbabilityUtils.requirePositiveProbability(probB, "P(B)");
        ProbabilityUtils.requirePossibleIntersection(probBGivenA * probA, probA, probB, "P(A and B)");
        return ProbabilityUtils.conditionalRatio(probBGivenA * probA, probB);
    }

    public static double getProbBGivenA(double probAAndB, double probA) {
        ProbabilityUtils.requirePositiveProbability(probA, "P(A)");
        ProbabilityUtils.requireProbability(probAAndB, "P(A und B)");
        return ProbabilityUtils.conditionalRatio(probAAndB, probA);
    }

    public static double getProbBGivenA(double probAGivenB, double probA, double probB) {
        ProbabilityUtils.requireProbability(probAGivenB, "P(A|B)");
        ProbabilityUtils.requirePositiveProbability(probA, "P(A)");
        ProbabilityUtils.requireProbability(probB, "P(B)");
        ProbabilityUtils.requirePossibleIntersection(probAGivenB * probB, probA, probB, "P(A and B)");
        return ProbabilityUtils.conditionalRatio(probAGivenB * probB, probA);
    }

    public static double getProbNotAGivenB(double probNotAAndB, double probB) {
        ProbabilityUtils.requirePositiveProbability(probB, "P(B)");
        ProbabilityUtils.requireProbability(probNotAAndB, "P(nicht A und B)");
        return ProbabilityUtils.conditionalRatio(probNotAAndB, probB);
    }

    public static double getProbNotAGivenB(double probBGivenNotA, double probNotA, double probB) {
        ProbabilityUtils.requireProbability(probBGivenNotA, "P(B|nicht A)");
        ProbabilityUtils.requireProbability(probNotA, "P(nicht A)");
        ProbabilityUtils.requirePositiveProbability(probB, "P(B)");
        ProbabilityUtils.requirePossibleIntersection(probBGivenNotA * probNotA, probNotA, probB, "P(not A and B)");
        return ProbabilityUtils.conditionalRatio(probBGivenNotA * probNotA, probB);
    }

    public static double getProbNotBGivenA(double probAAndNotB, double probA) {
        ProbabilityUtils.requirePositiveProbability(probA, "P(A)");
        ProbabilityUtils.requireProbability(probAAndNotB, "P(A und nicht B)");
        return ProbabilityUtils.conditionalRatio(probAAndNotB, probA);
    }

    public static double getProbNotBGivenA(double probAGivenNotB, double probNotB, double probA) {
        ProbabilityUtils.requireProbability(probAGivenNotB, "P(A|nicht B)");
        ProbabilityUtils.requireProbability(probNotB, "P(nicht B)");
        ProbabilityUtils.requirePositiveProbability(probA, "P(A)");
        ProbabilityUtils.requirePossibleIntersection(probAGivenNotB * probNotB, probNotB, probA, "P(A and not B)");
        return ProbabilityUtils.conditionalRatio(probAGivenNotB * probNotB, probA);
    }

    public static double getProbAGivenNotB(double probAAndNotB, double probNotB) {
        ProbabilityUtils.requirePositiveProbability(probNotB, "P(nicht B)");
        ProbabilityUtils.requireProbability(probAAndNotB, "P(A und nicht B)");
        return ProbabilityUtils.conditionalRatio(probAAndNotB, probNotB);
    }

    public static double getProbAGivenNotB(double probNotBGivenA, double probA, double probNotB) {
        ProbabilityUtils.requireProbability(probNotBGivenA, "P(nicht B|A)");
        ProbabilityUtils.requireProbability(probA, "P(A)");
        ProbabilityUtils.requirePositiveProbability(probNotB, "P(nicht B)");
        ProbabilityUtils.requirePossibleIntersection(probNotBGivenA * probA, probA, probNotB, "P(A and not B)");
        return ProbabilityUtils.conditionalRatio(probNotBGivenA * probA, probNotB);
    }

    public static double getProbBGivenNotA(double probNotAAndB, double probNotA) {
        ProbabilityUtils.requirePositiveProbability(probNotA, "P(nicht A)");
        ProbabilityUtils.requireProbability(probNotAAndB, "P(nicht A und B)");
        return ProbabilityUtils.conditionalRatio(probNotAAndB, probNotA);
    }

    public static double getProbBGivenNotA(double probNotAGivenB, double probB, double probNotA) {
        ProbabilityUtils.requireProbability(probNotAGivenB, "P(nicht A|B)");
        ProbabilityUtils.requireProbability(probB, "P(B)");
        ProbabilityUtils.requirePositiveProbability(probNotA, "P(nicht A)");
        ProbabilityUtils.requirePossibleIntersection(probNotAGivenB * probB, probB, probNotA, "P(not A and B)");
        return ProbabilityUtils.conditionalRatio(probNotAGivenB * probB, probNotA);
    }
}

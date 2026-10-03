package probabilities;

public class ConditionalProbs extends BaseProbabilities {
    public static double getProbAGivenB(double probAAndB, double probB) {
        ProbabilityUtils.requirePositiveProbability(probB, "P(B)");
        ProbabilityUtils.requireProbability(probAAndB, "P(A und B)");
        return probAAndB / probB;
    }

    public static double getProbAGivenB(double probBGivenA, double probA, double probB) {
        ProbabilityUtils.requireProbability(probBGivenA, "P(B|A)");
        ProbabilityUtils.requireProbability(probA, "P(A)");
        ProbabilityUtils.requirePositiveProbability(probB, "P(B)");
        return (probBGivenA * probA) / probB;
    }

    public static double getProbBGivenA(double probAAndB, double probA) {
        ProbabilityUtils.requirePositiveProbability(probA, "P(A)");
        ProbabilityUtils.requireProbability(probAAndB, "P(A und B)");
        return probAAndB / probA;
    }

    public static double getProbBGivenA(double probAGivenB, double probA, double probB) {
        ProbabilityUtils.requireProbability(probAGivenB, "P(A|B)");
        ProbabilityUtils.requirePositiveProbability(probA, "P(A)");
        ProbabilityUtils.requireProbability(probB, "P(B)");
        return (probAGivenB * probB) / probA;
    }

    public static double getProbNotAGivenB(double probNotAAndB, double probB) {
        ProbabilityUtils.requirePositiveProbability(probB, "P(B)");
        ProbabilityUtils.requireProbability(probNotAAndB, "P(nicht A und B)");
        return probNotAAndB / probB;
    }

    public static double getProbNotAGivenB(double probBGivenNotA, double probNotA, double probB) {
        ProbabilityUtils.requireProbability(probBGivenNotA, "P(B|nicht A)");
        ProbabilityUtils.requireProbability(probNotA, "P(nicht A)");
        ProbabilityUtils.requirePositiveProbability(probB, "P(B)");
        return (probBGivenNotA * probNotA) / probB;
    }

    public static double getProbNotBGivenA(double probAAndNotB, double probA) {
        ProbabilityUtils.requirePositiveProbability(probA, "P(A)");
        ProbabilityUtils.requireProbability(probAAndNotB, "P(A und nicht B)");
        return probAAndNotB / probA;
    }

    public static double getProbNotBGivenA(double probAGivenNotB, double probNotB, double probA) {
        ProbabilityUtils.requireProbability(probAGivenNotB, "P(A|nicht B)");
        ProbabilityUtils.requireProbability(probNotB, "P(nicht B)");
        ProbabilityUtils.requirePositiveProbability(probA, "P(A)");
        return (probAGivenNotB * probNotB) / probA;
    }

    public static double getProbAGivenNotB(double probAAndNotB, double probNotB) {
        ProbabilityUtils.requirePositiveProbability(probNotB, "P(nicht B)");
        ProbabilityUtils.requireProbability(probAAndNotB, "P(A und nicht B)");
        return probAAndNotB / probNotB;
    }

    public static double getProbAGivenNotB(double probNotBGivenA, double probA, double probNotB) {
        ProbabilityUtils.requireProbability(probNotBGivenA, "P(nicht B|A)");
        ProbabilityUtils.requireProbability(probA, "P(A)");
        ProbabilityUtils.requirePositiveProbability(probNotB, "P(nicht B)");
        return (probNotBGivenA * probA) / probNotB;
    }

    public static double getProbBGivenNotA(double probNotAAndB, double probNotA) {
        ProbabilityUtils.requirePositiveProbability(probNotA, "P(nicht A)");
        ProbabilityUtils.requireProbability(probNotAAndB, "P(nicht A und B)");
        return probNotAAndB / probNotA;
    }

    public static double getProbBGivenNotA(double probNotAGivenB, double probB, double probNotA) {
        ProbabilityUtils.requireProbability(probNotAGivenB, "P(nicht A|B)");
        ProbabilityUtils.requireProbability(probB, "P(B)");
        ProbabilityUtils.requirePositiveProbability(probNotA, "P(nicht A)");
        return (probNotAGivenB * probB) / probNotA;
    }
}

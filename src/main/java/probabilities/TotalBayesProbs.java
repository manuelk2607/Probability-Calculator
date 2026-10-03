package probabilities;

import java.util.List;

public class TotalBayesProbs extends BaseProbabilities {
    /**
     * Berechnet P(B) = Summe über i von P(B|A_i) * P(A_i)
     *
     * @param probBGivenA_i Liste der Wahrscheinlichkeiten P(B|A_i)
     * @param probA_i Liste der Wahrscheinlichkeiten P(A_i)
     * @return P(B)
     */
    public static double totalProbability(List<Double> probBGivenA_i, List<Double> probA_i) {
        if (probBGivenA_i == null || probA_i == null) {
            throw new IllegalArgumentException("Listen duerfen nicht null sein.");
        }
        if (probBGivenA_i.isEmpty()) {
            throw new IllegalArgumentException("Listen duerfen nicht leer sein.");
        }
        if (probBGivenA_i.size() != probA_i.size()) throw new IllegalArgumentException("Listen muessen gleich lang sein!");
        double sum = 0.0;
        double priorSum = 0.0;
        for (int i = 0; i < probBGivenA_i.size(); i++) {
            double probBGivenA = ProbabilityUtils.requireProbability(probBGivenA_i.get(i), "P(B|A" + (i + 1) + ")");
            double probA = ProbabilityUtils.requireProbability(probA_i.get(i), "P(A" + (i + 1) + ")");
            priorSum += probA;
            sum += probA * probBGivenA;
        }
        if (Math.abs(priorSum - 1.0) > 1e-9) {
            throw new IllegalArgumentException("Die Basiswahrscheinlichkeiten P(A_i) muessen zusammen 1 ergeben.");
        }
        return sum;
    }

    /**
     * Berechnet P(A|B) nach Bayes: P(A|B) = P(B|A) * P(A) / P(B)
     *
     * @param probBGivenA P(B|A)
     * @param probA P(A)
     * @param probB P(B) (z.B. berechnet mit calcTotalProbability)
     * @return P(A|B)
     */
    public static double calcBayes(double probBGivenA, double probA, double probB) {
        ProbabilityUtils.requireProbability(probBGivenA, "P(B|A)");
        ProbabilityUtils.requireProbability(probA, "P(A)");
        ProbabilityUtils.requirePositiveProbability(probB, "P(B)");
        return (probBGivenA * probA) / probB;
    }
}

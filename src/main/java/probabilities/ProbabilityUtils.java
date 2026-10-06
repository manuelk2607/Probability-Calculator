package probabilities;

final class ProbabilityUtils {
    private ProbabilityUtils() {
    }

    static double requireProbability(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new ProbabilityException(name + " muss zwischen 0 und 1 liegen.", name + " must be between 0 and 1.");
        }
        return value;
    }

    static double requirePositiveProbability(double value, String name) {
        requireProbability(value, name);
        if (value == 0.0) {
            throw new ProbabilityException(name + " darf nicht 0 sein.", name + " must not be 0.");
        }
        return value;
    }

    static void requirePossibleIntersection(double intersection, double probA, double probB, String name) {
        requireProbability(intersection, name);
        double lowerBound = Math.max(0.0, probA + probB - 1.0);
        double upperBound = Math.min(probA, probB);
        if (intersection < lowerBound || intersection > upperBound) {
            throw new ProbabilityException(name + " passt nicht zu den Randwahrscheinlichkeiten.", name + " is inconsistent with the marginal probabilities.");
        }
    }

    static double conditionalRatio(double intersection, double denominator) {
        requireProbability(intersection, "P(intersection)");
        requirePositiveProbability(denominator, "P(condition)");
        if (intersection > denominator) {
            throw new ProbabilityException("Die Schnittwahrscheinlichkeit darf die Bedingung nicht uebersteigen.",
                    "The intersection probability must not exceed the conditioning probability.");
        }
        return intersection / denominator;
    }
}

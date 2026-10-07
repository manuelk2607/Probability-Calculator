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

    static double requirePossibleIntersection(double intersection, double probA, double probB, String name) {
        requireProbability(intersection, name);
        requireProbability(probA, "P(A)");
        requireProbability(probB, "P(B)");
        double lowerBound = Math.max(0.0, probA + probB - 1.0);
        double upperBound = Math.min(probA, probB);
        double tolerance = 8 * Math.max(Math.ulp(probA), Math.ulp(probB));
        if (intersection < lowerBound - tolerance || intersection > upperBound + tolerance) {
            throw new ProbabilityException(name + " passt nicht zu den Randwahrscheinlichkeiten.", name + " is inconsistent with the marginal probabilities.");
        }
        return Math.max(lowerBound, Math.min(upperBound, intersection));
    }

    static double computedProbability(double value) {
        double tolerance = 8 * Math.ulp(1.0);
        if (Double.isFinite(value) && value >= -tolerance && value <= 1 + tolerance) {
            return Math.max(0, Math.min(1, value));
        }
        return requireProbability(value, "P(result)");
    }

    static double conditionalRatio(double intersection, double denominator) {
        requireProbability(intersection, "P(intersection)");
        requirePositiveProbability(denominator, "P(condition)");
        if (intersection > denominator + 8 * Math.ulp(denominator)) {
            throw new ProbabilityException("Die Schnittwahrscheinlichkeit darf die Bedingung nicht uebersteigen.",
                    "The intersection probability must not exceed the conditioning probability.");
        }
        return Math.min(intersection, denominator) / denominator;
    }
}

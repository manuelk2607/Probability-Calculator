package probabilities;

final class ProbabilityUtils {
    private ProbabilityUtils() {
    }

    static double requireProbability(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " muss zwischen 0 und 1 liegen.");
        }
        return value;
    }

    static double requirePositiveProbability(double value, String name) {
        requireProbability(value, name);
        if (value == 0.0) {
            throw new IllegalArgumentException(name + " darf nicht 0 sein.");
        }
        return value;
    }

    static void requirePossibleIntersection(double intersection, double probA, double probB, String name) {
        requireProbability(intersection, name);
        double lowerBound = Math.max(0.0, probA + probB - 1.0);
        double upperBound = Math.min(probA, probB);
        if (intersection < lowerBound - 1e-12 || intersection > upperBound + 1e-12) {
            throw new IllegalArgumentException(name + " passt nicht zu den Randwahrscheinlichkeiten.");
        }
    }
}

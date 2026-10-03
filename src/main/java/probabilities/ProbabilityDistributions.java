package probabilities;

public final class ProbabilityDistributions {
    private ProbabilityDistributions() {
    }

    public static double binomialProbability(int n, int k, double p) {
        requireNonNegativeInt(n, "n");
        requireRangeInt(k, 0, n, "k");
        ProbabilityUtils.requireProbability(p, "p");
        return Math.exp(logCombination(n, k) + k * Math.log(p == 0.0 ? 1.0 : p) + (n - k) * Math.log(p == 1.0 ? 1.0 : 1.0 - p))
                * edgeCaseFactor(k, n, p);
    }

    public static double binomialCumulative(int n, int k, double p) {
        requireNonNegativeInt(n, "n");
        ProbabilityUtils.requireProbability(p, "p");
        if (k < 0) return 0.0;
        if (k >= n) return 1.0;
        double sum = 0.0;
        for (int i = 0; i <= k; i++) {
            sum += binomialProbability(n, i, p);
        }
        return clampProbability(sum);
    }

    public static double binomialInterval(int n, int lower, int upper, double p) {
        requireRangeInt(lower, 0, n, "lower");
        requireRangeInt(upper, lower, n, "upper");
        return clampProbability(binomialCumulative(n, upper, p) - binomialCumulative(n, lower - 1, p));
    }

    public static double poissonProbability(double lambda, int k) {
        requirePositive(lambda, "lambda");
        requireNonNegativeInt(k, "k");
        return Math.exp(-lambda + k * Math.log(lambda) - logFactorial(k));
    }

    public static double poissonCumulative(double lambda, int k) {
        requirePositive(lambda, "lambda");
        if (k < 0) return 0.0;
        double sum = 0.0;
        for (int i = 0; i <= k; i++) {
            sum += poissonProbability(lambda, i);
        }
        return clampProbability(sum);
    }

    public static double poissonInterval(double lambda, int lower, int upper) {
        requireNonNegativeInt(lower, "lower");
        requireRangeInt(upper, lower, Integer.MAX_VALUE, "upper");
        return clampProbability(poissonCumulative(lambda, upper) - poissonCumulative(lambda, lower - 1));
    }

    public static double normalDensity(double mean, double standardDeviation, double x) {
        requirePositive(standardDeviation, "standardDeviation");
        double z = (x - mean) / standardDeviation;
        return Math.exp(-0.5 * z * z) / (standardDeviation * Math.sqrt(2.0 * Math.PI));
    }

    public static double normalCumulative(double mean, double standardDeviation, double x) {
        requirePositive(standardDeviation, "standardDeviation");
        double z = (x - mean) / standardDeviation;
        return clampProbability(0.5 * (1.0 + erfApprox(z / Math.sqrt(2.0))));
    }

    public static double normalInterval(double mean, double standardDeviation, double lower, double upper) {
        if (upper < lower) {
            throw new IllegalArgumentException("upper muss groesser oder gleich lower sein.");
        }
        return clampProbability(normalCumulative(mean, standardDeviation, upper) - normalCumulative(mean, standardDeviation, lower));
    }

    public static double expectedBinomial(int n, double p) {
        requireNonNegativeInt(n, "n");
        ProbabilityUtils.requireProbability(p, "p");
        return n * p;
    }

    public static double varianceBinomial(int n, double p) {
        requireNonNegativeInt(n, "n");
        ProbabilityUtils.requireProbability(p, "p");
        return n * p * (1.0 - p);
    }

    private static double edgeCaseFactor(int k, int n, double p) {
        if (p == 0.0) return k == 0 ? 1.0 : 0.0;
        if (p == 1.0) return k == n ? 1.0 : 0.0;
        return 1.0;
    }

    private static double logCombination(int n, int k) {
        return logFactorial(n) - logFactorial(k) - logFactorial(n - k);
    }

    private static double logFactorial(int n) {
        double sum = 0.0;
        for (int i = 2; i <= n; i++) {
            sum += Math.log(i);
        }
        return sum;
    }

    private static double erfApprox(double x) {
        double sign = Math.signum(x);
        double absolute = Math.abs(x);
        double t = 1.0 / (1.0 + 0.3275911 * absolute);
        double polynomial = (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t + 0.254829592) * t;
        return sign * (1.0 - polynomial * Math.exp(-absolute * absolute));
    }

    private static double clampProbability(double value) {
        if (value < 0.0 && value > -1e-12) return 0.0;
        if (value > 1.0 && value < 1.0 + 1e-12) return 1.0;
        return ProbabilityUtils.requireProbability(value, "Ergebnis");
    }

    private static void requireNonNegativeInt(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " darf nicht negativ sein.");
        }
    }

    private static void requireRangeInt(int value, int min, int max, String name) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " muss zwischen " + min + " und " + max + " liegen.");
        }
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " muss groesser als 0 sein.");
        }
    }
}

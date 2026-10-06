package probabilities;

import org.apache.commons.statistics.distribution.BinomialDistribution;
import org.apache.commons.statistics.distribution.NormalDistribution;
import org.apache.commons.statistics.distribution.PoissonDistribution;

public final class ProbabilityDistributions {
    private ProbabilityDistributions() { }

    public static double binomialProbability(int n, int k, double p) {
        requireRangeInt(k, 0, n, "k");
        return binomial(n, p).probability(k);
    }

    public static double binomialCumulative(int n, int k, double p) {
        return binomial(n, p).cumulativeProbability(k);
    }

    public static double binomialInterval(int n, int lower, int upper, double p) {
        requireRangeInt(lower, 0, n, "lower");
        requireRangeInt(upper, lower, n, "upper");
        return binomial(n, p).probability(lower - 1, upper);
    }

    public static double poissonProbability(double lambda, int k) {
        requireRangeInt(k, 0, Integer.MAX_VALUE, "k");
        return poisson(lambda).probability(k);
    }

    public static double poissonCumulative(double lambda, int k) {
        return poisson(lambda).cumulativeProbability(k);
    }

    public static double poissonInterval(double lambda, int lower, int upper) {
        requireRangeInt(lower, 0, Integer.MAX_VALUE, "lower");
        requireRangeInt(upper, lower, Integer.MAX_VALUE, "upper");
        return poisson(lambda).probability(lower - 1, upper);
    }

    public static double normalDensity(double mean, double standardDeviation, double x) {
        requireFinite(x, "x");
        double result = normal(mean, standardDeviation).density(x);
        requireFinite(result, "f(x)");
        return result;
    }

    public static double normalCumulative(double mean, double standardDeviation, double x) {
        requireFinite(x, "x");
        return normal(mean, standardDeviation).cumulativeProbability(x);
    }

    public static double normalSurvival(double mean, double standardDeviation, double x) {
        requireFinite(x, "x");
        return normal(mean, standardDeviation).survivalProbability(x);
    }

    public static double normalInterval(double mean, double standardDeviation, double lower, double upper) {
        requireFinite(lower, "lower");
        requireFinite(upper, "upper");
        if (upper < lower) throw new ProbabilityException("upper muss groesser oder gleich lower sein.", "upper must be greater than or equal to lower.");
        return normal(mean, standardDeviation).probability(lower, upper);
    }

    public static double expectedBinomial(int n, double p) { return binomial(n, p).getMean(); }
    public static double varianceBinomial(int n, double p) { return binomial(n, p).getVariance(); }

    static BinomialDistribution binomial(int n, double p) {
        requireRangeInt(n, 0, Integer.MAX_VALUE, "n");
        ProbabilityUtils.requireProbability(p, "p");
        return BinomialDistribution.of(n, p);
    }

    static PoissonDistribution poisson(double lambda) {
        requireFinite(lambda, "lambda");
        if (lambda <= 0 || lambda > 1e9) throw new ProbabilityException("lambda muss groesser als 0 und hoechstens 1e9 sein.", "lambda must be greater than 0 and at most 1e9.");
        return PoissonDistribution.of(lambda);
    }

    private static NormalDistribution normal(double mean, double standardDeviation) {
        requireFinite(mean, "mu");
        requireFinite(standardDeviation, "sigma");
        if (standardDeviation <= 0) throw new ProbabilityException("sigma muss groesser als 0 sein.", "sigma must be greater than 0.");
        return NormalDistribution.of(mean, standardDeviation);
    }

    private static void requireRangeInt(int value, int min, int max, String name) {
        if (value < min || value > max) throw new ProbabilityException(name + " muss zwischen " + min + " und " + max + " liegen.", name + " must be between " + min + " and " + max + ".");
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) throw new ProbabilityException(name + " muss endlich sein.", name + " must be finite.");
    }
}

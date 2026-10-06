package probabilities;

import org.apache.commons.statistics.distribution.DiscreteDistribution;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class CalculationService {
    record Value(String label, double value, boolean probability) {
        Value(String label, double value) { this(label, value, true); }
    }
    record Share(String label, double value, int color) { }
    record Group(String label, List<Share> shares) { }
    record Point(String label, double value, boolean selected) { }
    record Result(String heading, List<Value> values, List<Share> shares, List<Group> groups, List<Point> points) { }

    static Result calculate(CalculationRequest request) {
        List<Value> values = new ArrayList<>();
        List<Share> shares = new ArrayList<>();
        List<Group> groups = new ArrayList<>();
        List<Point> points = new ArrayList<>();
        String heading = request.analysis().toLowerCase(java.util.Locale.ROOT);
        switch (request.analysis()) {
            case "COMPLEMENT" -> {
                double a = probability(request, "a"), b = probability(request, "b");
                values.addAll(List.of(new Value("Pr(A)", a), new Value("Pr(not A)", 1 - a), new Value("Pr(B)", b), new Value("Pr(not B)", 1 - b)));
                groups.add(new Group("A", List.of(new Share("Pr(A)", a, 0), new Share("Pr(not A)", 1 - a, 1))));
                groups.add(new Group("B", List.of(new Share("Pr(B)", b, 2), new Share("Pr(not B)", 1 - b, 3))));
            }
            case "JOINT" -> {
                double a = probability(request, "a"), b = probability(request, "b");
                boolean independent = request.inputs().get("intersection").isBlank();
                double intersection = independent ? a * b : probability(request, "intersection");
                double union = JointProbs.getProbAOrB(a, b, intersection);
                values.addAll(List.of(new Value("Pr(A and B)", intersection), new Value("Pr(A or B)", union), new Value("result.aWithoutB", a - intersection), new Value("result.bWithoutA", b - intersection), new Value("Pr(neither)", 1 - union)));
                shares.addAll(List.of(new Share("segment.aOnly", a - intersection, 0), new Share("segment.intersection", intersection, 2), new Share("segment.bOnly", b - intersection, 1), new Share("segment.neither", 1 - union, 4)));
                if (independent) heading = "jointIndependent";
            }
            case "CONDITIONAL" -> {
                double a = probability(request, "a"), b = probability(request, "b"), intersection = probability(request, "intersection");
                ProbabilityUtils.requirePossibleIntersection(intersection, a, b, "Pr(A and B)");
                double ab = ConditionalProbs.getProbAGivenB(intersection, b), ba = ConditionalProbs.getProbBGivenA(intersection, a);
                values.addAll(List.of(new Value("Pr(A|B)", ab), new Value("Pr(B|A)", ba), new Value("Pr(A and B)", intersection)));
                groups.add(new Group("segment.givenB", List.of(new Share("Pr(A|B)", ab, 0), new Share("Pr(not A|B)", 1 - ab, 1))));
                groups.add(new Group("segment.givenA", List.of(new Share("Pr(B|A)", ba, 2), new Share("Pr(not B|A)", 1 - ba, 3))));
            }
            case "BAYES" -> {
                List<Double> likelihoods = list(request, "likelihoods"), priors = list(request, "priors");
                double total = TotalBayesProbs.totalProbability(likelihoods, priors);
                int index = integer(request, "index");
                if (index < 1 || index > priors.size()) throw new ProbabilityException("A_i muss zwischen 1 und " + priors.size() + " liegen.", "A_i must be between 1 and " + priors.size() + ".");
                double posterior = TotalBayesProbs.calcBayes(likelihoods.get(index - 1), priors.get(index - 1), total);
                values.addAll(List.of(new Value("Pr(B)", total), new Value("Pr(A" + index + "|B)", posterior)));
                double other = 0;
                for (int i = 0; i < priors.size(); i++) {
                    double contribution = priors.get(i) * likelihoods.get(i);
                    values.add(new Value("Pr(B and A" + (i + 1) + ")", contribution));
                    if (i < 6) shares.add(new Share("A" + (i + 1), contribution, i % 4));
                    else other += contribution;
                }
                if (priors.size() > 6) shares.add(new Share("segment.other", other, 3));
                shares.add(new Share("Pr(not B)", Math.max(0, 1 - total), 4));
            }
            case "BINOMIAL", "POISSON" -> {
                int k = integer(request, "k"), lower = integer(request, "lower"), upper = integer(request, "upper");
                DiscreteDistribution distribution;
                double exact, interval;
                if (request.analysis().equals("BINOMIAL")) {
                    int n = integer(request, "n");
                    double p = probability(request, "p");
                    exact = ProbabilityDistributions.binomialProbability(n, k, p);
                    interval = ProbabilityDistributions.binomialInterval(n, lower, upper, p);
                    distribution = ProbabilityDistributions.binomial(n, p);
                } else {
                    double lambda = number(request, "lambda");
                    exact = ProbabilityDistributions.poissonProbability(lambda, k);
                    interval = ProbabilityDistributions.poissonInterval(lambda, lower, upper);
                    distribution = ProbabilityDistributions.poisson(lambda);
                }
                values.addAll(List.of(new Value("Pr(X = k)", exact), new Value("Pr(X <= k)", distribution.cumulativeProbability(k)), new Value("Pr(X > k)", distribution.survivalProbability(k)), new Value("Pr(lower <= X <= upper)", interval), new Value("E(X)", distribution.getMean(), false), new Value("Var(X)", distribution.getVariance(), false), new Value("SD(X)", Math.sqrt(distribution.getVariance()), false)));
                // Aggregate adjacent counts so even very large distributions fit the plot.
                int start = distribution.inverseCumulativeProbability(0.0001);
                int end = distribution.inverseCumulativeProbability(0.9999);
                long step = Math.max(1, ((long) end - start + 60) / 60);
                for (long x = start; x <= end; x += step) {
                    int last = (int) Math.min(end, x + step - 1);
                    points.add(new Point(x == last ? Long.toString(x) : x + "-" + last, distribution.probability((int) x - 1, last), k >= x && k <= last));
                }
            }
            case "NORMAL" -> {
                double mean = number(request, "mean"), sd = number(request, "sd"), lower = number(request, "lower"), upper = number(request, "upper");
                double left = ProbabilityDistributions.normalCumulative(mean, sd, lower), between = ProbabilityDistributions.normalInterval(mean, sd, lower, upper), right = ProbabilityDistributions.normalSurvival(mean, sd, upper);
                values.addAll(List.of(new Value("Pr(X <= lower)", left), new Value("Pr(lower <= X <= upper)", between), new Value("Pr(X > upper)", right), new Value("f(mu)", ProbabilityDistributions.normalDensity(mean, sd, mean), false)));
                shares.addAll(List.of(new Share("segment.leftTail", left, 0), new Share("segment.between", between, 2), new Share("segment.rightTail", right, 1)));
            }
            default -> throw new IllegalArgumentException("Unknown calculation type: " + request.analysis());
        }
        return new Result(heading, List.copyOf(values), List.copyOf(shares), List.copyOf(groups), List.copyOf(points));
    }

    private static double probability(CalculationRequest request, String key) { return ProbabilityUtils.requireProbability(number(request, key), key); }

    private static double number(CalculationRequest request, String key) {
        try {
            double value = Double.parseDouble(request.inputs().get(key).trim().replace(',', '.'));
            if (!Double.isFinite(value)) throw new NumberFormatException();
            return value;
        } catch (RuntimeException exception) {
            throw new ProbabilityException(key + " muss eine endliche Zahl sein.", key + " must be a finite number.");
        }
    }

    private static int integer(CalculationRequest request, String key) {
        try {
            int value = Integer.parseInt(request.inputs().get(key).trim());
            if (value < 0) throw new NumberFormatException();
            return value;
        } catch (RuntimeException exception) {
            throw new ProbabilityException(key + " muss eine nichtnegative ganze Zahl sein.", key + " must be a non-negative integer.");
        }
    }

    private static List<Double> list(CalculationRequest request, String key) {
        String raw = request.inputs().get(key);
        if (raw == null || raw.length() > 8192) throw new ProbabilityException("Die Liste ist zu lang.", "The list is too long.");
        String[] tokens = raw.trim().split("[;\\s]+", -1);
        if (tokens.length > 100) throw new ProbabilityException("Hoechstens 100 Listenwerte sind erlaubt.", "At most 100 list values are allowed.");
        try {
            return Arrays.stream(tokens).map(token -> ProbabilityUtils.requireProbability(Double.parseDouble(token.replace(',', '.')), key)).toList();
        } catch (NumberFormatException exception) {
            throw new ProbabilityException(key + " enthaelt eine ungueltige Zahl.", key + " contains an invalid number.");
        }
    }
}

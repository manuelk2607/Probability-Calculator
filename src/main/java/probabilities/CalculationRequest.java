package probabilities;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

record CalculationRequest(String analysis, Map<String, String> inputs) {
    CalculationRequest {
        if (analysis == null || inputs == null || inputs.size() > 16) throw new IllegalArgumentException("Invalid calculation request.");
        for (var input : inputs.entrySet()) {
            if (input.getKey() == null || input.getValue() == null || input.getValue().length() > 8192) {
                throw new ProbabilityException("Eingaben duerfen hoechstens 8192 Zeichen lang sein.", "Inputs must not exceed 8192 characters.");
            }
        }
        inputs = Collections.unmodifiableMap(new LinkedHashMap<>(inputs));
    }
}

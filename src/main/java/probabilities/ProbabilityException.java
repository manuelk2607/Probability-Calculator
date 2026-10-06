package probabilities;

final class ProbabilityException extends IllegalArgumentException {
    private final String englishMessage;

    ProbabilityException(String germanMessage, String englishMessage) {
        super(germanMessage);
        this.englishMessage = englishMessage;
    }

    String englishMessage() {
        return englishMessage;
    }
}

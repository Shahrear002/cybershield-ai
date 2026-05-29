package com.cybershield.api;

/**
 * Unchecked exception thrown when the Spring AI classification pipeline fails
 * for any reason — network error, malformed LLM response, token quota exceeded, etc.
 *
 * <p>Using an unchecked exception keeps the {@code CyberShieldAIService} API clean
 * while still allowing callers (e.g., {@link TriageController}) and Spring's
 * {@code @RestControllerAdvice} to catch and translate it into a meaningful HTTP
 * error response.
 *
 * <p>The optional {@code cause} field preserves the original exception for logging
 * and debugging without leaking internal details to the HTTP client.
 */
public class AiClassificationException extends RuntimeException {

    /**
     * Constructs an {@code AiClassificationException} with a descriptive message.
     *
     * @param message human-readable failure description
     */
    public AiClassificationException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code AiClassificationException} with a message and the
     * originating cause.
     *
     * @param message human-readable failure description
     * @param cause   the underlying throwable that triggered this exception
     */
    public AiClassificationException(String message, Throwable cause) {
        super(message, cause);
    }
}

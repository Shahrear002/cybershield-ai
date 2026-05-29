package com.cybershield.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Immutable value object representing the outcome of an AI triage classification
 * run against a chat-history transcript or incident report.
 *
 * <p>As a Java 17 {@code record}, all fields are final and the canonical
 * constructor, {@code equals}, {@code hashCode}, and {@code toString} are
 * generated automatically by the compiler. Jackson serialises records natively
 * via {@code spring-boot-starter-json} (Jackson 2.14+).
 *
 * <h2>Field semantics</h2>
 * <ul>
 *   <li>{@code category}              – the highest-confidence {@link TriageCategory}
 *       identified by the classifier.</li>
 *   <li>{@code primaryOffenderHandle} – the username, alias, or identifier of the
 *       suspected primary offender as extracted from the transcript. May be
 *       {@code "UNKNOWN"} if no handle could be detected.</li>
 *   <li>{@code riskScore}             – a normalised risk score in {@code [0.0, 100.0]}.
 *       Scores ≥ 75 are considered high-priority and trigger escalation alerts.</li>
 *   <li>{@code legalJustification}    – a plain-language string citing the applicable
 *       legal statute or platform policy provision that supports this classification,
 *       intended for display in case-management UI and export reports.</li>
 * </ul>
 *
 * @param category              the triage category assigned to this incident
 * @param primaryOffenderHandle extracted offender alias or {@code "UNKNOWN"}
 * @param riskScore             normalised risk score in {@code [0.0, 100.0]}
 * @param legalJustification    human-readable legal basis for the classification
 */
@JsonPropertyOrder({"category", "primaryOffenderHandle", "riskScore", "legalJustification"})
public record TriageAnalysis(

        @JsonProperty("category")
        TriageCategory category,

        @JsonProperty("primaryOffenderHandle")
        String primaryOffenderHandle,

        @JsonProperty("riskScore")
        double riskScore,

        @JsonProperty("legalJustification")
        String legalJustification

) {

    // -----------------------------------------------------------------------
    // Compact canonical constructor — input validation
    // -----------------------------------------------------------------------

    /**
     * Compact constructor validates invariants so callers can never construct
     * an incoherent {@code TriageAnalysis} object.
     */
    public TriageAnalysis {
        if (category == null) {
            throw new IllegalArgumentException("TriageAnalysis: category must not be null");
        }
        if (primaryOffenderHandle == null || primaryOffenderHandle.isBlank()) {
            primaryOffenderHandle = "UNKNOWN";
        }
        if (riskScore < 0.0 || riskScore > 100.0) {
            throw new IllegalArgumentException(
                    "TriageAnalysis: riskScore must be in [0.0, 100.0], got " + riskScore);
        }
        if (legalJustification == null || legalJustification.isBlank()) {
            legalJustification = "No legal justification provided.";
        }
    }

    // -----------------------------------------------------------------------
    // Derived / convenience accessors
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} when this analysis warrants immediate escalation
     * to a human case manager (risk score ≥ 75).
     *
     * @return {@code true} if this is a high-priority incident
     */
    public boolean isHighPriority() {
        return riskScore >= 75.0;
    }

    /**
     * Returns the severity label suitable for badge rendering in the frontend.
     *
     * @return {@code "CRITICAL"}, {@code "HIGH"}, {@code "MEDIUM"}, or {@code "LOW"}
     */
    public String severityLabel() {
        if (riskScore >= 85.0) return "CRITICAL";
        if (riskScore >= 65.0) return "HIGH";
        if (riskScore >= 35.0) return "MEDIUM";
        return "LOW";
    }

    // -----------------------------------------------------------------------
    // Static factory helpers
    // -----------------------------------------------------------------------

    /**
     * Convenience factory for building a {@code TriageAnalysis} directly from
     * a detected category, applying the category's {@link TriageCategory#baseRiskMultiplier()}
     * to a provided raw signal score.
     *
     * @param category       the detected triage category
     * @param offenderHandle the extracted offender alias
     * @param rawSignal      a raw signal in {@code [0.0, 50.0]} before multiplier scaling
     * @param justification  legal basis string
     * @return a fully validated {@code TriageAnalysis} record
     */
    public static TriageAnalysis fromRawSignal(
            TriageCategory category,
            String offenderHandle,
            double rawSignal,
            String justification) {

        double scored = Math.min(rawSignal * category.baseRiskMultiplier(), 100.0);
        // Round to 2 decimal places for clean JSON output
        double rounded = Math.round(scored * 100.0) / 100.0;
        return new TriageAnalysis(category, offenderHandle, rounded, justification);
    }
}

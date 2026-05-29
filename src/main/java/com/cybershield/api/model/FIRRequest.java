package com.cybershield.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Immutable data-transfer object representing the user's FIR form submission.
 *
 * <p>Constructed as a Java 17 {@code record} for complete immutability and
 * zero-boilerplate accessor generation. The compact constructor validates the
 * most critical fields so that invalid requests are rejected at the edge before
 * touching any service or persistence layer.
 *
 * <h2>Language support</h2>
 * <ul>
 *   <li>{@code "EN"} — English (default for cross-border FIR submissions)</li>
 *   <li>{@code "BN"} — Bangla (native script for Bangladesh Police portal)</li>
 * </ul>
 *
 * <h2>JSON mapping</h2>
 * All fields use explicit {@code @JsonProperty} names that match the Nuxt 3
 * camelCase form payload so Jackson deserialization is unambiguous even when
 * Spring's {@code spring.jackson.property-naming-strategy} is altered.
 *
 * @param language             ISO 639-1 code controlling LLM output language.
 *                             Accepted values: {@code "EN"} | {@code "BN"}.
 * @param policeStation        Name of the receiving police station / unit.
 * @param subject              One-line summary title of the complaint.
 * @param placeOfOffence       Geographic or online location where the crime occurred.
 * @param dateOfOffence        Human-readable date string (e.g., "15 May 2025").
 * @param informantName        Full legal name of the complainant / victim.
 * @param informantDetails     Supplementary identity context (parents, spouse, address).
 * @param offenderDetails      Known handles, phone numbers, or real names of suspects.
 * @param chronologicalNarrative Cleaned, ordered narrative of events from the transcript.
 * @param witnesses            Names and contact details of available witnesses, if any.
 */
public record FIRRequest(

        @NotBlank(message = "Language code must not be blank.")
        @Pattern(
                regexp = "^(EN|BN)$",
                message = "Language must be one of: EN, BN."
        )
        @JsonProperty("language")
        String language,

        @NotBlank(message = "Police station must not be blank.")
        @Size(max = 200, message = "Police station name must not exceed 200 characters.")
        @JsonProperty("policeStation")
        String policeStation,

        @NotBlank(message = "Subject / complaint title must not be blank.")
        @Size(max = 500, message = "Subject must not exceed 500 characters.")
        @JsonProperty("subject")
        String subject,

        @NotBlank(message = "Place of offence must not be blank.")
        @Size(max = 500, message = "Place of offence must not exceed 500 characters.")
        @JsonProperty("placeOfOffence")
        String placeOfOffence,

        @NotBlank(message = "Date of offence must not be blank.")
        @Size(max = 100, message = "Date of offence must not exceed 100 characters.")
        @JsonProperty("dateOfOffence")
        String dateOfOffence,

        @NotBlank(message = "Informant name must not be blank.")
        @Size(max = 300, message = "Informant name must not exceed 300 characters.")
        @JsonProperty("informantName")
        String informantName,

        @Size(max = 2000, message = "Informant details must not exceed 2000 characters.")
        @JsonProperty("informantDetails")
        String informantDetails,

        @Size(max = 2000, message = "Offender details must not exceed 2000 characters.")
        @JsonProperty("offenderDetails")
        String offenderDetails,

        @NotBlank(message = "Chronological narrative must not be blank.")
        @Size(max = 10_000, message = "Narrative must not exceed 10,000 characters.")
        @JsonProperty("chronologicalNarrative")
        String chronologicalNarrative,

        @Size(max = 2000, message = "Witnesses field must not exceed 2000 characters.")
        @JsonProperty("witnesses")
        String witnesses

) {
    // ── Compact constructor — normalisation + cross-field guard ──────────────

    /**
     * Compact constructor that:
     * <ol>
     *   <li>Trims all text fields so trailing whitespace never leaks into the DB.</li>
     *   <li>Normalises {@code language} to upper-case so callers may pass
     *       {@code "en"} or {@code "En"} without causing a validation failure.</li>
     *   <li>Substitutes {@code null} for optional fields with safe empty strings
     *       so downstream code never needs null-checks for those fields.</li>
     * </ol>
     */
    public FIRRequest {
        // Mandatory normalisation
        language              = language              != null ? language.strip().toUpperCase()  : language;
        policeStation         = policeStation         != null ? policeStation.strip()           : policeStation;
        subject               = subject               != null ? subject.strip()                 : subject;
        placeOfOffence        = placeOfOffence        != null ? placeOfOffence.strip()          : placeOfOffence;
        dateOfOffence         = dateOfOffence         != null ? dateOfOffence.strip()           : dateOfOffence;
        informantName         = informantName         != null ? informantName.strip()           : informantName;
        chronologicalNarrative = chronologicalNarrative != null ? chronologicalNarrative.strip() : chronologicalNarrative;

        // Optional fields — default to empty string if null for safe rendering
        informantDetails = informantDetails != null ? informantDetails.strip() : "";
        offenderDetails  = offenderDetails  != null ? offenderDetails.strip()  : "";
        witnesses        = witnesses        != null ? witnesses.strip()         : "";
    }
}

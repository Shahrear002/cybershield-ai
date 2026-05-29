package com.cybershield.api;

/**
 * Enumeration of top-level triage categories used by the CyberShield AI
 * classification engine to bucket incoming incident reports.
 *
 * <p>Each constant maps to a distinct threat vector recognised by the
 * platform's legal and safety workflows.
 *
 * <ul>
 *   <li>{@link #HARASSMENT}   – sustained intimidation, stalking, or bullying.</li>
 *   <li>{@link #HACKING}      – unauthorised system access, credential theft,
 *                               or malware distribution.</li>
 *   <li>{@link #BLACKMAIL}    – coercive demands backed by threats to expose
 *                               private material (e.g., sextortion).</li>
 *   <li>{@link #DIGITAL_ABUSE}– psychological abuse conducted through digital
 *                               channels (gaslighting, doxxing, account takeovers).</li>
 * </ul>
 */
public enum TriageCategory {

    /** Sustained intimidation, cyberbullying, or stalking via digital channels. */
    HARASSMENT,

    /** Unauthorised access, credential theft, phishing, or malware deployment. */
    HACKING,

    /**
     * Extortion or coercive threats—commonly sextortion or ransomware demands—
     * where the offender leverages sensitive material for financial or sexual gain.
     */
    BLACKMAIL,

    /**
     * Psychological or emotional abuse perpetrated through digital platforms:
     * doxxing, account impersonation, coordinated pile-ons, or manipulation.
     */
    DIGITAL_ABUSE;

    // -----------------------------------------------------------------------
    // Convenience helpers
    // -----------------------------------------------------------------------

    /**
     * Returns a human-readable display label for this category suitable for
     * use in API responses and UI badges.
     *
     * @return capitalised label string, e.g. {@code "Digital Abuse"}
     */
    public String displayLabel() {
        return switch (this) {
            case HARASSMENT    -> "Harassment";
            case HACKING       -> "Hacking / Unauthorised Access";
            case BLACKMAIL     -> "Blackmail / Extortion";
            case DIGITAL_ABUSE -> "Digital Abuse";
        };
    }

    /**
     * Maps this category to a baseline risk multiplier used by the scoring
     * engine. Higher multipliers indicate greater urgency for human review.
     *
     * @return risk multiplier in the range {@code [1.0, 2.0]}
     */
    public double baseRiskMultiplier() {
        return switch (this) {
            case DIGITAL_ABUSE -> 1.0;
            case HARASSMENT    -> 1.3;
            case BLACKMAIL     -> 1.7;
            case HACKING       -> 2.0;
        };
    }
}

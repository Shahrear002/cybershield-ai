package com.cybershield.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Immutable value object representing a tamper-evident certificate issued when
 * digital evidence is anchored to the CyberShield blockchain ledger.
 *
 * <p>Each {@code EvidenceCertificate} provides:
 * <ul>
 *   <li>A {@link #fileHash()} — the SHA-256 digest of the submitted evidence file,
 *       encoded as a lowercase hex string. This hash is the canonical fingerprint
 *       of the evidence: any subsequent modification to the file will produce a
 *       different hash, making tampering immediately detectable.</li>
 *   <li>A {@link #transactionId()} — a mock blockchain receipt formatted as a
 *       {@code 0x}-prefixed 32-byte hex string, simulating the transaction hash
 *       that would be returned by an EVM-compatible chain (e.g., Ethereum, Polygon).</li>
 *   <li>A {@link #timestamp()} — the ISO-8601 UTC instant at which the anchoring
 *       request was processed by the CyberShield server.</li>
 * </ul>
 *
 * <p>In a production deployment, {@link #transactionId()} would be the actual
 * on-chain transaction hash returned by a Web3 provider (e.g., via Web3j).
 * For the hackathon prototype it is synthesised from a UUID to preserve
 * realistic formatting without requiring a live blockchain node.
 *
 * @param fileHash      SHA-256 hex digest of the anchored evidence file
 * @param transactionId mock EVM-style {@code 0x}-prefixed 64-character hex string
 * @param timestamp     ISO-8601 UTC anchoring timestamp, e.g. {@code "2025-11-01T09:32:00Z"}
 */
@JsonPropertyOrder({"fileHash", "transactionId", "timestamp"})
public record EvidenceCertificate(

        @JsonProperty("fileHash")
        String fileHash,

        @JsonProperty("transactionId")
        String transactionId,

        @JsonProperty("timestamp")
        String timestamp

) {

    // -----------------------------------------------------------------------
    // Compact canonical constructor — input validation
    // -----------------------------------------------------------------------

    /**
     * Compact constructor enforces structural integrity so downstream consumers
     * can rely on the format of each field without defensive null-checks.
     */
    public EvidenceCertificate {
        if (fileHash == null || fileHash.isBlank()) {
            throw new IllegalArgumentException("EvidenceCertificate: fileHash must not be null or blank");
        }
        if (!fileHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException(
                    "EvidenceCertificate: fileHash must be a 64-character lowercase hex SHA-256 digest, got: "
                            + fileHash);
        }
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("EvidenceCertificate: transactionId must not be null or blank");
        }
        if (timestamp == null || timestamp.isBlank()) {
            throw new IllegalArgumentException("EvidenceCertificate: timestamp must not be null or blank");
        }
    }

    // -----------------------------------------------------------------------
    // Derived accessors
    // -----------------------------------------------------------------------

    /**
     * Returns the first 8 characters of the {@link #fileHash()} as a short
     * fingerprint suitable for display in compact UI contexts (e.g., table rows).
     *
     * @return 8-character abbreviated hash, e.g. {@code "a3f9c102"}
     */
    public String shortHash() {
        return fileHash.substring(0, 8);
    }

    /**
     * Returns the first 10 characters of the {@link #transactionId()} (including
     * the {@code 0x} prefix) as an abbreviated transaction reference.
     *
     * @return abbreviated transaction ID, e.g. {@code "0x1a2b3c4d"}
     */
    public String shortTxId() {
        return transactionId.substring(0, Math.min(10, transactionId.length()));
    }

    /**
     * Returns a human-readable one-liner summary of this certificate, useful for
     * logging and notification messages.
     *
     * @return summary string
     */
    public String summary() {
        return String.format(
                "Evidence anchored [hash=%s...] at %s (tx=%s...)",
                shortHash(), timestamp, shortTxId());
    }
}

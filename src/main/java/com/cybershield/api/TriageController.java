package com.cybershield.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * REST controller exposing CyberShield AI triage and digital-evidence endpoints.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code POST /api/triage/classify}  — delegates to {@link CyberShieldAIService}
 *       for live LLM-powered classification of a raw chat-history transcript into
 *       a {@link TriageAnalysis} using Spring AI structured output.</li>
 *   <li>{@code POST /api/evidence/secure}  — SHA-256 hashing of an uploaded file with
 *       a mock blockchain transaction receipt returned as an {@link EvidenceCertificate}.</li>
 * </ul>
 */
@CrossOrigin
@RestController
@RequestMapping("/api")
public class TriageController {

    private static final Logger log = LoggerFactory.getLogger(TriageController.class);

    // ISO-8601 UTC formatter for EvidenceCertificate timestamps
    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .withZone(ZoneOffset.UTC);

    // -----------------------------------------------------------------------
    // Injected collaborator
    // -----------------------------------------------------------------------

    private final CyberShieldAIService aiService;

    /**
     * Constructor injection — preferred over field injection for testability.
     * Spring will automatically supply the {@link CyberShieldAIService} bean
     * registered by {@code @Service}.
     *
     * @param aiService the live AI orchestration service
     */
    public TriageController(CyberShieldAIService aiService) {
        this.aiService = aiService;
    }

    // -----------------------------------------------------------------------
    // POST /api/triage/classify
    // -----------------------------------------------------------------------

    /**
     * Classifies a raw chat-history transcript using the live Spring AI engine
     * and returns a structured {@link TriageAnalysis}.
     *
     * <p>All classification intelligence — category assignment, offender handle
     * extraction, risk scoring, and legal justification — is performed by
     * {@link CyberShieldAIService#analyzeIncident(String)} which calls the
     * configured LLM via Spring AI's {@code ChatClient} and parses the response
     * using {@code BeanOutputConverter<TriageAnalysis>}.
     *
     * @param transcript raw chat-history or incident description as plain text
     * @return {@code 200 OK} with the {@link TriageAnalysis} JSON body
     * @throws ResponseStatusException {@code 400} if transcript is blank
     * @throws ResponseStatusException {@code 503} if the AI service is unavailable
     */
    @PostMapping(
            value = "/triage/classify",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TriageAnalysis> classify(@RequestBody String transcript) {

        if (transcript == null || transcript.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body must contain a non-empty transcript.");
        }

        log.info("Classify request received — transcript length={} chars", transcript.length());

        try {
            TriageAnalysis analysis = aiService.analyzeIncident(transcript);
            log.info("Classify response — category={} riskScore={} severity={}",
                    analysis.category(), analysis.riskScore(), analysis.severityLabel());
            return ResponseEntity.ok(analysis);

        } catch (AiClassificationException e) {
            log.error("AI classification pipeline failed", e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "The AI classification service is currently unavailable. " +
                    "Please retry in a moment. Details: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // POST /api/evidence/secure
    // -----------------------------------------------------------------------

    /**
     * Accepts a multipart file upload, computes its SHA-256 digest using Java's
     * built-in {@link MessageDigest}, and returns a mock blockchain-anchored
     * {@link EvidenceCertificate}.
     *
     * <h3>Hash computation</h3>
     * The SHA-256 algorithm reads the file's raw byte stream in 8 KB chunks,
     * feeding each buffer into the {@code MessageDigest} without loading the
     * entire file into heap memory — safe for arbitrarily large evidence files.
     *
     * <h3>Transaction ID format</h3>
     * The mock transaction receipt is constructed as:
     * {@code 0x} + two UUID values with hyphens stripped, producing a realistic
     * 66-character EVM-style transaction hash (e.g., {@code 0x1a2b3c...f09e}).
     *
     * @param file the evidence file submitted for anchoring (any format)
     * @return {@code 200 OK} with the {@link EvidenceCertificate} JSON body
     * @throws ResponseStatusException {@code 400} if no file provided or file is empty
     * @throws ResponseStatusException {@code 500} if hashing or I/O fails unexpectedly
     */
    @PostMapping(
            value = "/evidence/secure",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EvidenceCertificate> secureEvidence(
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A non-empty file must be provided in the 'file' form field.");
        }

        log.info("Evidence anchoring request — filename='{}' size={} bytes",
                file.getOriginalFilename(), file.getSize());

        // ── 1. Compute SHA-256 digest ────────────────────────────────────────
        String fileHash = computeSha256(file);

        // ── 2. Generate mock blockchain transaction ID ───────────────────────
        //    Format: "0x" + 64 hex chars  (mirrors an EVM tx hash)
        String txId = "0x"
                + UUID.randomUUID().toString().replace("-", "")   // 32 hex chars
                + UUID.randomUUID().toString().replace("-", "");  // 32 hex chars

        // ── 3. Capture anchoring timestamp (ISO-8601 UTC) ────────────────────
        String timestamp = ISO_FORMATTER.format(Instant.now());

        EvidenceCertificate certificate = new EvidenceCertificate(fileHash, txId, timestamp);

        log.info("Evidence anchored — {}", certificate.summary());

        return ResponseEntity.ok(certificate);
    }

    // -----------------------------------------------------------------------
    // Private helper — SHA-256 hashing (used by /evidence/secure)
    // -----------------------------------------------------------------------

    /**
     * Streams the given {@link MultipartFile} through a {@link MessageDigest} in
     * 8 KB chunks and returns the resulting SHA-256 digest as a 64-character
     * lowercase hex string.
     *
     * @param file the uploaded file to hash
     * @return 64-character hex SHA-256 digest
     * @throws ResponseStatusException wrapping any {@link IOException} or
     *         {@link NoSuchAlgorithmException} encountered during hashing
     */
    private String computeSha256(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192]; // 8 KB read buffer — memory-efficient
            int bytesRead;

            try (InputStream inputStream = file.getInputStream()) {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            // Convert raw byte array to lowercase hex string
            byte[] hashBytes = digest.digest();
            StringBuilder hexBuilder = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                hexBuilder.append(String.format("%02x", b));
            }
            return hexBuilder.toString();

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JVM spec — this branch is unreachable in practice
            log.error("SHA-256 algorithm not available on this JVM — this should never happen", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Cryptographic hash algorithm unavailable.");
        } catch (IOException e) {
            log.error("I/O error reading uploaded file for hashing", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to read uploaded file: " + e.getMessage());
        }
    }
}

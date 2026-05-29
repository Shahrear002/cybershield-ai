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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Controller handling evidence file uploads, hashing, local preservation, 
 * and mock blockchain chain-of-custody anchoring.
 */
@CrossOrigin
@RestController
@RequestMapping("/api")
public class EvidenceController {

    private static final Logger log = LoggerFactory.getLogger(EvidenceController.class);

    // ISO-8601 UTC formatter matching platform standards
    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .withZone(ZoneOffset.UTC);

    private static final String UPLOAD_DIR_PATH = System.getProperty("java.io.tmpdir") + "/cybershield/uploads/";

    /**
     * Uploads an evidence file, preserves it locally with directory traversal protection,
     * hashes it via SHA-256, and returns a cryptographic ledger receipt.
     *
     * @param file      the raw evidence file (e.g. screenshot, document)
     * @param sessionId active session identifier from the chatbot
     * @return the formal EvidenceReceipt
     */
    @PostMapping(
            value = "/evidence/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EvidenceReceipt> uploadEvidence(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sessionId") String sessionId
    ) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Upload payload must contain a valid, non-empty file."
            );
        }

        log.info("Incoming evidence upload — sessionId={} fileName='{}' size={} bytes", 
                sessionId, file.getOriginalFilename(), file.getSize());

        try {
            // 1. Calculate SHA-256 Cryptographic Hash
            String fileHash = calculateSHA256(file);

            // 2. Generate Random Randomized File Name to avoid directory traversal
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // Sanitize file extension to remove dangerous characters
            fileExtension = fileExtension.replaceAll("[^a-zA-Z0-9.]", "");
            String randomizedFilename = UUID.randomUUID().toString() + fileExtension;

            // 3. Save file locally with directory traversal protections
            Path uploadDir = Paths.get(UPLOAD_DIR_PATH).toAbsolutePath().normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path destinationPath = uploadDir.resolve(randomizedFilename).normalize();
            
            // Security Check: Verify normalized destination resides strictly inside target directory
            if (!destinationPath.startsWith(uploadDir)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Malicious file path detected: directory traversal is strictly prohibited."
                );
            }

            // Copy file content
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // 4. Generate Mock Blockchain Transaction ID
            String transactionId = "0x" + UUID.randomUUID().toString().replace("-", "");
            String timestamp = ISO_FORMATTER.format(Instant.now());

            log.info("Evidence anchored successfully — hash={} txnId={} savedAs={}", 
                    fileHash, transactionId, randomizedFilename);

            EvidenceReceipt receipt = new EvidenceReceipt(
                    originalFilename,
                    fileHash,
                    transactionId,
                    timestamp,
                    "Evidence securely preserved and time-stamped."
            );

            return ResponseEntity.ok(receipt);

        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 hashing algorithm not available", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to process cryptographic hash: SHA-256 algorithm not found."
            );
        } catch (IOException e) {
            log.error("Failed to save or read file payload", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to store file payload locally: " + e.getMessage()
            );
        }
    }

    /**
     * Helper to compute SHA-256 checksum of MultipartFile
     */
    private String calculateSHA256(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

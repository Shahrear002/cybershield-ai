package com.cybershield.api;

/**
 * Record representing the cryptographic evidence chain-of-custody receipt.
 */
public record EvidenceReceipt(
        String fileName,
        String fileHash,
        String transactionId,
        String timestamp,
        String message
) {}

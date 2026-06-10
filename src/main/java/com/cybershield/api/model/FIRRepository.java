package com.cybershield.api.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link FIRObject} persistence operations.
 *
 * <p>All standard CRUD methods ({@code save}, {@code findById}, {@code findAll},
 * {@code deleteById}, etc.) are inherited from
 * {@link JpaRepository}{@code <FIRObject, Long>} and require no additional code.
 *
 * <h2>Custom query methods</h2>
 * Spring Data derives SQL from method names automatically; JPQL is used only
 * where the derived-name approach would be unreadable or ambiguous.
 *
 * <h2>Usage examples</h2>
 * <pre>{@code
 * // Save a new FIR
 * FIRObject saved = firRepository.save(FIRObject.from(request, refNumber));
 *
 * // Pull all pending FIRs for the law enforcement forwarding batch job
 * List<FIRObject> pending = firRepository.findByStatus("PENDING_SUBMISSION");
 *
 * // Retrieve a single FIR by its human-readable reference number
 * Optional<FIRObject> fir = firRepository.findByFirReferenceNumber("CCID-CSA-2025-4821");
 * }</pre>
 *
 * @see FIRObject
 * @see FIRRequest
 */
@Repository
public interface FIRRepository extends JpaRepository<FIRObject, Long> {

    // ── Status-based queries ─────────────────────────────────────────────────

    /**
     * Returns all FIR records matching the given status string.
     *
     * <p>Typical status values: {@code "PENDING_SUBMISSION"}, {@code "SUBMITTED"},
     * {@code "ACKNOWLEDGED"}, {@code "CLOSED"}.
     *
     * @param status the workflow status to filter by (case-sensitive)
     * @return an ordered list (newest first) of matching records
     */
    List<FIRObject> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Returns all FIR records created by the specified user.
     *
     * @param userId the ID of the user
     * @return an ordered list (newest first) of matching records
     */
    List<FIRObject> findByUser_IdOrderByCreatedAtDesc(Long userId);

    /**
     * Returns all FIR records NOT yet submitted to police — i.e., where
     * {@code status = 'PENDING_SUBMISSION'}.
     *
     * <p>Used by the scheduled law enforcement forwarding task to identify
     * records that still need to be dispatched.
     *
     * @return pending FIR records ordered by creation time ascending
     *         (oldest first so they are processed in FIFO order)
     */
    @Query("SELECT f FROM FIRObject f WHERE f.status = 'PENDING_SUBMISSION' ORDER BY f.createdAt ASC")
    List<FIRObject> findAllPending();

    // ── Reference number lookup ──────────────────────────────────────────────

    /**
     * Finds a single FIR by its unique human-readable reference number
     * (e.g., {@code "CCID-CSA-2025-4821"}).
     *
     * @param firReferenceNumber the reference number generated at form submission
     * @return an {@link Optional} containing the matching record, or empty if not found
     */
    Optional<FIRObject> findByFirReferenceNumber(String firReferenceNumber);

    // ── Language-based queries ───────────────────────────────────────────────

    /**
     * Returns all FIR records generated in the specified language.
     *
     * <p>Useful for producing language-specific reports or exporting
     * Bangla-only FIRs for the Bangladesh Police portal.
     *
     * @param language ISO 639-1 code ({@code "EN"} | {@code "BN"})
     * @return list of FIRs in that language, ordered newest first
     */
    List<FIRObject> findByLanguageOrderByCreatedAtDesc(String language);

    // ── Informant / victim search ────────────────────────────────────────────

    /**
     * Searches for FIR records whose {@code informantName} contains the given
     * search term (case-insensitive LIKE match).
     *
     * <p>Allows investigators to pull all FIRs filed by or on behalf of a
     * specific victim without knowing their exact stored name.
     *
     * @param namePart partial or full name of the informant / victim
     * @return list of matching FIRs ordered newest first
     */
    @Query("SELECT f FROM FIRObject f WHERE LOWER(f.informantName) LIKE LOWER(CONCAT('%', :namePart, '%')) ORDER BY f.createdAt DESC")
    List<FIRObject> searchByInformantName(@Param("namePart") String namePart);

    // ── Police station queries ───────────────────────────────────────────────

    /**
     * Returns all FIR records addressed to the specified police station.
     *
     * @param policeStation exact police station name as stored
     * @return list of matching FIRs ordered newest first
     */
    List<FIRObject> findByPoliceStationOrderByCreatedAtDesc(String policeStation);

    // ── Date-range queries ───────────────────────────────────────────────────

    /**
     * Returns all FIR records created within the given UTC date-time window.
     *
     * <p>Used by the analytics dashboard to build time-series data for
     * submission volume charts.
     *
     * @param from start of the window (inclusive)
     * @param to   end of the window (inclusive)
     * @return list of FIRs created in the given range, ordered newest first
     */
    @Query("SELECT f FROM FIRObject f WHERE f.createdAt BETWEEN :from AND :to ORDER BY f.createdAt DESC")
    List<FIRObject> findByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to
    );

    // ── Count helpers (used by dashboard KPI tiles) ──────────────────────────

    /**
     * Counts FIR records in a given status bucket.
     *
     * @param status the workflow status to count
     * @return total count of records with that status
     */
    long countByStatus(String status);

    /**
     * Counts total FIR records filed in the specified language.
     *
     * @param language ISO 639-1 code
     * @return total count
     */
    long countByLanguage(String language);
}

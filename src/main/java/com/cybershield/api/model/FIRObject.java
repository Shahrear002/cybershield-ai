package com.cybershield.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a persisted First Information Report (FIR) record
 * in the H2 in-memory database (dev) or any configured relational store (prod).
 *
 * <h2>Lifecycle</h2>
 * <pre>
 *   PENDING_SUBMISSION → SUBMITTED → ACKNOWLEDGED → CLOSED
 * </pre>
 * Status transitions are managed by a dedicated service layer and may be updated
 * by law enforcement officers accessing the admin panel.
 *
 * <h2>Persistence notes</h2>
 * <ul>
 *   <li>All large text fields (narrative, offender details, etc.) are mapped as
 *       {@code @Column(columnDefinition = "TEXT")} so H2 and PostgreSQL both
 *       allocate variable-length storage instead of the default VARCHAR(255).</li>
 *   <li>{@code createdAt} is set once in {@link #prePersist()} and never mutated;
 *       {@code lastUpdatedAt} is refreshed on every update lifecycle event.</li>
 *   <li>The entity intentionally does NOT use Lombok because Java 17 records
 *       are used for DTOs — plain hand-written accessors keep the entity class
 *       framework-agnostic and easily testable.</li>
 * </ul>
 *
 * <h2>Language field</h2>
 * Stored as-is from the validated {@link FIRRequest#language()} accessor.
 * The AI service reads this field to produce output in the correct script
 * ({@code "EN"} = English prose, {@code "BN"} = Bengali / Bangla prose).
 */
@Entity
@Table(name = "fir_records")
public class FIRObject {

    // ── Primary key ──────────────────────────────────────────────────────────

    /**
     * Auto-incremented surrogate primary key.
     * Uses {@code GenerationType.IDENTITY} for compatibility with both H2
     * (dev) and PostgreSQL (prod) without needing a separate sequence table.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    // ── Lifecycle timestamps ─────────────────────────────────────────────────

    /**
     * UTC timestamp set automatically on first persist via {@link #prePersist()}.
     * Never overwritten after the initial insert.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * UTC timestamp refreshed on every entity update via {@link #preUpdate()}.
     * Allows administrators to quickly filter recently modified records.
     */
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    // ── Status ───────────────────────────────────────────────────────────────

    /**
     * Workflow status of the FIR.
     *
     * <p>Allowed values (enforced at the service layer, not the DB):
     * <ul>
     *   <li>{@code PENDING_SUBMISSION} — generated but not yet forwarded to police</li>
     *   <li>{@code SUBMITTED}          — digitally submitted to the police portal</li>
     *   <li>{@code ACKNOWLEDGED}       — police station has acknowledged receipt</li>
     *   <li>{@code CLOSED}             — case resolved or withdrawn</li>
     * </ul>
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING_SUBMISSION";

    // ── FIR form fields ──────────────────────────────────────────────────────

    /**
     * ISO 639-1 language code controlling the AI-generated FIR content.
     * Values: {@code "EN"} | {@code "BN"}.
     */
    @Column(name = "language", nullable = false, length = 10)
    private String language;

    /**
     * Full name of the receiving police station / cyber crime unit.
     * Example: {@code "CCID Head Office, Dhaka Metropolitan"}.
     */
    @Column(name = "police_station", nullable = false, length = 200)
    private String policeStation;

    /**
     * One-line complaint title / subject.
     */
    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    /**
     * Geographic or online location where the crime occurred.
     * May contain URLs, platform names, or physical addresses.
     */
    @Column(name = "place_of_offence", nullable = false, length = 500)
    private String placeOfOffence;

    /**
     * Human-readable date string describing when the offence took place.
     * Stored as text to accommodate approximate ranges (e.g., "March–April 2025").
     */
    @Column(name = "date_of_offence", nullable = false, length = 100)
    private String dateOfOffence;

    /**
     * Full legal name of the complainant / victim.
     */
    @Column(name = "informant_name", nullable = false, length = 300)
    private String informantName;

    /**
     * Supplementary identity context for the informant: parents' names,
     * spouse's name, permanent and present addresses.
     * Stored as free-form text to accommodate diverse input formats.
     */
    @Column(name = "informant_details", columnDefinition = "TEXT")
    private String informantDetails;

    /**
     * Suspect identification details: social media handles, phone numbers,
     * real names if known, IP addresses, email addresses.
     */
    @Column(name = "offender_details", columnDefinition = "TEXT")
    private String offenderDetails;

    /**
     * Cleaned, chronologically ordered narrative of events reconstructed
     * from the victim's raw transcript by the AI triage pipeline.
     * This is the most legally significant field and is passed verbatim
     * into Section 6 of the printed FIR.
     */
    @Column(name = "chronological_narrative", columnDefinition = "TEXT", nullable = false)
    private String chronologicalNarrative;

    /**
     * Names and contact details of potential witnesses.
     * May be empty if the victim has no witnesses.
     */
    @Column(name = "witnesses", columnDefinition = "TEXT")
    private String witnesses;

    /**
     * Reference number auto-generated by the frontend (e.g., {@code "CCID-CSA-2025-4821"}).
     * Stored for traceability and deduplication.
     */
    @Column(name = "fir_reference_number", length = 60)
    private String firReferenceNumber;

    /**
     * AI-generated FIR body text rendered in the chosen language.
     * Populated by {@code FIRGenerationService} after the entity is first saved;
     * {@code null} until generation completes.
     */
    @Column(name = "generated_content", columnDefinition = "TEXT")
    private String generatedContent;

    // ── User ─────────────────────────────────────────────────────────────────

    /**
     * The victim or admin user who created this FIR.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ── JPA lifecycle callbacks ──────────────────────────────────────────────

    /**
     * Called by JPA before the very first {@code INSERT} of this entity.
     * Sets both timestamp fields to the current UTC moment.
     */
    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt      = now;
        this.lastUpdatedAt  = now;
    }

    /**
     * Called by JPA before every subsequent {@code UPDATE} of this entity.
     * Refreshes {@code lastUpdatedAt} without touching {@code createdAt}.
     */
    @PreUpdate
    private void preUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // ── No-arg constructor required by JPA ───────────────────────────────────

    /** Required by JPA spec — do not use directly; prefer the factory method. */
    protected FIRObject() {}

    // ── Static factory — construct from validated FIRRequest ────────────────

    /**
     * Constructs a new, transient {@link FIRObject} from a validated
     * {@link FIRRequest} DTO and an auto-generated reference number.
     *
     * <p>The returned instance is NOT yet persisted. Call
     * {@code FIRRepository.save(fir)} to persist it.
     *
     * @param req          the validated incoming form data
     * @param refNumber    unique FIR reference number (e.g., "CCID-CSA-2025-4821")
     * @param user         the authenticated user creating the FIR
     * @return a populated, transient FIRObject ready for persistence
     */
    public static FIRObject from(FIRRequest req, String refNumber, User user) {
        FIRObject fir             = new FIRObject();
        fir.language              = req.language();
        fir.policeStation         = req.policeStation();
        fir.subject               = req.subject();
        fir.placeOfOffence        = req.placeOfOffence();
        fir.dateOfOffence         = req.dateOfOffence();
        fir.informantName         = req.informantName();
        fir.informantDetails      = req.informantDetails();
        fir.offenderDetails       = req.offenderDetails();
        fir.chronologicalNarrative = req.chronologicalNarrative();
        fir.witnesses             = req.witnesses();
        fir.firReferenceNumber    = refNumber;
        fir.status                = "PENDING_SUBMISSION";
        fir.user                  = user;
        return fir;
    }

    // ── Accessors (read-only where immutability is intended) ─────────────────

    public Long          getId()                    { return id; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public LocalDateTime getLastUpdatedAt()         { return lastUpdatedAt; }
    public String        getStatus()                { return status; }
    public String        getLanguage()              { return language; }
    public String        getPoliceStation()         { return policeStation; }
    public String        getSubject()               { return subject; }
    public String        getPlaceOfOffence()        { return placeOfOffence; }
    public String        getDateOfOffence()         { return dateOfOffence; }
    public String        getInformantName()         { return informantName; }
    public String        getInformantDetails()      { return informantDetails; }
    public String        getOffenderDetails()       { return offenderDetails; }
    public String        getChronologicalNarrative(){ return chronologicalNarrative; }
    public String        getWitnesses()             { return witnesses; }
    public String        getFirReferenceNumber()    { return firReferenceNumber; }
    public String        getGeneratedContent()      { return generatedContent; }
    public User          getUser()                  { return user; }

    // ── Mutators (only for mutable lifecycle fields) ─────────────────────────

    /** Updates the workflow status. See Javadoc on the {@link #status} field for allowed values. */
    public void setStatus(String status)                     { this.status = status; }

    /** Stores the AI-generated FIR body text after generation completes. */
    public void setGeneratedContent(String generatedContent) { this.generatedContent = generatedContent; }

    // ── toString (for logging — excludes large text fields) ──────────────────

    @Override
    public String toString() {
        return "FIRObject{id=" + id
                + ", refNumber='" + firReferenceNumber + '\''
                + ", language='" + language + '\''
                + ", status='" + status + '\''
                + ", informantName='" + informantName + '\''
                + ", createdAt=" + createdAt + '}';
    }
}

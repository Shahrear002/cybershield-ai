package com.cybershield.api;

import com.cybershield.api.model.FIRObject;
import com.cybershield.api.model.FIRRepository;
import com.cybershield.api.model.FIRRequest;
import com.cybershield.api.model.User;
import com.cybershield.api.model.UserRepository;
import com.cybershield.api.service.AnalyticsService;
import com.cybershield.api.service.FIRGeneratorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
import java.util.Map;

/**
 * REST controller exposing the multi-language FIR (First Information Report)
 * generation and retrieval endpoints.
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>{@code POST /api/fir/generate} — Accepts a {@link FIRRequest} JSON body,
 *       calls {@link FIRGeneratorService} to compile the bilingual FIR via the
 *       LLM, persists the result, and returns the full {@link FIRObject} entity.</li>
 *   <li>{@code GET /api/fir/{id}} — Retrieves a previously generated FIR by its
 *       database ID.</li>
 *   <li>{@code GET /api/fir/ref/{referenceNumber}} — Retrieves a FIR by its
 *       human-readable reference number (e.g., {@code CCID-CSA-2025-4821}).</li>
 *   <li>{@code GET /api/fir/pending} — Lists all FIRs with status
 *       {@code PENDING_SUBMISSION} for the law enforcement forwarding task.</li>
 *   <li>{@code PATCH /api/fir/{id}/status} — Advances a FIR's workflow status.</li>
 * </ul>
 *
 * <h2>CORS</h2>
 * {@code @CrossOrigin} with no arguments permits all origins — intended for
 * the local Nuxt 3 dev server on {@code http://localhost:3000}. Restrict to
 * specific origins in production deployments.
 */
@CrossOrigin
@RestController
@RequestMapping("/api/fir")
public class FIRController {

    private static final Logger log = LoggerFactory.getLogger(FIRController.class);

    // ── Injected collaborators ────────────────────────────────────────────────

    private final FIRGeneratorService firGeneratorService;
    private final FIRRepository       firRepository;
    private final UserRepository      userRepository;
    private final AnalyticsService    analyticsService;

    /**
     * Constructor injection — preferred over {@code @Autowired} field injection.
     *
     * @param firGeneratorService Spring AI–backed FIR compilation service
     * @param firRepository       JPA repository for retrieval and status updates
     * @param userRepository      JPA repository for User lookup
     */
    public FIRController(FIRGeneratorService firGeneratorService,
                         FIRRepository       firRepository,
                         UserRepository      userRepository,
                         AnalyticsService    analyticsService) {
        this.firGeneratorService = firGeneratorService;
        this.firRepository       = firRepository;
        this.userRepository      = userRepository;
        this.analyticsService    = analyticsService;
    }

    // ── POST /api/fir/generate ────────────────────────────────────────────────

    /**
     * Accepts a validated {@link FIRRequest} JSON body, orchestrates the
     * bilingual FIR generation pipeline, and returns the persisted
     * {@link FIRObject} (including the AI-generated text and database ID).
     *
     * <h3>Request</h3>
     * <pre>{@code
     * POST /api/fir/generate
     * Content-Type: application/json
     *
     * {
     *   "language": "EN",
     *   "policeStation": "CCID Head Office, Dhaka Metropolitan",
     *   "subject": "Cybercrime — Hacking and Blackmail",
     *   "placeOfOffence": "Online / Facebook Messenger",
     *   "dateOfOffence": "15 May 2025",
     *   "informantName": "Nusrat Jahan",
     *   "informantDetails": "Father: Abdur Rahman, Address: 12 Mirpur Road, Dhaka",
     *   "offenderDetails": "@darkh4ck3r on Facebook",
     *   "chronologicalNarrative": "...",
     *   "witnesses": "Sadia Akter (friend)"
     * }
     * }</pre>
     *
     * <h3>Response — 201 Created</h3>
     * The full saved {@link FIRObject} JSON, including {@code id},
     * {@code firReferenceNumber}, {@code createdAt}, {@code status},
     * and the {@code generatedContent} field containing the compiled FIR letter.
     *
     * @param request the validated FIR form data from the client
     * @return {@code 201 Created} with the saved {@link FIRObject} body
     * @throws ResponseStatusException {@code 400} if validation fails
     * @throws ResponseStatusException {@code 503} if the AI service is unavailable
     */
    @PostMapping(
            value    = "/generate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FIRObject> generate(
            @Valid @RequestBody FIRRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("FIR generation request received — language={} informant='{}'",
                request.language(), request.informantName());

        User user = null;
        if (userDetails != null) {
            user = userRepository.findByUsername(userDetails.getUsername())
                    .orElse(null);
        }

        try {
            FIRObject saved = firGeneratorService.compileFIR(request, user);
            log.info("FIR generated and persisted — id={} refNumber='{}'",
                    saved.getId(), saved.getFirReferenceNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (IllegalArgumentException e) {
            log.warn("FIR generation rejected — invalid input: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid FIR request: " + e.getMessage());

        } catch (AiClassificationException e) {
            log.error("FIR generation failed — AI pipeline error", e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "FIR generation unavailable. The AI service could not be reached. " +
                    "Details: " + e.getMessage());
        }
    }

    // ── GET /api/fir/{id} ─────────────────────────────────────────────────────

    /**
     * Retrieves a single previously generated FIR by its database surrogate ID.
     *
     * @param id the auto-generated database primary key
     * @return {@code 200 OK} with the {@link FIRObject}, or {@code 404} if not found
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FIRObject> getById(@PathVariable Long id) {
        return firRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No FIR found with id: " + id));
    }

    // ── GET /api/fir/ref/{referenceNumber} ────────────────────────────────────

    /**
     * Retrieves a FIR by its human-readable reference number
     * (e.g., {@code CCID-CSA-2025-4821}).
     *
     * @param referenceNumber the {@code firReferenceNumber} stored on the entity
     * @return {@code 200 OK} with the matching {@link FIRObject}, or {@code 404}
     */
    @GetMapping(value = "/ref/{referenceNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FIRObject> getByReferenceNumber(
            @PathVariable String referenceNumber) {

        return firRepository.findByFirReferenceNumber(referenceNumber)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No FIR found with reference number: " + referenceNumber));
    }

    // ── GET /api/fir/pending ──────────────────────────────────────────────────

    /**
     * Returns all FIR records currently in {@code PENDING_SUBMISSION} status,
     * ordered oldest-first for FIFO processing by the law enforcement dispatch task.
     *
     * @return {@code 200 OK} with a (possibly empty) list of pending FIRs
     */
    @GetMapping(value = "/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FIRObject>> getPending() {
        List<FIRObject> pending = firRepository.findAllPending();
        log.info("Pending FIR list requested — {} records found", pending.size());
        return ResponseEntity.ok(pending);
    }

    // ── GET /api/fir (list all) ───────────────────────────────────────────────

    /**
     * Returns all FIR records ordered newest-first.
     * Intended for the admin dashboard table view. If the user is a VICTIM,
     * it returns only their own FIRs.
     *
     * @return {@code 200 OK} with stored FIRs
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FIRObject>> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        List<FIRObject> all;
        if (user.getRole() == User.Role.ROLE_ADMIN) {
            all = firRepository.findAll();
        } else {
            all = firRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
        }
        
        log.info("FIR list requested — {} total records for user {}", all.size(), user.getUsername());
        return ResponseEntity.ok(all);
    }

    // ── PATCH /api/fir/{id}/status ────────────────────────────────────────────

    /**
     * Advances a FIR's workflow status.
     *
     * <h3>Request body</h3>
     * <pre>{@code { "status": "SUBMITTED" }}</pre>
     *
     * <h3>Allowed status values</h3>
     * {@code PENDING_SUBMISSION → SUBMITTED → ACKNOWLEDGED → CLOSED}
     *
     * @param id      the FIR database ID
     * @param payload JSON body containing the new {@code status} string
     * @return {@code 200 OK} with the updated {@link FIRObject}
     */
    @PatchMapping(
            value    = "/{id}/status",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FIRObject> updateStatus(
            @PathVariable Long              id,
            @RequestBody  Map<String,String> payload) {

        String newStatus = payload.get("status");

        if (newStatus == null || newStatus.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Request body must contain a non-blank 'status' field.");
        }

        // Validate allowed transitions
        if (!List.of("PENDING_SUBMISSION", "SUBMITTED", "ACKNOWLEDGED", "CLOSED")
                 .contains(newStatus.toUpperCase())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status value: '" + newStatus + "'. " +
                    "Allowed values: PENDING_SUBMISSION, SUBMITTED, ACKNOWLEDGED, CLOSED.");
        }

        FIRObject fir = firRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No FIR found with id: " + id));

        String previous = fir.getStatus();
        fir.setStatus(newStatus.toUpperCase());
        FIRObject updated = firRepository.save(fir);

        log.info("FIR status updated — id={} refNumber='{}' {} → {}",
                updated.getId(), updated.getFirReferenceNumber(), previous, newStatus.toUpperCase());

        return ResponseEntity.ok(updated);
    }
    
    // ── GET /api/fir/analytics ───────────────────────────────────────────────

    /**
     * Retrieves threat intelligence analytics directly from the LLM.
     * Uses semantic deduplication on recent FIRs.
     */
    @GetMapping(value = "/analytics", produces = "application/json")
    public ResponseEntity<String> getAnalytics() {
        log.info("Fetching FIR analytics via AnalyticsService...");
        try {
            String analyticsJson = analyticsService.generateAnalytics();
            return ResponseEntity.ok(analyticsJson);
        } catch (Exception e) {
            log.error("Failed to generate analytics", e);
            return ResponseEntity.internalServerError().body("{\"error\": \"Failed to generate analytics\"}");
        }
    }
}

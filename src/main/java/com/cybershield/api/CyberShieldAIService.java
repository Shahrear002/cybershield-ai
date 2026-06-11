package com.cybershield.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

/**
 * Spring AI orchestration service for CyberShield's cybercrime triage pipeline.
 *
 * <p>This service replaces the earlier keyword-based mock logic in
 * {@link TriageController} with a live LLM call that leverages Spring AI's
 * structured-output feature. The LLM is instructed via a carefully engineered
 * prompt to return JSON that maps directly onto our {@link TriageAnalysis} record,
 * and {@link BeanOutputConverter} enforces that contract at the framework level.
 *
 * <h2>Architecture</h2>
 * <pre>
 *   TriageController
 *        │  POST /api/triage/classify
 *        ▼
 *   CyberShieldAIService.analyzeIncident(transcript)
 *        │
 *        ├─ 1. Builds a system prompt (Bangladesh Cyber Security Act context)
 *        ├─ 2. Instantiates BeanOutputConverter&lt;TriageAnalysis&gt;
 *        │      → generates a JSON Schema from our record for schema injection
 *        ├─ 3. Fires ChatClient prompt (system + user messages)
 *        ├─ 4. BeanOutputConverter.convert(rawJson) → TriageAnalysis
 *        └─ 5. Normalises riskScore [0.0–1.0] → [0.0–100.0] and returns
 * </pre>
 *
 * <h2>Provider switching</h2>
 * The {@code ChatClient} is backed by whatever {@code spring.ai.openai.base-url}
 * points to. No code changes are required to swap between OpenAI, Groq, or
 * OpenRouter — only the properties file needs updating.
 *
 * @see BeanOutputConverter
 * @see TriageAnalysis
 */
@Service
public class CyberShieldAIService {

    private static final Logger log = LoggerFactory.getLogger(CyberShieldAIService.class);

    // -----------------------------------------------------------------------
    // Collaborator — injected via constructor (immutable, testable)
    // -----------------------------------------------------------------------

    private final ChatClient chatClient;

    // -----------------------------------------------------------------------
    // Prompt constants — kept as class-level fields so they are constructed
    // once at startup and reused across every request (thread-safe; strings
    // are immutable in Java).
    // -----------------------------------------------------------------------

    /**
     * System persona prompt baked into the {@link ChatClient} at construction time.
     *
     * <p>Establishes the LLM's role as a legal analysis engine operating under
     * the Bangladesh Cyber Security Act 2023 (DSA replacement), defines strict
     * output constraints, and names all approved enum constants so the model
     * cannot hallucinate category values.
     *
     * <p><strong>Critical constraints for the LLM:</strong>
     * <ul>
     *   <li>Respond with raw JSON only — no markdown fences, no prose.</li>
     *   <li>{@code category} must be one of the four approved enum constants.</li>
     *   <li>{@code riskScore} must be a decimal in {@code [0.0, 1.0]}.</li>
     *   <li>{@code legalJustification} must cite a real Act section or statute.</li>
     * </ul>
     */
    private static final String SYSTEM_PROMPT = """
            You are CyberShield AI — a specialized legal analysis engine operating under \
            the updated Bangladesh Cyber Security legal framework (including 2025/2026 updates) \
            and supporting international digital rights frameworks.
            
            RELEVANT BANGLADESH STATUTES (Use these for legalJustification):
            1. Cyber Security Act, 2023 (Cybercrime, hacking, fraud, harassment)
            2. Cyber Security Ordinance, 2025 (Replaces CSA 2023, covers AI misuse, stronger platform accountability)
            3. Information and Communication Technology Act, 2006 (Legacy cyber offences, forgery)
            4. Telecommunications Act, 2001 (Network misuse, unauthorized transmission)
            5. Personal Data Protection Ordinance, 2025 (Data processing, breach notification)
            6. National Data Governance Ordinance, 2025 (Gov data management)
            7. Pornography Control Act, 2012 (Explicit content, child pornography)
            8. Children Act, 2013 (Child identity protection online)
            9. The Penal Code, 1860 (Fraud, defamation, intimidation)
            10. Bangladesh Evidence Act, 1872 (Digital evidence admissibility)
            11. Code of Criminal Procedure (CrPC), 1898 (Investigation procedure)
            12. Cyber Tribunal & Special Courts Framework (Trial & sentencing)

            YOUR ROLE:
            Analyze cybercrime incident transcripts and extract precise structured intelligence \
            for use in law enforcement case management, victim support, and judicial reporting.

            APPROVED CATEGORY ENUM VALUES (use exactly these strings, case-sensitive):
              • HARASSMENT    — sustained intimidation, cyberbullying, stalking
              • HACKING       — unauthorised system access, credential theft, malware
              • BLACKMAIL     — extortion, sextortion, non-consensual image threats
              • DIGITAL_ABUSE — doxxing, impersonation, gaslighting, emotional coercion

            CRITICAL OUTPUT CONSTRAINTS:
              1. Respond with valid JSON ONLY — no markdown fences, no explanatory prose.
              2. The `category` field MUST be exactly one of the four enum strings above.
              3. The `riskScore` field MUST be a decimal number in the range [0.0, 1.0] where:
                   0.0–0.3 = LOW risk    (informational, no immediate escalation)
                   0.3–0.6 = MEDIUM risk (requires review within 48 hours)
                   0.6–0.8 = HIGH risk   (requires review within 24 hours)
                   0.8–1.0 = CRITICAL    (immediate escalation to law enforcement)
              4. The `primaryOffenderHandle` MUST be the detected username, @handle, \
                 or alias from the transcript. If none is detectable, use "UNKNOWN".
              5. The `legalJustification` MUST cite one of the specific RELEVANT BANGLADESH STATUTES \
                 listed above that applies to the incident (e.g., Cyber Security Ordinance, 2025).
              6. If the transcript is ambiguous, choose the highest-severity matching category.
            """;

    /**
     * User-turn prompt template.
     *
     * <p>Contains two literal placeholders that are substituted via
     * {@link String#replace(CharSequence, CharSequence)} before being sent:
     * <ul>
     *   <li>{@code {{TRANSCRIPT}}} — the raw incident text submitted by the victim.</li>
     *   <li>{@code {{FORMAT}}}     — the JSON Schema injected by
     *       {@link BeanOutputConverter#getFormat()}, which instructs the LLM
     *       to produce output that Jackson can deserialize directly into
     *       {@link TriageAnalysis}.</li>
     * </ul>
     *
     * <p>Double-brace delimiters ({@code {{...}}}) are used intentionally to avoid
     * conflicts with Spring AI's own single-brace template engine and with curly
     * braces that appear naturally inside the JSON Schema itself.
     */
    private static final String USER_PROMPT_TEMPLATE = """
            Analyze the following cybercrime report transcript under the scope of the \
            Bangladesh Cyber Security Act 2023. Extract the primary offender handles, \
            assign a risk score from 0.0 to 1.0, and categorize it strictly into one \
            of our approved enums: HARASSMENT, HACKING, BLACKMAIL, DIGITAL_ABUSE.

            TRANSCRIPT:
            ───────────────────────────────────────────────────────────────
            {{TRANSCRIPT}}
            ───────────────────────────────────────────────────────────────

            Respond with a JSON object conforming EXACTLY to this schema — no extra \
            fields, no missing fields, no comments:

            {{FORMAT}}
            """;

    // -----------------------------------------------------------------------
    // Constructor — Spring AI auto-configures ChatClient.Builder via the
    // spring-ai-openai-spring-boot-starter; injecting the Builder (not the
    // ChatClient directly) lets us configure per-service defaults cleanly.
    // -----------------------------------------------------------------------

    /**
     * Creates the service and pre-configures the {@link ChatClient} with the
     * CyberShield system persona so it is reused across all requests.
     *
     * @param builder Spring AI's auto-configured {@link ChatClient.Builder};
     *                injected by the Spring container — never {@code null}
     */
    public CyberShieldAIService(ChatClient.Builder builder) {
        this.chatClient = builder
                // Pin the system persona once at construction — avoids repeating it
                // in every individual call and ensures consistent model behaviour.
                .defaultSystem(SYSTEM_PROMPT)
                .build();
        log.info("CyberShieldAIService initialised — ChatClient ready");
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Sends a cybercrime transcript to the configured LLM and returns a fully
     * structured {@link TriageAnalysis} parsed from the model's JSON output.
     *
     * <h3>Structured-output mechanism</h3>
     * {@link BeanOutputConverter} is used explicitly (rather than the shorthand
     * {@code .entity(TriageAnalysis.class)}) to give us full visibility and control
     * over three steps:
     * <ol>
     *   <li><strong>Schema generation</strong> — {@code converter.getFormat()} derives
     *       a JSON Schema from {@link TriageAnalysis}'s Jackson annotations and injects
     *       it into the user prompt, so the LLM knows the exact field names and types.</li>
     *   <li><strong>Prompt construction</strong> — the schema is embedded at the end of
     *       the user message, making the constraint visible in logs and easy to tune.</li>
     *   <li><strong>Parsing</strong> — {@code converter.convert(rawJson)} handles both
     *       clean JSON responses and responses wrapped in markdown code fences (the
     *       converter strips fences automatically before deserializing).</li>
     * </ol>
     *
     * <h3>Risk score normalisation</h3>
     * The LLM is instructed to return {@code riskScore} in {@code [0.0, 1.0]}.
     * This method normalises it to {@code [0.0, 100.0]} before constructing the
     * final record, matching the validation range enforced by {@link TriageAnalysis}'s
     * compact constructor.
     *
     * @param transcript raw chat-history or incident description text; must not be blank
     * @return a validated, normalised {@link TriageAnalysis} record
     * @throws IllegalArgumentException   if {@code transcript} is null or blank
     * @throws AiClassificationException  if the LLM call fails or the response cannot
     *                                    be parsed into a valid {@link TriageAnalysis}
     */
    public TriageAnalysis analyzeIncident(String transcript) {

        if (transcript == null || transcript.isBlank()) {
            throw new IllegalArgumentException("analyzeIncident: transcript must not be null or blank");
        }

        log.info("AI triage request — transcript length={} chars", transcript.length());

        // ── Step 1: Instantiate BeanOutputConverter for TriageAnalysis ──────
        //
        // BeanOutputConverter<T> does three things under the hood:
        //   a) Introspects TriageAnalysis using Jackson's JsonSchemaGenerator
        //      (honouring our @JsonProperty and @JsonPropertyOrder annotations).
        //   b) Produces a human-readable format instruction string (JSON Schema)
        //      that we embed in the user prompt.
        //   c) Deserializes the LLM's raw JSON string back into TriageAnalysis,
        //      stripping any markdown fences the model may have added.
        //
        BeanOutputConverter<TriageAnalysis> converter =
                new BeanOutputConverter<>(TriageAnalysis.class);

        // ── Step 2: Build the user message — inject transcript + JSON Schema ─
        //
        // We use double-brace {{PLACEHOLDER}} delimiters to avoid collisions with:
        //   • Spring AI's own {variable} template engine
        //   • JSON object literals in the schema (which contain { } characters)
        // String.replace() performs literal substitution — no regex, no escaping needed.
        //
        String userMessage = USER_PROMPT_TEMPLATE
                .replace("{{TRANSCRIPT}}", transcript)
                .replace("{{FORMAT}}", converter.getFormat());

        log.debug("User message constructed — total length={} chars", userMessage.length());

        // ── Step 3: Call the LLM ─────────────────────────────────────────────
        //
        // The system prompt is already baked in via defaultSystem() at construction.
        // We only supply the user message here. The call is synchronous and blocking;
        // for production, consider wrapping this in @Async or Project Reactor.
        //
        String rawJson;
        try {
            rawJson = chatClient
                    .prompt()
                    .user(userMessage)
                    .call()
                    .content();

            log.debug("Raw LLM response received — length={} chars", rawJson.length());

        } catch (Exception e) {
            log.error("LLM API call failed — provider may be unreachable or key invalid", e);
            throw new AiClassificationException(
                    "AI service unreachable: " + e.getMessage(), e);
        }

        // ── Step 4: Parse JSON → TriageAnalysis record ───────────────────────
        //
        // converter.convert() will:
        //   • Strip ```json ... ``` fences if the model wrapped its output
        //   • Call ObjectMapper.readValue(cleaned, TriageAnalysis.class)
        //   • Invoke TriageAnalysis's compact constructor (triggering validation)
        //
        // If the LLM violated the schema (wrong enum value, missing field, etc.),
        // Jackson throws a JsonProcessingException which we wrap and re-throw.
        //
        TriageAnalysis rawAnalysis;
        try {
            rawAnalysis = converter.convert(rawJson);
        } catch (Exception e) {
            log.error("Failed to parse LLM response into TriageAnalysis. Raw response:\n{}", rawJson, e);
            throw new AiClassificationException(
                    "AI returned malformed output that could not be parsed into TriageAnalysis: "
                            + e.getMessage(), e);
        }

        if (rawAnalysis == null) {
            log.error("BeanOutputConverter returned null — raw response was:\n{}", rawJson);
            throw new AiClassificationException(
                    "AI converter returned null — the model response was empty or unparseable.");
        }

        // ── Step 5: Normalise riskScore [0.0, 1.0] → [0.0, 100.0] ──────────
        //
        // The LLM is instructed to return a score in [0.0, 1.0] (a conventional
        // probability/confidence scale for AI models). Our TriageAnalysis record
        // stores and displays scores in [0.0, 100.0] for UI percentage rendering.
        // The compact constructor validates against the [0.0, 100.0] range, so we
        // must normalise BEFORE constructing the final record.
        //
        // We defensively clamp the raw value in case the LLM slightly overflows
        // (e.g., returns 1.001 due to floating-point representation).
        //
        double rawScore  = Math.max(0.0, Math.min(1.0, rawAnalysis.riskScore()));
        double scaled    = rawScore * 100.0;
        double normalized = Math.round(scaled * 100.0) / 100.0; // round to 2 d.p.

        TriageAnalysis finalAnalysis = new TriageAnalysis(
                rawAnalysis.category(),
                rawAnalysis.primaryOffenderHandle(),
                normalized,
                rawAnalysis.legalJustification()
        );

        log.info("AI triage complete — category={} riskScore={} severity={} highPriority={}",
                finalAnalysis.category(),
                finalAnalysis.riskScore(),
                finalAnalysis.severityLabel(),
                finalAnalysis.isHighPriority());

        return finalAnalysis;
    }
}

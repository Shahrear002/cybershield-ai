package com.cybershield.api.service;

import com.cybershield.api.AiClassificationException;
import com.cybershield.api.model.FIRObject;
import com.cybershield.api.model.FIRRepository;
import com.cybershield.api.model.FIRRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Spring AI orchestration service responsible for compiling bilingual
 * First Information Reports (FIRs) from structured form data.
 *
 * <h2>Architecture</h2>
 * <pre>
 *   FIRController
 *        │  POST /api/fir/generate
 *        ▼
 *   FIRGeneratorService.compileFIR(FIRRequest)
 *        │
 *        ├─ 1. Select system template for language (EN | BN)
 *        ├─ 2. Build user message embedding all FIRRequest fields
 *        ├─ 3. Call ChatClient (Groq / OpenAI-compatible) — plain text output
 *        ├─ 4. Strip residual markdown fences from LLM response
 *        ├─ 5. Persist FIRObject (status = PENDING_SUBMISSION) via repository
 *        └─ 6. Return saved entity to controller
 * </pre>
 *
 * <h2>Output format</h2>
 * The LLM is instructed via a strict system prompt to produce ONLY the formatted
 * FIR text — no markdown, no preamble, no signature explanations beyond what the
 * template specifies.
 *
 * <h2>Language support</h2>
 * <ul>
 *   <li>{@code "EN"} — English prose following Bangladesh Police Form No. 53 / Ejahar format</li>
 *   <li>{@code "BN"} — Bengali script (বাংলা) following the same structural format</li>
 * </ul>
 *
 * @see FIRRequest
 * @see FIRObject
 * @see FIRRepository
 */
@Service
public class FIRGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(FIRGeneratorService.class);

    // ── Collaborators ────────────────────────────────────────────────────────

    private final ChatClient    chatClient;
    private final FIRRepository firRepository;

    // ── English system prompt ────────────────────────────────────────────────

    /**
     * System persona for English FIR generation.
     *
     * <p>Forces the LLM into a legal scrivener role that reproduces the exact
     * Bangladesh Police Ejahar / First Information Report format without any
     * conversational content, Markdown syntax, or creative additions.
     */
    private static final String SYSTEM_PROMPT_EN = """
            You are a professional legal document writer for the Bangladesh Cyber Crime \
            Investigation Division (CCID). Your ONLY job is to produce a complete, \
            formally worded First Information Report (FIR / Ejahar) in English.

            STRICT OUTPUT FORMAT — reproduce EXACTLY this skeleton, replacing bracketed \
            tokens with the supplied field values and expanding the narrative naturally \
            into formal legal prose. Preserve every line break shown:

            To
            The Officer-In-Charge
            {{POLICE_STATION}}
            Subject: Entry of an Ejahar
            Place of offence: {{PLACE}}
            Date and time: {{DATE}}

            Respected Sir / Madam,

            I, the undersigned, {{INFORMANT_NAME}}, {{INFORMANT_DETAILS}}, most respectfully \
            beg to state the following particulars of a cybercrime committed against me under \
            the Bangladesh Cyber Security Act 2023:

            {{NARRATIVE}}

            The perpetrator(s) identified as {{OFFENDER_DETAILS}} committed the above offence \
            via digital means and violated Sections of the Bangladesh Cyber Security Act 2023 \
            as cited in the complaint.

            Witnesses to the above incident include: {{WITNESSES}}

            As such I would request you to kindly register this complaint, take proper legal \
            steps under the applicable provisions of the Bangladesh Cyber Security Act 2023, \
            and provide me with a copy of the registered FIR for my records.

            Yours Sincerely,
            {{INFORMANT_NAME}}

            CRITICAL OUTPUT CONSTRAINTS:
            1. Output ONLY the FIR letter. No preamble, no conclusion, no commentary.
            2. Do NOT use any Markdown (no **, no ```, no #, no —).
            3. Do NOT invent facts not present in the supplied fields.
            4. Expand the narrative field into coherent formal paragraphs.
            5. If witnesses field is empty or "N/A", write "No witnesses are known at this time."
            6. If offender details are partial, write them exactly as supplied without embellishment.
            """;

    // ── Bengali (Bangla) system prompt ───────────────────────────────────────

    /**
     * System persona for Bengali FIR generation (বাংলা ভাষায় এজাহার)।
     *
     * <p>The model is instructed to reproduce the exact Bangla official format
     * used by Bangladesh Police for cyber crime complaints, substituting the
     * supplied field values into the correct structural positions.
     */
    private static final String SYSTEM_PROMPT_BN = """
            আপনি বাংলাদেশ পুলিশের সাইবার ক্রাইম ইনভেস্টিগেশন ডিভিশনের (CCID) জন্য \
            একজন পেশাদার আইনি দলিল লেখক। আপনার একমাত্র কাজ হল নিচের কঠোর ফরম্যাট \
            অনুসরণ করে সম্পূর্ণ এজাহার/প্রথম তথ্য প্রতিবেদন (FIR) বাংলায় তৈরি করা।

            কঠোর আউটপুট ফরম্যাট — নিচের কাঠামো হুবহু অনুসরণ করুন, ব্র্যাকেটের মান \
            সরবরাহকৃত তথ্য দিয়ে প্রতিস্থাপন করুন এবং বিবরণ আনুষ্ঠানিক আইনি বাংলায় বিস্তার করুন:

            বরাবর,
            ভারপ্রাপ্ত কর্মকর্তা
            {{POLICE_STATION}}
            বিষয়: এজাহার প্রসঙ্গে।

            জনাব,
            বিনীত নিবেদন এই যে, আমি {{INFORMANT_NAME}}, {{INFORMANT_DETAILS}}, বাংলাদেশ \
            সাইবার নিরাপত্তা আইন ২০২৩ এর অধীনে আমার বিরুদ্ধে সংঘটিত সাইবার অপরাধের \
            নিম্নলিখিত বিবরণ সবিনয়ে উপস্থাপন করছি:

            অপরাধের স্থান: {{PLACE}}
            অপরাধের তারিখ ও সময়: {{DATE}}

            {{NARRATIVE}}

            অভিযুক্ত ব্যক্তি/ব্যক্তিগণ: {{OFFENDER_DETAILS}} উক্ত অপরাধ ডিজিটাল \
            মাধ্যমে সংঘটন করেছেন এবং বাংলাদেশ সাইবার নিরাপত্তা আইন ২০২৩ এর প্রযোজ্য \
            ধারা লঙ্ঘন করেছেন।

            সাক্ষী: {{WITNESSES}}

            অতএব, মহোদয়ের নিকট বিনীত প্রার্থনা এই যে, উপরোক্ত অভিযোগটি নথিভুক্ত করে \
            বাংলাদেশ সাইবার নিরাপত্তা আইন ২০২৩ এর প্রযোজ্য বিধান অনুযায়ী যথাযথ \
            আইনগত ব্যবস্থা গ্রহণ করতে এবং নথিভুক্ত এজাহারের একটি অনুলিপি প্রদান করতে \
            মহোদয়কে অনুরোধ করছি।

            বিনীত নিবেদক,
            {{INFORMANT_NAME}}

            গুরুত্বপূর্ণ আউটপুট নির্দেশনা:
            ১. শুধুমাত্র এজাহার পত্রটি আউটপুট করুন। কোনো ভূমিকা, উপসংহার বা মন্তব্য করবেন না।
            ২. কোনো Markdown ব্যবহার করবেন না (**, ```, #, — নিষিদ্ধ)।
            ৩. সরবরাহকৃত তথ্যের বাইরে কোনো তথ্য যোগ করবেন না।
            ৪. বিবরণকে সুসংগত আনুষ্ঠানিক অনুচ্ছেদে বিস্তার করুন।
            ৫. সাক্ষী ক্ষেত্র খালি থাকলে লিখুন: "বর্তমানে কোনো সাক্ষী জানা নেই।"
            """;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Constructs the service.
     *
     * <p>Note: unlike {@code CyberShieldAIService}, we do NOT call
     * {@code .defaultSystem()} at build time because this service switches
     * between two different system prompts (EN / BN) per request. Instead,
     * the system prompt is injected dynamically per call via
     * {@code .prompt().system(…).user(…)}.
     *
     * @param builder       Spring AI's auto-configured {@link ChatClient.Builder}
     * @param firRepository JPA repository for persisting generated FIR records
     */
    public FIRGeneratorService(ChatClient.Builder builder, FIRRepository firRepository) {
        this.chatClient    = builder.build();
        this.firRepository = firRepository;
        log.info("FIRGeneratorService initialised — bilingual FIR generation ready");
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Compiles a complete, formally worded FIR from the validated request fields,
     * persists the result, and returns the saved {@link FIRObject}.
     *
     * <h3>Steps</h3>
     * <ol>
     *   <li>Select the correct system prompt template based on {@code request.language()}.</li>
     *   <li>Substitute all placeholder tokens in the user message.</li>
     *   <li>Call the configured LLM via {@link ChatClient} — synchronous / blocking.</li>
     *   <li>Strip any residual Markdown fences that the model may have included
     *       despite strict instructions (defensive post-processing).</li>
     *   <li>Build a {@link FIRObject} via the static factory, set the generated
     *       content, and persist it via {@link FIRRepository#save(Object)}.</li>
     *   <li>Return the saved entity (which now has an auto-assigned {@code id}
     *       and {@code createdAt} timestamp).</li>
     * </ol>
     *
     * @param request the validated, normalised FIR form submission
     * @return the persisted {@link FIRObject} containing the generated FIR text
     * @throws IllegalArgumentException  if {@code request} or {@code language} is null
     * @throws AiClassificationException if the LLM call fails or returns empty content
     */
    public FIRObject compileFIR(FIRRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("compileFIR: FIRRequest must not be null");
        }

        log.info("FIR generation requested — language={} informant='{}' station='{}'",
                request.language(), request.informantName(), request.policeStation());

        // ── Step 1: Select localised system prompt ────────────────────────────
        String systemPrompt = selectSystemPrompt(request.language());

        // ── Step 2: Build the user instruction message ────────────────────────
        String userMessage = buildUserMessage(request);
        log.debug("FIR user message built — {} chars", userMessage.length());

        // ── Step 3: Call the LLM ─────────────────────────────────────────────
        String rawOutput;
        try {
            rawOutput = chatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .content();

            log.debug("LLM raw FIR output received — {} chars", rawOutput != null ? rawOutput.length() : 0);

        } catch (Exception e) {
            log.error("LLM API call failed during FIR generation", e);
            throw new AiClassificationException(
                    "FIR generation failed — AI service unreachable: " + e.getMessage(), e);
        }

        if (rawOutput == null || rawOutput.isBlank()) {
            throw new AiClassificationException(
                    "FIR generation failed — LLM returned an empty response.");
        }

        // ── Step 4: Defensive Markdown stripping ─────────────────────────────
        String cleanedOutput = stripMarkdownFences(rawOutput);
        log.debug("Cleaned FIR output — {} chars", cleanedOutput.length());

        // ── Step 5: Persist FIRObject ─────────────────────────────────────────
        String    refNumber  = generateReferenceNumber();
        FIRObject firObject  = FIRObject.from(request, refNumber);
        firObject.setGeneratedContent(cleanedOutput);

        FIRObject saved = firRepository.save(firObject);

        log.info("FIR persisted — id={} refNumber='{}' language={} status={}",
                saved.getId(), saved.getFirReferenceNumber(),
                saved.getLanguage(), saved.getStatus());

        return saved;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Returns the appropriate system prompt constant based on the requested language.
     *
     * @param language ISO 639-1 code — {@code "EN"} or {@code "BN"}
     * @return the localised system prompt string
     * @throws IllegalArgumentException for any unsupported language code
     */
    private String selectSystemPrompt(String language) {
        return switch (language) {
            case "EN" -> SYSTEM_PROMPT_EN;
            case "BN" -> SYSTEM_PROMPT_BN;
            default   -> throw new IllegalArgumentException(
                    "Unsupported language code: '" + language + "'. Accepted values: EN, BN.");
        };
    }

    /**
     * Builds the user-turn message that instructs the LLM to populate
     * the FIR template with the supplied field values.
     *
     * <p>All field tokens use the same {@code {{FIELD_NAME}}} double-brace
     * syntax as the system prompt to avoid conflicts with Spring AI's own
     * single-brace template engine.
     *
     * @param req the validated FIR request
     * @return the fully assembled user instruction string
     */
    private String buildUserMessage(FIRRequest req) {
        return """
                Generate the FIR using the format specified in your system prompt.
                Substitute every {{TOKEN}} with the exact value provided below.
                Do NOT add any explanatory text outside the FIR document itself.

                FIELD VALUES:
                ──────────────────────────────────────────────────────────────
                POLICE_STATION   : %s
                SUBJECT          : %s
                PLACE            : %s
                DATE             : %s
                INFORMANT_NAME   : %s
                INFORMANT_DETAILS: %s
                OFFENDER_DETAILS : %s
                WITNESSES        : %s
                ──────────────────────────────────────────────────────────────

                NARRATIVE (expand this into formal legal paragraphs):
                %s
                ──────────────────────────────────────────────────────────────

                Output ONLY the FIR letter. No markdown. No backticks. No extra commentary.
                """.formatted(
                req.policeStation(),
                req.subject(),
                req.placeOfOffence(),
                req.dateOfOffence(),
                req.informantName(),
                blankOrDefault(req.informantDetails(), "Not provided"),
                blankOrDefault(req.offenderDetails(),  "Unknown / under investigation"),
                blankOrDefault(req.witnesses(),         "N/A"),
                req.chronologicalNarrative()
        );
    }

    /**
     * Defensively strips Markdown code fences and stray backtick sequences that
     * the LLM may include despite being explicitly instructed not to.
     *
     * <p>Handles the three most common artefact patterns:
     * <ul>
     *   <li>{@code ```text ... ```}</li>
     *   <li>{@code ``` ... ```}</li>
     *   <li>Leading/trailing single backticks</li>
     * </ul>
     *
     * @param raw the raw LLM output string
     * @return the cleaned FIR text
     */
    private String stripMarkdownFences(String raw) {
        // Remove leading/trailing whitespace
        String cleaned = raw.strip();

        // Strip ```language ... ``` or ``` ... ``` fences
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline != -1) {
                cleaned = cleaned.substring(firstNewline + 1);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.strip();
        }

        return cleaned;
    }

    /**
     * Generates a unique FIR reference number in the format
     * {@code CCID-CSA-YYYY-NNNN} (e.g., {@code CCID-CSA-2025-7423}).
     *
     * @return a collision-resistant reference number string
     */
    private String generateReferenceNumber() {
        int year   = Year.now().getValue();
        int serial = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "CCID-CSA-" + year + "-" + serial;
    }

    /**
     * Returns {@code value} if it is non-null and non-blank,
     * otherwise returns {@code defaultValue}.
     *
     * @param value        the candidate value
     * @param defaultValue the fallback
     * @return a non-blank string
     */
    private String blankOrDefault(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}

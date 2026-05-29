# CyberShield AI — Hackathon Implementation Checklist

> **Goal:** Build a working, demo-ready prototype in 3 days.
> Track progress by checking boxes as tasks are completed.
> Priority order within each phase matters — follow it top-to-bottom.

---

## 📅 Day 1 — Phase 1: Spring Boot Base Setup, Data Records & Seed Data

### 1.1 Project Scaffolding
- [ ] Verify Java 17 is active (`java -version`)
- [ ] Verify Maven is installed (`mvn -version`)
- [ ] Confirm `pom.xml` dependencies resolve (`mvn dependency:resolve`)
- [ ] Generate Maven Wrapper (`mvn wrapper:wrapper`)
- [ ] Create base package `com.cybershield` under `src/main/java/`
- [ ] Create `CybershieldApplication.java` with `@SpringBootApplication`

### 1.2 Configuration
- [ ] Create `src/main/resources/application.yml` with:
  - [ ] Server port: `8080`
  - [ ] H2 in-memory database connection (`spring.datasource.*`)
  - [ ] H2 console enabled for debugging (`spring.h2.console.enabled: true`)
  - [ ] JPA `ddl-auto: create-drop` for dev
  - [ ] Logging level `DEBUG` for `com.cybershield`
- [ ] Create `WebConfig.java` with CORS config allowing `http://localhost:3000`

### 1.3 Domain Models (JPA Entities)
- [ ] Create `enums/ReportCategory.java`
  - Values: `CYBERBULLYING`, `HARASSMENT`, `DATA_BREACH`, `IDENTITY_THEFT`, `HATE_SPEECH`, `SCAM`, `DOXXING`, `OTHER`
- [ ] Create `enums/SeverityLevel.java`
  - Values: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- [ ] Create `enums/ReportStatus.java`
  - Values: `PENDING`, `UNDER_REVIEW`, `RESOLVED`, `ESCALATED`, `CLOSED`
- [ ] Create `model/Report.java` entity with fields:
  - [ ] `id` (UUID, generated)
  - [ ] `title` (String, not null)
  - [ ] `description` (String, TEXT)
  - [ ] `category` (ReportCategory enum)
  - [ ] `severity` (SeverityLevel enum)
  - [ ] `status` (ReportStatus enum, default PENDING)
  - [ ] `reporterAlias` (String, nullable for anonymity)
  - [ ] `aiSummary` (String, nullable — filled by AI engine)
  - [ ] `aiConfidenceScore` (Double, nullable)
  - [ ] `createdAt` (LocalDateTime, auto)
  - [ ] `updatedAt` (LocalDateTime, auto)
- [ ] Create `model/Case.java` entity with fields:
  - [ ] `id` (UUID)
  - [ ] `caseNumber` (String, unique, generated)
  - [ ] `linkedReports` (List<Report>, OneToMany)
  - [ ] `assignedTo` (String)
  - [ ] `notes` (String)
  - [ ] `status` (ReportStatus enum)
  - [ ] `createdAt`, `updatedAt`

### 1.4 DTOs
- [ ] Create `dto/ReportRequestDTO.java` (input from frontend)
- [ ] Create `dto/ReportResponseDTO.java` (output to frontend, includes AI fields)
- [ ] Create `dto/AIClassificationDTO.java` (AI response wrapper)
- [ ] Create `dto/AnalyticsSummaryDTO.java` (dashboard metrics)

### 1.5 Repositories
- [ ] Create `repository/ReportRepository.java` extending `JpaRepository<Report, UUID>`
  - [ ] Add `findByCategory(ReportCategory)` query method
  - [ ] Add `findBySeverityOrderByCreatedAtDesc(SeverityLevel)` query method
  - [ ] Add `countByStatus(ReportStatus)` query method
- [ ] Create `repository/CaseRepository.java` extending `JpaRepository<Case, UUID>`

### 1.6 Service Layer
- [ ] Create `service/ReportService.java`
  - [ ] `createReport(ReportRequestDTO)` → saves entity, triggers async AI classification
  - [ ] `getAllReports()` → returns list of ResponseDTOs
  - [ ] `getReportById(UUID)` → finds or throws 404
  - [ ] `updateReportStatus(UUID, ReportStatus)` → PATCH operation
  - [ ] `deleteReport(UUID)` → soft delete (set status to CLOSED)
- [ ] Create `service/CaseService.java`
  - [ ] `createCaseFromReports(List<UUID>)` → groups reports into a case
  - [ ] `getAllCases()` → list all cases

### 1.7 REST Controllers
- [ ] Create `controller/ReportController.java`
  - [ ] `GET  /api/v1/reports`       → list all
  - [ ] `POST /api/v1/reports`       → create new
  - [ ] `GET  /api/v1/reports/{id}`  → get by ID
  - [ ] `PATCH /api/v1/reports/{id}` → update status
  - [ ] `DELETE /api/v1/reports/{id}`→ delete/close
- [ ] Create `controller/CaseController.java`
  - [ ] `GET  /api/v1/cases`         → list all cases
  - [ ] `POST /api/v1/cases`         → create case
- [ ] Create `controller/AnalyticsController.java`
  - [ ] `GET /api/v1/analytics/summary` → counts by status, severity, category
  - [ ] `GET /api/v1/analytics/trends`  → reports per day (last 30 days)

### 1.8 Seed Data
- [ ] Create `seed/DataSeeder.java` implementing `CommandLineRunner`
  - [ ] Insert 10–15 realistic `Report` records across all categories
  - [ ] Insert 3–5 `Case` records linking existing reports
  - [ ] Annotate with `@Profile("dev")` so it only runs in dev mode
- [ ] Verify seed data via H2 console at `http://localhost:8080/h2-console`

### 1.9 Day 1 Verification
- [ ] `mvn spring-boot:run` starts without errors
- [ ] `GET http://localhost:8080/api/v1/reports` returns 200 with seeded data
- [ ] `POST http://localhost:8080/api/v1/reports` creates a new report
- [ ] H2 console shows populated tables

---

## 📅 Day 2 — Phase 2: Spring AI Engine — Automated Classification

### 2.1 Spring AI Setup
- [ ] Confirm `spring-ai-openai-spring-boot-starter` (or Gemini) is in `pom.xml`
- [ ] Add `spring.ai.openai.api-key` to `application.yml` (or use env var `OPENAI_API_KEY`)
- [ ] Add `spring.ai.openai.chat.options.model: gpt-4o-mini` (cost-efficient)
- [ ] Add `spring.ai.openai.chat.options.temperature: 0.2` (deterministic outputs)
- [ ] Verify Spring AI auto-configuration boots (`ChatClient` bean present)

### 2.2 AI Service Implementation
- [ ] Create `service/AIService.java`
  - [ ] Inject `ChatClient` (Spring AI)
  - [ ] Implement `classifyReport(String title, String description)`:
    - [ ] Build a structured prompt with classification instructions
    - [ ] Define output schema: `{ category, severity, summary, confidenceScore }`
    - [ ] Parse JSON response into `AIClassificationDTO`
    - [ ] Handle API errors gracefully with fallback values
  - [ ] Implement `summarizeReport(String description)`:
    - [ ] Return a 2–3 sentence plain-language summary
  - [ ] Implement `calculateRiskScore(UUID reportId)`:
    - [ ] Fetch report, build context, return 0–100 risk integer
  - [ ] Implement `@Async classifyAndUpdateReport(UUID reportId)`:
    - [ ] Classify report post-creation, update entity fields via repository

### 2.3 AI Classification Prompt Engineering
- [ ] Create `config/PromptTemplates.java` with static prompt constants:
  - [ ] `CLASSIFICATION_PROMPT` — instructs AI to classify into enum values
  - [ ] `SUMMARIZATION_PROMPT` — instructs AI to summarize concisely
  - [ ] `RISK_SCORE_PROMPT` — instructs AI to return numeric score with rationale
- [ ] Test prompts manually via `POST /api/v1/ai/classify`

### 2.4 AI Controller
- [ ] Create `controller/AIController.java`
  - [ ] `POST /api/v1/ai/classify` → accepts `{ title, description }`, returns `AIClassificationDTO`
  - [ ] `POST /api/v1/ai/summarize` → accepts `{ text }`, returns `{ summary }`
  - [ ] `GET  /api/v1/ai/risk/{id}` → returns `{ reportId, riskScore, rationale }`

### 2.5 Async Integration
- [ ] Enable `@EnableAsync` in main application class or config
- [ ] Wire `AIService.classifyAndUpdateReport(id)` into `ReportService.createReport()`
- [ ] Verify AI fields (`aiSummary`, `aiConfidenceScore`) are populated after a few seconds

### 2.6 Error Handling & Resilience
- [ ] Create `exception/GlobalExceptionHandler.java` with `@RestControllerAdvice`
  - [ ] Handle `EntityNotFoundException` → 404
  - [ ] Handle `AIServiceException` (custom) → 503 with fallback message
  - [ ] Handle generic `Exception` → 500 with sanitized message
- [ ] Add retry logic for AI calls (Spring Retry or Resilience4j `@Retryable`)

### 2.7 Day 2 Verification
- [ ] `POST /api/v1/ai/classify` with sample text returns valid JSON classification
- [ ] New report submission auto-populates `aiSummary` within 5–10 seconds
- [ ] `GET /api/v1/ai/risk/{id}` returns a numeric score with rationale
- [ ] API key not found returns a graceful 503 (not a 500 stack trace)
- [ ] All existing Day 1 endpoints still function

---

## 📅 Day 3 — Phase 3: Nuxt 3 Client Interface & Dashboard Layout

### 3.1 Nuxt 3 Project Setup
- [ ] Scaffold Nuxt 3 app in `/client` directory:
  ```bash
  cd client && npx nuxi@latest init . --force
  ```
- [ ] Install dependencies:
  ```bash
  npm install @nuxtjs/tailwindcss @pinia/nuxt @nuxt/icon
  npm install chart.js vue-chartjs
  npm install lucide-vue-next
  ```
- [ ] Configure `nuxt.config.ts`:
  - [ ] Add modules: `@nuxtjs/tailwindcss`, `@pinia/nuxt`, `@nuxt/icon`
  - [ ] Set `runtimeConfig.public.apiBase: 'http://localhost:8080/api/v1'`
- [ ] Configure `tailwind.config.ts` with CyberShield color palette:
  - Primary: Cyberpunk teal/cyan (`#06B6D4`)
  - Accent: Warning amber (`#F59E0B`)
  - Danger: Alert red (`#EF4444`)
  - Background: Deep navy (`#0F172A`)
- [ ] Create `client/.env` with `NUXT_PUBLIC_API_BASE=http://localhost:8080/api/v1`

### 3.2 Composables (API Layer)
- [ ] Create `composables/useReports.ts`
  - [ ] `fetchReports()` — GET all reports
  - [ ] `fetchReport(id)` — GET single report
  - [ ] `submitReport(payload)` — POST new report
  - [ ] `updateStatus(id, status)` — PATCH status
- [ ] Create `composables/useAI.ts`
  - [ ] `classifyText(title, description)` — POST to AI classify
  - [ ] `getRiskScore(id)` — GET AI risk score
- [ ] Create `composables/useAnalytics.ts`
  - [ ] `fetchSummary()` — GET analytics summary
  - [ ] `fetchTrends()` — GET trend data

### 3.3 Pinia Stores
- [ ] Create `stores/reports.ts`
  - [ ] State: `reports`, `selectedReport`, `loading`, `error`
  - [ ] Actions: `loadReports()`, `addReport()`, `setSelected()`
- [ ] Create `stores/ui.ts`
  - [ ] State: `sidebarOpen`, `activeTab`, `notifications[]`
  - [ ] Actions: `toggleSidebar()`, `addNotification()`

### 3.4 Layout & Navigation
- [ ] Create `layouts/default.vue`:
  - [ ] Collapsible sidebar with navigation links
  - [ ] Top header bar with CyberShield AI logo, user avatar, notification bell
  - [ ] Dark theme background (`bg-slate-950`)
  - [ ] Glassmorphism sidebar effect (`backdrop-blur`, semi-transparent)
- [ ] Create `components/AppSidebar.vue` with links:
  - Dashboard, Reports, Cases, Analytics, AI Engine (admin)
- [ ] Create `components/AppHeader.vue` with:
  - Logo, breadcrumb, notification badge, theme toggle

### 3.5 Pages
- [ ] Create `pages/index.vue` — Landing / Login page:
  - [ ] Animated hero with CyberShield tagline
  - [ ] Login form (simulated JWT — hardcode for demo)
  - [ ] Stats preview strip (total reports, resolved cases)
- [ ] Create `pages/dashboard.vue`:
  - [ ] Metric cards: Total Reports, Active Cases, AI Classified, Critical Alerts
  - [ ] Recent activity feed
  - [ ] Quick-submit report button
- [ ] Create `pages/reports/index.vue`:
  - [ ] Data table of all reports (sortable, filterable by category/severity)
  - [ ] Status badge chips with color coding
  - [ ] AI confidence score indicator
  - [ ] "New Report" button → modal or slide-over
- [ ] Create `pages/reports/[id].vue`:
  - [ ] Full report detail view
  - [ ] AI Summary panel (highlighted box)
  - [ ] Risk Score gauge widget
  - [ ] Status update dropdown
  - [ ] Timeline of status changes
- [ ] Create `pages/analytics.vue`:
  - [ ] Bar chart: Reports per day (last 30 days)
  - [ ] Pie chart: Distribution by category
  - [ ] Severity breakdown donut chart
  - [ ] KPI summary row

### 3.6 Reusable Components
- [ ] Create `components/ReportForm.vue`:
  - [ ] Title, description textarea, category select, severity select
  - [ ] AI pre-classification button (calls classify endpoint on blur)
  - [ ] Submit button with loading spinner
- [ ] Create `components/CaseCard.vue`:
  - [ ] Case number, linked report count, severity badge, status chip
- [ ] Create `components/AIBadge.vue`:
  - [ ] Shows AI-detected category with confidence bar
  - [ ] Animated shimmer while AI is processing
- [ ] Create `components/SeverityBadge.vue`:
  - [ ] Color-coded pill: green/yellow/orange/red
- [ ] Create `components/charts/TrendChart.vue`:
  - [ ] Line chart using vue-chartjs (reports over time)
- [ ] Create `components/charts/CategoryPieChart.vue`:
  - [ ] Doughnut chart with legend

### 3.7 UX Polish
- [ ] Add page transition animations (`app.vue` `<NuxtPage>` with CSS transitions)
- [ ] Add loading skeletons for async data fetching
- [ ] Add toast notifications for report submission success/failure
- [ ] Add empty states for tables with no data (illustration + CTA)
- [ ] Ensure mobile responsiveness (sidebar collapses on `< md`)

### 3.8 Integration Testing (Full Stack)
- [ ] Ensure Spring Boot is running on port 8080
- [ ] Ensure Nuxt dev server is running on port 3000
- [ ] Submit a report via the Nuxt form → verify it appears in the reports list
- [ ] Wait ~10s → verify AI summary and confidence score populated
- [ ] Navigate to analytics → verify charts render with real data
- [ ] Test error state: stop Spring Boot, verify Nuxt shows graceful error message

### 3.9 Demo Preparation
- [ ] Create a "Demo Script" walkthrough (submit report → AI classifies → dashboard updates)
- [ ] Add demo seed data covering diverse report types
- [ ] Prepare slide: architecture diagram from `ARCHITECTURE.md`
- [ ] Record a short screen capture or prepare live demo flow
- [ ] Verify everything works with `npm run build` (production mode check)

---

## 🎯 Definition of "Done" (Hackathon Submission)

| Deliverable                              | Status |
|------------------------------------------|--------|
| Spring Boot API running on :8080         | ☐      |
| All REST endpoints functional            | ☐      |
| AI classification working end-to-end     | ☐      |
| Nuxt 3 frontend running on :3000         | ☐      |
| Dashboard with real API data             | ☐      |
| Report submission + AI auto-classification | ☐    |
| Analytics charts rendering               | ☐      |
| Responsive UI (mobile + desktop)         | ☐      |
| CORS properly configured                 | ☐      |
| Seed data for realistic demo             | ☐      |

---

*Last updated: Day 0 (Setup) — update this file as tasks are completed.*

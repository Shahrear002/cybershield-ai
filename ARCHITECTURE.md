# CyberShield AI — System Architecture

## Overview

CyberShield AI is a **decoupled, full-stack platform** for integrated digital rights management, safety monitoring, and justice reporting. The system separates concerns cleanly into an independently deployable frontend client and a backend API server, connected through a well-defined REST (and optionally WebSocket) boundary.

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER (Nuxt 3)                        │
│                      http://localhost:3000                           │
│                                                                     │
│  ┌───────────────┐  ┌────────────────┐  ┌────────────────────────┐  │
│  │  Pages /      │  │  Composables   │  │  Pinia Store           │  │
│  │  Dashboard    │  │  useReport()   │  │  auth / cases /        │  │
│  │  Reports      │  │  useAI()       │  │  notifications         │  │
│  │  Analytics    │  │  useAuth()     │  │                        │  │
│  └───────┬───────┘  └───────┬────────┘  └──────────┬─────────────┘  │
│          └──────────────────┴───────────────────────┘               │
│                              │  HTTP / SSE                          │
└──────────────────────────────┼──────────────────────────────────────┘
                               │
              ┌────────────────▼────────────────┐
              │        API Boundary              │
              │  Base URL: http://localhost:8080  │
              │  Prefix:   /api/v1               │
              │  CORS:     allowed origin :3000  │
              └────────────────┬────────────────┘
                               │
┌──────────────────────────────┼──────────────────────────────────────┐
│                       SERVER LAYER (Spring Boot 3.x)                │
│                        http://localhost:8080                         │
│                                                                     │
│  ┌─────────────────┐  ┌──────────────────┐  ┌───────────────────┐  │
│  │  REST Controllers│  │  Service Layer   │  │  Spring AI Engine │  │
│  │  /reports        │  │  ReportService   │  │  Classification   │  │
│  │  /cases          │  │  CaseService     │  │  Summarization    │  │
│  │  /analytics      │  │  AIService       │  │  Risk Scoring     │  │
│  │  /auth           │  │  UserService     │  │  (OpenAI/Gemini)  │  │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬──────────┘  │
│           └────────────────────┴────────────────────────┘           │
│                                │                                    │
│              ┌─────────────────▼──────────────────┐                 │
│              │          Data Layer                 │                 │
│              │  In-Memory (H2) → PostgreSQL        │                 │
│              │  JPA / Spring Data Repositories     │                 │
│              └────────────────────────────────────┘                 │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Component Breakdown

### Frontend — Nuxt 3 Client (`/client`)

| Concern          | Technology                        |
|------------------|-----------------------------------|
| Framework        | Nuxt 3 (Vue 3 + Composition API)  |
| Styling          | Tailwind CSS v3                   |
| State Management | Pinia                             |
| HTTP Client      | `$fetch` / `useFetch` (ofetch)    |
| Icons            | Heroicons / Lucide Vue            |
| Charts           | Chart.js via vue-chartjs          |
| Auth             | nuxt-auth-utils or custom JWT     |

**Key Nuxt Modules:**
- `@nuxtjs/tailwindcss`
- `@pinia/nuxt`
- `@nuxt/icon`

**Environment variable (`client/.env`):**
```env
NUXT_PUBLIC_API_BASE=http://localhost:8080/api/v1
```

---

### Backend — Spring Boot 3.x Server (`/server`)

| Concern          | Technology                              |
|------------------|-----------------------------------------|
| Framework        | Spring Boot 3.x (Spring MVC)            |
| Language         | Java 17                                 |
| Build Tool       | Maven                                   |
| AI Integration   | Spring AI (OpenAI / Gemini adapter)     |
| ORM              | Spring Data JPA + Hibernate 6           |
| Database (dev)   | H2 In-Memory                            |
| Database (prod)  | PostgreSQL                              |
| Boilerplate      | Lombok                                  |
| Serialization    | Jackson (spring-boot-starter-json)      |
| Validation       | Jakarta Bean Validation (Hibernate)     |

**Server runs on:** `http://localhost:8080`  
**API prefix:** `/api/v1`

---

## Communication Protocol

### REST API Conventions

```
GET    /api/v1/reports          → list all incident reports
POST   /api/v1/reports          → submit a new report
GET    /api/v1/reports/{id}     → get report by ID
PATCH  /api/v1/reports/{id}     → update report status
DELETE /api/v1/reports/{id}     → soft-delete report

POST   /api/v1/ai/classify      → AI classification of report text
POST   /api/v1/ai/summarize     → AI narrative summarization
GET    /api/v1/ai/risk/{id}     → AI risk score for a case

GET    /api/v1/analytics/summary → aggregated metrics dashboard
GET    /api/v1/analytics/trends  → time-series trend data
```

### CORS Configuration

The Spring Boot server is configured to accept cross-origin requests from the Nuxt dev server:

```java
// WebConfig.java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### Data Flow: Incident Report Submission

```
User fills Report Form (Nuxt)
      │
      ▼
POST /api/v1/reports  ──────────────────────────────────────┐
      │                                                      │
      ▼                                                  Spring Boot
ReportController receives DTO                           ReportController
      │                                                      │
      ▼                                                      │
ReportService.createReport()                                 │
      │                                                      │
      ├──► JPA: save to H2/PostgreSQL ◄────────────────────┘
      │
      ├──► AIService.classifyAsync(report)
      │         │
      │         └──► Spring AI → OpenAI/Gemini API
      │                   │
      │                   └──► Returns: category, severity, summary
      │
      └──► Response: ReportResponseDTO (201 Created)
                │
                ▼
        Nuxt updates Pinia store → re-renders dashboard
```

---

## Directory Structure

```
cybershield-ai/
├── ARCHITECTURE.md          ← This file
├── TODO.md                  ← Implementation checklist
├── pom.xml                  ← Maven build configuration
│
├── src/
│   ├── main/
│   │   ├── java/com/cybershield/
│   │   │   ├── CybershieldApplication.java
│   │   │   ├── config/
│   │   │   │   ├── WebConfig.java         (CORS)
│   │   │   │   └── SpringAIConfig.java    (AI provider setup)
│   │   │   ├── controller/
│   │   │   │   ├── ReportController.java
│   │   │   │   ├── CaseController.java
│   │   │   │   ├── AIController.java
│   │   │   │   └── AnalyticsController.java
│   │   │   ├── service/
│   │   │   │   ├── ReportService.java
│   │   │   │   ├── CaseService.java
│   │   │   │   └── AIService.java
│   │   │   ├── model/
│   │   │   │   ├── Report.java
│   │   │   │   ├── Case.java
│   │   │   │   └── enums/
│   │   │   │       ├── ReportCategory.java
│   │   │   │       └── SeverityLevel.java
│   │   │   ├── dto/
│   │   │   │   ├── ReportRequestDTO.java
│   │   │   │   ├── ReportResponseDTO.java
│   │   │   │   └── AIClassificationDTO.java
│   │   │   ├── repository/
│   │   │   │   ├── ReportRepository.java
│   │   │   │   └── CaseRepository.java
│   │   │   └── seed/
│   │   │       └── DataSeeder.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── data.sql                   (optional seed SQL)
│   └── test/
│       └── java/com/cybershield/
│
└── client/                                ← Nuxt 3 frontend
    ├── nuxt.config.ts
    ├── tailwind.config.ts
    ├── app.vue
    ├── pages/
    │   ├── index.vue                      (Landing / Login)
    │   ├── dashboard.vue
    │   ├── reports/
    │   │   ├── index.vue
    │   │   └── [id].vue
    │   └── analytics.vue
    ├── components/
    │   ├── ReportForm.vue
    │   ├── CaseCard.vue
    │   ├── AIBadge.vue
    │   └── charts/
    │       ├── TrendChart.vue
    │       └── CategoryPieChart.vue
    ├── composables/
    │   ├── useReports.ts
    │   └── useAI.ts
    └── stores/
        ├── reports.ts
        └── auth.ts
```

---

## Security Considerations (Hackathon Scope)

| Area              | Approach                                                       |
|-------------------|----------------------------------------------------------------|
| Authentication    | JWT Bearer tokens (simulated for hackathon)                    |
| API Key Safety    | OpenAI/Gemini keys stored in `application.yml` (not committed) |
| Data Privacy      | All PII fields annotated with `@JsonIgnore` in prod profile    |
| Rate Limiting     | Spring Boot Resilience4j or basic filter (Phase 2+)           |
| HTTPS             | Termination at reverse proxy level (out of hackathon scope)   |

---

## Development Quick-Start

```bash
# Start the Spring Boot backend
./mvnw spring-boot:run

# In a separate terminal — start the Nuxt 3 frontend
cd client
npm install
npm run dev
```

Backend: http://localhost:8080  
Frontend: http://localhost:3000  
H2 Console: http://localhost:8080/h2-console

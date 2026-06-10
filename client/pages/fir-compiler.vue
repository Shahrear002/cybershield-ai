<template>
  <div class="flex h-full flex-col overflow-hidden bg-navy-900">

    <!-- ═══════════════════════════════════════════════════════════════════════
         PAGE HEADER
         ═══════════════════════════════════════════════════════════════════════ -->
    <header class="flex flex-shrink-0 items-center justify-between gap-4 border-b border-border bg-navy-900 px-6 py-4">
      <div class="flex items-center gap-3">
        <div
          class="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg"
          style="background: linear-gradient(135deg, #6366F1 0%, #06B6D4 100%);"
        >
          <svg class="h-5 w-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
            <path stroke-linecap="round" stroke-linejoin="round"
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </div>
        <div>
          <h1 class="text-sm font-bold tracking-tight text-slate-100">FIR Compiler</h1>
          <p class="text-[11px] text-slate-500">Bilingual First Information Report generator · Bangladesh Cyber Security Act 2023</p>
        </div>
      </div>

      <!-- Saved badge — slides in after successful compile -->
      <Transition
        enter-active-class="transition duration-400 ease-out"
        enter-from-class="opacity-0 translate-x-4"
        enter-to-class="opacity-100 translate-x-0"
      >
        <div
          v-if="savedRecord"
          class="flex items-center gap-2 rounded-full border border-emerald-500/25 bg-emerald-500/06 px-3.5 py-1.5"
        >
          <span class="relative flex h-2 w-2">
            <span class="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-60" />
            <span class="relative inline-flex h-2 w-2 rounded-full bg-emerald-400" />
          </span>
          <svg class="h-3.5 w-3.5 flex-shrink-0 text-emerald-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m5.25-7.5l-3 3-1.5-1.5" />
          </svg>
          <span class="text-[11px] font-medium text-emerald-400">
            Saved to Secure Database Hub
            <span class="mx-1 rounded bg-emerald-400/12 px-1 font-mono text-[10px]">{{ savedRecord.firReferenceNumber }}</span>
            — Ready for Law Enforcement Relay
          </span>
        </div>
      </Transition>
    </header>

    <!-- ═══════════════════════════════════════════════════════════════════════
         SPLIT PANEL
         ═══════════════════════════════════════════════════════════════════════ -->
    <div class="flex flex-1 overflow-hidden">

      <!-- ─────────────────────────────────────────────────────────────────────
           LEFT: CONFIGURATION PANEL
           ───────────────────────────────────────────────────────────────────── -->
      <aside class="flex w-80 flex-shrink-0 flex-col overflow-hidden border-r border-border bg-navy-900">
        <div class="flex flex-1 flex-col gap-0 overflow-y-auto p-5 scrollbar-thin">

          <!-- ── Language Toggle ─────────────────────────────────────────── -->
          <section class="mb-5">
            <label class="fir-section-label">
              <svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129" />
              </svg>
              Output Language
            </label>
            <div class="mt-2 grid grid-cols-2 gap-1 rounded-lg border border-border bg-surface p-1">
              <button
                id="lang-en-btn"
                type="button"
                class="fir-lang-btn"
                :class="language === 'EN' ? 'fir-lang-btn--on' : 'fir-lang-btn--off'"
                @click="language = 'EN'"
              >
                <span class="text-sm leading-none">🇬🇧</span> English
              </button>
              <button
                id="lang-bn-btn"
                type="button"
                class="fir-lang-btn font-bengali"
                :class="language === 'BN' ? 'fir-lang-btn--on' : 'fir-lang-btn--off'"
                @click="language = 'BN'"
              >
                <span class="text-sm leading-none">🇧🇩</span> বাংলা
              </button>
            </div>
          </section>

          <!-- ── Case Metadata ───────────────────────────────────────────── -->
          <section class="mb-5">
            <label class="fir-section-label">
              <svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
              Recipient &amp; Complaint Subject
            </label>

            <div class="mt-2 flex flex-col gap-3">
              <div>
                <label class="fir-field-label" for="fir-station">Police Station / Unit</label>
                <input
                  id="fir-station"
                  v-model="policeStation"
                  type="text"
                  class="fir-input"
                  placeholder="e.g. CCID Head Office, Dhaka Metropolitan"
                />
              </div>
              <div>
                <label class="fir-field-label" for="fir-subject">Complaint Subject</label>
                <input
                  id="fir-subject"
                  v-model="subject"
                  type="text"
                  class="fir-input"
                  placeholder="e.g. Cybercrime Complaint — Unauthorized Access"
                />
              </div>
              <div class="grid grid-cols-2 gap-2">
                <div>
                  <label class="fir-field-label" for="fir-place">Place of Offence</label>
                  <input id="fir-place" v-model="placeOfOffence" type="text" class="fir-input" placeholder="e.g. Facebook Messenger" />
                </div>
                <div>
                  <label class="fir-field-label" for="fir-date">Date of Offence</label>
                  <input id="fir-date" v-model="dateOfOffence" type="text" class="fir-input" placeholder="15 May 2025" />
                </div>
              </div>
            </div>
          </section>

          <!-- ── Informant ────────────────────────────────────────────────── -->
          <section class="mb-5">
            <label class="fir-section-label">
              <svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              Informant (Complainant / Victim)
            </label>
            <div class="mt-2 flex flex-col gap-3">
              <div>
                <label class="fir-field-label" for="fir-name">Full Legal Name</label>
                <input id="fir-name" v-model="informantName" type="text" class="fir-input" placeholder="e.g. Nusrat Jahan" />
              </div>
              <div>
                <label class="fir-field-label" for="fir-details">Parent / Spouse / Address Details</label>
                <input id="fir-details" v-model="informantDetails" type="text" class="fir-input" placeholder="Father: Abdur Rahman, Dhaka, Bangladesh" />
              </div>
            </div>
          </section>

          <!-- ── Witnesses ────────────────────────────────────────────────── -->
          <section class="mb-5">
            <label class="fir-section-label">
              <svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20H2v-2a3 3 0 015.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              Witnesses
            </label>
            <div class="mt-2">
              <input id="fir-witnesses" v-model="witnesses" type="text" class="fir-input" placeholder="e.g. Sadia Akter (Friend, Phone: 017...)" />
            </div>
          </section>

          <!-- ── Triage Data Source ──────────────────────────────────────── -->
          <section class="mb-5">
            <label class="fir-section-label">
              <svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
              </svg>
              AI Triage Data
            </label>
            <div class="mt-2 flex flex-col gap-3">
              <div>
                <label class="fir-field-label" for="fir-transcript">Chronological Incident Narrative</label>
                <textarea
                  id="fir-transcript"
                  v-model="triageData.transcript"
                  class="fir-input resize-none"
                  rows="4"
                  placeholder="Paste the victim's raw transcript or incident description here…"
                />
              </div>
              <div>
                <label class="fir-field-label" for="fir-offender">Accused Handle / Number</label>
                <input id="fir-offender" v-model="triageData.primaryOffenderHandle" type="text" class="fir-input" placeholder="e.g. @darkh4ck3r" />
              </div>
              <div>
                <label class="fir-field-label" for="fir-hash">Evidence Hash <span class="text-slate-600">(from Vault)</span></label>
                <input id="fir-hash" v-model="triageData.evidenceHash" type="text" class="fir-input font-mono text-xs" placeholder="SHA-256 hash from Evidence Vault" />
              </div>
              <div class="grid grid-cols-2 gap-2">
                <div>
                  <label class="fir-field-label" for="fir-category">Crime Category</label>
                  <input id="fir-category" v-model="triageData.category" type="text" class="fir-input" placeholder="e.g. BLACKMAIL" />
                </div>
                <div>
                  <label class="fir-field-label" for="fir-justification">Legal Justification</label>
                  <input id="fir-justification" v-model="triageData.legalJustification" type="text" class="fir-input" placeholder="AI-provided statute note" />
                </div>
              </div>
            </div>
          </section>

          <!-- ── Action Buttons ─────────────────────────────────────────── -->
          <div class="mt-auto flex flex-col gap-2 pt-2 border-t border-border/40">
            <!-- Major Compile Button -->
            <button
              id="btn-compile"
              type="button"
              class="fir-btn-primary group relative overflow-hidden"
              :class="{ 'opacity-65 cursor-not-allowed': isCompiling }"
              :disabled="isCompiling"
              @click="compileDocument"
            >
              <span class="pointer-events-none absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/10 to-transparent transition-transform duration-700 group-hover:translate-x-full" />
              <template v-if="isCompiling">
                <svg class="h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                Compiling via Spring AI…
              </template>
              <template v-else>
                <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9.813 15.904L9 21l5.904-.813a2 2 0 001.12-.564L21 14.586a2 2 0 000-2.828l-1.758-1.758a2 2 0 00-2.828 0L11.586 14.78a2 2 0 00-.564 1.12z" />
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9.613 13.904L14 18M18 10L14 6" />
                </svg>
                {{ savedRecord ? 'Re-Compile Document' : 'Compile &amp; Save' }}
              </template>
            </button>

            <!-- PDF Download -->
            <button
              id="btn-download-pdf"
              type="button"
              class="fir-btn-secondary"
              :class="{ 'opacity-60 cursor-not-allowed': isExporting }"
              :disabled="isExporting"
              @click="downloadPDF"
            >
              <template v-if="isExporting">
                <svg class="h-4 w-4 animate-spin text-slate-400" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                Generating PDF…
              </template>
              <template v-else>
                <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                </svg>
                Download PDF Draft
              </template>
            </button>

            <!-- Print -->
            <button
              id="btn-print"
              type="button"
              class="fir-btn-secondary"
              @click="printDocument"
            >
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h6a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
              </svg>
              Print Document
            </button>

            <!-- Reset / Start New FIR -->
            <button
              id="btn-reset"
              type="button"
              class="fir-btn-secondary border-dashed border-red-500/30 hover:border-red-500/60 hover:bg-red-500/5 hover:text-red-400"
              @click="resetForm"
            >
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
              Reset Form
            </button>
          </div>

          <!-- ── Error Banners ────────────────────────────────────────────── -->
          <div class="mt-4 space-y-2">
            <Transition
              enter-active-class="transition duration-300"
              enter-from-class="opacity-0 -translate-y-1"
              enter-to-class="opacity-100 translate-y-0"
            >
              <div v-if="compileError" class="flex items-start gap-2 rounded-lg border border-red-500/20 bg-red-500/06 p-3 text-xs text-red-400">
                <svg class="mt-0.5 h-3.5 w-3.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <p><strong>Compile Failed:</strong> {{ compileError }}</p>
              </div>
            </Transition>

            <Transition
              enter-active-class="transition duration-300"
              enter-from-class="opacity-0 -translate-y-1"
              enter-to-class="opacity-100 translate-y-0"
            >
              <div v-if="pdfError" class="flex items-start gap-2 rounded-lg border border-red-500/20 bg-red-500/06 p-3 text-xs text-red-400">
                <svg class="mt-0.5 h-3.5 w-3.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <p><strong>Export Failed:</strong> {{ pdfError }}</p>
              </div>
            </Transition>
          </div>

        </div>
      </aside>

      <!-- ─────────────────────────────────────────────────────────────────────
           RIGHT: DOCUMENT PREVIEW PANEL
           ───────────────────────────────────────────────────────────────────── -->
      <section class="flex flex-1 flex-col overflow-hidden bg-slate-700/20">

        <!-- Preview toolbar bar -->
        <div class="flex flex-shrink-0 items-center justify-between gap-3 border-b border-border bg-navy-900 px-5 py-2.5">
          <div class="flex items-center gap-2.5">
            <div class="flex items-center gap-1.5">
              <svg class="h-3.5 w-3.5 text-slate-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
                <path stroke-linecap="round" stroke-linejoin="round" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
              </svg>
              <span class="text-xs text-slate-500">
                {{ savedRecord ? 'Official Compiled Complaint' : 'Live Preview — A4 Draft Document' }}
              </span>
            </div>
            <span
              class="rounded-full px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide"
              :class="activeLanguage === 'BN'
                ? 'border border-emerald-500/25 bg-emerald-500/08 text-emerald-400'
                : 'border border-indigo-500/25 bg-indigo-500/08 text-indigo-400'"
            >
              {{ activeLanguage === 'BN' ? 'বাংলা' : 'English' }}
            </span>
          </div>
          <span class="font-mono text-[10px] text-slate-600">{{ nowString }}</span>
        </div>

        <!-- Scrollable A4 canvas -->
        <div class="flex flex-1 items-start justify-center overflow-y-auto bg-gradient-to-b from-slate-800/40 to-slate-900/60 p-8">

          <!-- ─────────────────── A4 DOCUMENT ─────────────────── -->
          <div
            id="fir-document-preview"
            class="w-[210mm] min-h-[297mm] flex-shrink-0 rounded-sm bg-white text-slate-900 shadow-2xl"
            style="padding: 2.2cm 2cm 2cm; box-shadow: 0 0 0 1px rgba(0,0,0,0.1), 0 20px 60px rgba(0,0,0,0.5);"
            :class="activeLanguage === 'BN' ? 'font-bengali' : 'font-latin'"
          >


            <!-- ══ Document Header (Title & Date/Serial Parallel) ══════════ -->
            <div class="flex items-start justify-between gap-4 border-b-2 pb-4" style="border-color: #1a2e5a;">
              <div class="text-left">
                <h2 class="text-base font-bold uppercase tracking-widest whitespace-nowrap" style="color: #1a2e5a; letter-spacing: 0.15em;">
                  {{ activeLanguage === 'BN' ? 'এজাহার (প্রাথমিক তথ্য বিবরণী) - খসড়া' : 'FIRST INFORMATION REPORT (FIR) — DRAFT' }}
                </h2>
                <p class="mt-1 text-[10px] font-medium uppercase tracking-widest whitespace-nowrap" style="color: #718096;">
                  {{ activeLanguage === 'BN' ? 'বাংলাদেশ সাইবার নিরাপত্তা আইন ২০২৩ এর অধীনে' : 'Under the Bangladesh Cyber Security Act 2023' }}
                </p>
              </div>
              <div class="text-right text-xs whitespace-nowrap" style="color: #718096;">
                <p class="font-mono font-semibold" style="color: #1a2e5a;">
                  {{ savedRecord ? savedRecord.firReferenceNumber : docRefNumber }}
                </p>
                <p class="mt-0.5">{{ todayLong }}</p>
              </div>
            </div>

            <!-- If compiled successfully, render compiled letter from AI -->
            <template v-if="savedRecord && savedRecord.generatedContent">
              <div class="mt-6 text-sm leading-relaxed whitespace-pre-wrap font-serif text-slate-800 text-slate-900" style="color: #1a202c;">
                {{ savedRecord.generatedContent }}
              </div>
            </template>

            <!-- Else, show live Draft preview with structure binders -->
            <template v-else>
              <!-- Recipient block -->
              <div class="mt-6 text-sm leading-7" style="color: #1a202c;">
                <template v-if="activeLanguage === 'BN'">
                  <p>বরাবর,</p>
                  <p class="font-semibold">ভারপ্রাপ্ত কর্মকর্তা</p>
                  <p>{{ policeStation || '[থানার নাম]' }}</p>
                  <p class="mt-2">বিষয়: {{ subject || 'এজাহার প্রসঙ্গে।' }}</p>
                </template>
                <template v-else>
                  <p>To</p>
                  <p class="font-semibold">The Officer-In-Charge</p>
                  <p>{{ policeStation || '[Police Station]' }}</p>
                  <p class="mt-2">Subject: {{ subject || 'Entry of an Ejahar / First Information Report' }}</p>
                </template>
              </div>

              <!-- Metadata strip -->
              <div class="mt-4 grid grid-cols-3 gap-2 rounded border bg-slate-50 px-3 py-2 text-[10px]" style="border-color: #e2e8f0;">
                <div>
                  <p class="font-semibold uppercase tracking-wide" style="color: #718096;">{{ activeLanguage === 'BN' ? 'অপরাধের স্থান' : 'Place of Offence' }}</p>
                  <p class="mt-0.5 font-medium" style="color: #2d3748;">{{ placeOfOffence || '—' }}</p>
                </div>
                <div>
                  <p class="font-semibold uppercase tracking-wide" style="color: #718096;">{{ activeLanguage === 'BN' ? 'তারিখ' : 'Date of Offence' }}</p>
                  <p class="mt-0.5 font-medium" style="color: #2d3748;">{{ dateOfOffence || '—' }}</p>
                </div>
                <div>
                  <p class="font-semibold uppercase tracking-wide" style="color: #718096;">{{ activeLanguage === 'BN' ? 'অভিযোগকারীর নাম' : 'Informant Name' }}</p>
                  <p class="mt-0.5 font-medium" style="color: #2d3748;">{{ informantName || '—' }}</p>
                </div>
              </div>

              <!-- SECTION 1: Narrative -->
              <div class="mt-6">
                <h3 class="fir-doc-section-heading">
                  {{ activeLanguage === 'BN' ? '১। ঘটনার বিবরণ' : '1. NARRATIVE OF FACTS' }}
                </h3>
                <div class="mt-2 rounded border-l-4 pl-4 text-sm leading-relaxed font-serif text-slate-800" style="border-color: #1a2e5a;">
                  <template v-if="activeLanguage === 'BN'">
                    <p class="mb-2">
                      মহোদয়, বিনীত নিবেদন এই যে, আমি <strong>{{ informantName || '[নাম]' }}</strong>
                      <span v-if="informantDetails">, {{ informantDetails }},</span>
                      বাংলাদেশ সাইবার নিরাপত্তা আইন ২০২৩ এর অধীনে নিম্নলিখিত সাইবার অপরাধের অভিযোগ দায়ের করছি:
                    </p>
                    <p class="whitespace-pre-wrap italic bg-slate-50 p-2.5 rounded text-slate-600 border border-slate-100">
                      {{ narrativeText }}
                    </p>
                  </template>
                  <template v-else>
                    <p class="mb-2">
                      Respected Sir / Madam, I, <strong>{{ informantName || '[Informant Name]' }}</strong>
                      <span v-if="informantDetails">, {{ informantDetails }},</span>
                      most respectfully beg to state the following particulars of a cybercrime committed against me
                      under the Bangladesh Cyber Security Act 2023:
                    </p>
                    <p class="whitespace-pre-wrap italic bg-slate-50 p-2.5 rounded text-slate-600 border border-slate-100">
                      {{ narrativeText }}
                    </p>
                  </template>
                </div>
              </div>

              <!-- SECTION 2: Accused Details -->
              <div class="mt-6">
                <h3 class="fir-doc-section-heading">
                  {{ activeLanguage === 'BN' ? '২। অভিযুক্তের বিবরণ' : '2. ACCUSED DETAILS' }}
                </h3>
                <div class="mt-2 overflow-hidden rounded border text-sm" style="border-color: #e2e8f0;">
                  <table class="w-full border-collapse">
                    <tbody>
                      <tr class="border-b" style="border-color: #e2e8f0;">
                        <td class="w-1/3 bg-slate-50 px-3 py-2 text-[10px] font-semibold uppercase tracking-wide" style="color: #718096;">
                          {{ activeLanguage === 'BN' ? 'পরিচিত হ্যান্ডেল / নম্বর' : 'Known Handle / Number' }}
                        </td>
                        <td class="px-3 py-2 font-mono font-medium" style="color: #1a202c;">
                          {{ triageData.primaryOffenderHandle || (activeLanguage === 'BN' ? 'তদন্তাধীন' : 'Under Investigation') }}
                        </td>
                      </tr>
                      <tr>
                        <td class="w-1/3 bg-slate-50 px-3 py-2 text-[10px] font-semibold uppercase tracking-wide" style="color: #718096;">
                          {{ activeLanguage === 'BN' ? 'অপরাধ শ্রেণী' : 'Crime Category' }}
                        </td>
                        <td class="px-3 py-2 font-semibold" style="color: #c53030;">
                          {{ triageData.category || (activeLanguage === 'BN' ? 'শ্রেণীবদ্ধ নয়' : 'Unclassified') }}
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <!-- Witnesses Row in Draft -->
              <div class="mt-6">
                <h3 class="fir-doc-section-heading">
                  {{ activeLanguage === 'BN' ? 'সাক্ষীগণের বিবরণ' : 'Witnesses' }}
                </h3>
                <p class="mt-2 text-sm text-slate-700">
                  {{ witnesses || (activeLanguage === 'BN' ? 'বর্তমানে কোনো সাক্ষী জানা নেই।' : 'No witnesses are known at this time.') }}
                </p>
              </div>

              <!-- Closing Prayer & Signature Block -->
              <div class="mt-8 text-sm leading-7" style="color: #1a202c;">
                <p>
                  {{ activeLanguage === 'BN'
                    ? 'অতএব, মহোদয়ের নিকট বিনীত প্রার্থনা এই যে, উপরোক্ত অভিযোগটি নথিভুক্ত করে বাংলাদেশ সাইবার নিরাপত্তা আইন ২০২৩ এর প্রযোজ্য বিধান অনুযায়ী যথাযথ আইনগত ব্যবস্থা গ্রহণ করতে মহোদয়কে অনুরোধ করছি।'
                    : 'As such I would request you to kindly register this complaint, take proper legal steps under the applicable provisions of the Bangladesh Cyber Security Act 2023, and provide me with a copy of the registered FIR.'
                  }}
                </p>
                <p class="mt-4">
                  {{ activeLanguage === 'BN' ? 'বিনীত নিবেদক,' : 'Yours Sincerely,' }}
                </p>
                <p class="font-semibold">{{ informantName || (activeLanguage === 'BN' ? '[নাম]' : '[Name]') }}</p>
              </div>
            </template>

            <!-- ══ SECTION 3: Digital Evidence (Always Shown) ════════════════════ -->
            <div class="mt-6 break-inside-avoid">
              <h3 class="fir-doc-section-heading">
                {{ activeLanguage === 'BN' ? '৩। ডিজিটাল প্রমাণ সংরক্ষণ' : '3. DIGITAL EVIDENCE PRESERVATION' }}
              </h3>
              <div class="mt-2 rounded border p-3 text-xs leading-relaxed" style="border-color: #bee3f8; background: #ebf8ff; color: #2c5282;">
                <p class="font-semibold">
                  {{ activeLanguage === 'BN'
                    ? '✓ সাইবারশিল্ড লেজারের মাধ্যমে ডিজিটাল লগ এবং টাইমস্ট্যাম্প সুরক্ষিত করা হয়েছে।'
                    : '✓ Digital logs and timestamps secured via CyberShield Ledger.' }}
                </p>
                <p class="mt-2 font-mono">
                  <span class="font-sans font-semibold not-italic" style="color: #718096;">Hash: </span>
                  <span class="break-all" style="color: #2d3748;">{{ triageData.evidenceHash || '[SHA-256 hash — generate from Evidence Vault]' }}</span>
                </p>
                <p class="mt-2" style="color: #4a5568;">
                  {{ activeLanguage === 'BN'
                    ? 'এই হ্যাশ পরিবর্তন-প্রতিরোধী এবং আদালতে প্রমাণ হিসাবে উপস্থাপনযোগ্য।'
                    : 'This hash is tamper-evident and may be presented as admissible evidence in court proceedings.' }}
                </p>
              </div>
            </div>

            <!-- ══ SECTION 4: AI Triage Assessment (Always Shown) ════════════════ -->
            <div class="mt-6 break-inside-avoid">
              <h3 class="fir-doc-section-heading">
                {{ activeLanguage === 'BN' ? '৪। এআই ট্রায়াজ মূল্যায়ন (শুধুমাত্র পুলিশের বিবেচনার জন্য)' : '4. AI TRIAGE ASSESSMENT (FOR POLICE CONSIDERATION ONLY)' }}
              </h3>
              <div class="mt-2 rounded border-2 border-dashed p-4" style="border-color: #f6ad55; background: #fffaf0;">
                <div class="flex items-start gap-3">
                  <div class="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full" style="background: #fbd38d;">
                    <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="#c05621" stroke-width="2">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                  </div>
                  <div class="flex-1">
                    <p class="text-[10px] font-bold uppercase tracking-widest" style="color: #c05621;">
                      {{ activeLanguage === 'BN' ? 'সতর্কতা — স্বয়ংক্রিয় মূল্যায়ন' : 'Automated Assessment — Advisory Only' }}
                    </p>
                    <p class="mt-1.5 text-xs leading-relaxed" style="color: #7b341e;">
                      {{ activeLanguage === 'BN'
                        ? 'নিম্নলিখিত মূল্যায়ন কঠোরভাবে তদন্তকারী কর্মকর্তার জন্য প্রদান করা হয়েছে। এটি কোনো আইনি অভিযোগ গঠন করে না।'
                        : 'The following is an automated assessment provided strictly for the investigating officer. It does not constitute a legal charge and must be independently verified by a qualified law enforcement professional.'
                      }}
                    </p>
                    <div class="mt-3 rounded border p-2.5 text-xs" style="border-color: #fbd38d; background: rgba(255,255,255,0.6);">
                      <p class="font-semibold" style="color: #744210;">
                        {{ activeLanguage === 'BN' ? 'প্রস্তাবিত ধারা (তদন্ত সহায়ক):' : 'Suggested Relevant Statute:' }}
                      </p>
                      <p class="mt-1 leading-relaxed" style="color: #7b341e;">
                        {{ activeLanguage === 'BN'
                          ? `সাইবার নিরাপত্তা আইন ২০২৩ — ${triageData.legalJustification || 'তদন্তাধীন ধারা নির্ধারণ করুন'}`
                          : `Cyber Security Act 2023 — ${triageData.legalJustification || 'Statute mapping under review'}`
                        }}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Signature / Seal row (Always Shown) -->
            <div class="mt-12 grid grid-cols-2 gap-8 border-t pt-6 text-center text-[10px] break-inside-avoid" style="border-color: #e2e8f0;">
              <div class="flex flex-col items-center gap-2">
                <div class="h-14 w-36 border-b border-dashed" style="border-color: #a0aec0;" />
                <p class="font-bold" style="color: #4a5568;">
                  {{ activeLanguage === 'BN' ? 'অভিযোগকারীর স্বাক্ষর' : 'Signature of Complainant / Victim' }}
                </p>
              </div>
              <div class="flex flex-col items-center gap-2">
                <div class="flex h-14 w-36 items-center justify-center rounded border text-[8px] uppercase tracking-widest" style="border-color: #a0aec0; color: #a0aec0;">
                  [OFFICIAL SEAL]
                </div>
                <p class="font-bold" style="color: #4a5568;">
                  {{ activeLanguage === 'BN' ? 'ডিউটি অফিসার, সাইবার ক্রাইম বিভাগ' : 'Duty Officer, Cyber Crime Division' }}
                </p>
              </div>
            </div>

            <!-- Document Footer (Always Shown) -->
            <div class="mt-8 border-t pt-4 break-inside-avoid" style="border-color: #e2e8f0;">
              <div class="flex items-center justify-between text-[9px]" style="color: #a0aec0;">
                <span>Generated by CyberShield AI Platform v0.1 — {{ todayLong }}</span>
                <span class="font-mono">
                  {{ savedRecord ? 'Status: RECORD SAVED · ' + savedRecord.status : 'Status: LOCAL DRAFT — NOT OFFICIALLY FILED' }}
                </span>
              </div>
              <p class="mt-1 text-[8px] italic leading-relaxed" style="color: #cbd5e0;">
                {{ activeLanguage === 'BN'
                  ? 'এই নথিটি AI সিস্টেম দ্বারা তৈরি করা হয়েছে এবং সরকারি দাখিলের আগে আইনি পর্যলোচনার প্রয়োজন হতে পারে।'
                  : 'This document was compiled by an AI system. Its contents reflect information provided by the informant and have not been independently verified. Legal review is required before official submission.'
                }}
              </p>
            </div>

          </div>
          <!-- /a4 document -->

        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'

// ── Page meta ─────────────────────────────────────────────────────────────────
useHead({
  title: 'FIR Compiler — CyberShield AI',
  meta: [
    {
      name: 'description',
      content: 'Bilingual FIR / Ejahar compiler for cybercrime victims. Generates court-ready PDF documents in English and Bangla under the Bangladesh Cyber Security Act 2023.'
    }
  ]
})

// ── Types ─────────────────────────────────────────────────────────────────────
interface SavedFIR {
  id: number
  firReferenceNumber: string
  status: string
  generatedContent: string
  language?: 'EN' | 'BN'
}

// ── State — language & form fields ───────────────────────────────────────────
const language        = ref<'EN' | 'BN'>('EN')
const policeStation   = ref('CCID Head Office, Dhaka Metropolitan')
const subject         = ref('Cybercrime Complaint — Unauthorized Access and Extortion')
const placeOfOffence  = ref('')
const dateOfOffence   = ref('')
const informantName   = ref('')
const informantDetails = ref('')
const witnesses       = ref('')

const config = useRuntimeConfig()

// ── triageData — populated from AI triage or manually entered ─────────────────
const triageData = reactive({
  transcript:             '',
  primaryOffenderHandle:  '',
  evidenceHash:           '',
  category:               '',
  legalJustification:     '',
})

// ── UI state ──────────────────────────────────────────────────────────────────
const isCompiling = ref(false)
const isExporting = ref(false)
const compileError = ref<string | null>(null)
const pdfError    = ref<string | null>(null)
const savedRecord = ref<SavedFIR | null>(null)

// ── Computed helpers ──────────────────────────────────────────────────────────
const activeLanguage = computed<'EN' | 'BN'>(() => {
  if (savedRecord.value && savedRecord.value.language) {
    return savedRecord.value.language.toUpperCase() as 'EN' | 'BN'
  }
  return language.value
})

const nowString = computed(() =>
  new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
)

const todayLong = computed(() =>
  new Date().toLocaleDateString('en-GB', {
    day: '2-digit', month: 'long', year: 'numeric',
    timeZone: 'Asia/Dhaka'
  })
)

const docRefNumber = computed(() => {
  const year   = new Date().getFullYear()
  const serial = Math.floor(1000 + Math.random() * 9000)
  return `CCID-CSA-${year}-${serial}`
})

const narrativeText = ref('')

const updateNarrative = () => {
  if (activeLanguage.value === 'EN') {
    narrativeText.value = localStorage.getItem('cybershield_summary_en') || 'English summary generation pending...'
  } else {
    narrativeText.value = localStorage.getItem('cybershield_summary_bn') || 'বাংলা সারসংক্ষেপ জেনারেশন পেন্ডিং...'
  }
}

watch(activeLanguage, () => {
  updateNarrative()
})

// ── Clock Ticker & LocalStorage Retrieval ──────────────────────────────────────
let _clockInterval: ReturnType<typeof setInterval>

onMounted(() => {
  _clockInterval = setInterval(() => { /* triggers reactivity via nowString */ }, 1000)

  // 1. Retrieve Triage Data from Chatbot Workflow
  try {
    const rawTriage = localStorage.getItem('cybershield_triage_data')
    if (rawTriage) {
      const parsed = JSON.parse(rawTriage)
      if (parsed.transcript) triageData.transcript = parsed.transcript
      if (parsed.primaryOffenderHandle) triageData.primaryOffenderHandle = parsed.primaryOffenderHandle
      if (parsed.category) triageData.category = parsed.category
      if (parsed.legalJustification) triageData.legalJustification = parsed.legalJustification

      // Intelligently prepopulate Subject and Informant details
      if (parsed.category) {
        subject.value = `Cybercrime Complaint under CSA 2023 — AI Classified: ${parsed.category}`
      }
    }
  } catch (err) {
    console.error('Failed to parse cybershield_triage_data from localStorage:', err)
  }

  // 2. Retrieve last secure evidence hash from Vault
  try {
    const lastHash = localStorage.getItem('cybershield_last_evidence_hash')
    if (lastHash) {
      triageData.evidenceHash = lastHash
    }
  } catch (err) {
    console.error('Failed to retrieve cybershield_last_evidence_hash from localStorage:', err)
  }

  // 3. Initialize narrative
  updateNarrative()
})

// ── Backend Spring AI Integration ─────────────────────────────────────────────
async function compileDocument() {
  isCompiling.value = true
  compileError.value = null

  try {
    const payload = {
      language:               language.value,
      policeStation:          policeStation.value,
      subject:                subject.value,
      placeOfOffence:         placeOfOffence.value,
      dateOfOffence:          dateOfOffence.value,
      informantName:          informantName.value,
      informantDetails:       informantDetails.value,
      offenderDetails:        triageData.primaryOffenderHandle,
      chronologicalNarrative: triageData.transcript,
      witnesses:              witnesses.value
    }

    const result = await $fetch<SavedFIR>('/api/fir/generate', {
      baseURL: config.public.apiBase,
      method:  'POST',
      body:    payload
    })

    savedRecord.value = result
  } catch (err: any) {
    const msg = err.data?.message ?? err.message ?? 'Spring Boot server unreachable.'
    compileError.value = `Failed to generate FIR: ${msg}`
  } finally {
    isCompiling.value = false
  }
}

function resetForm() {
  // 1. Clear form state
  language.value = 'EN'
  policeStation.value = 'CCID Head Office, Dhaka Metropolitan'
  subject.value = 'Cybercrime Complaint — Unauthorized Access and Extortion'
  placeOfOffence.value = ''
  dateOfOffence.value = ''
  informantName.value = ''
  informantDetails.value = ''
  witnesses.value = ''
  
  // 2. Clear triageData
  triageData.transcript = ''
  triageData.primaryOffenderHandle = ''
  triageData.evidenceHash = ''
  triageData.category = ''
  triageData.legalJustification = ''
  
  // 3. Clear UI state
  savedRecord.value = null
  compileError.value = null
  pdfError.value = null
  
  // 4. Remove cached item from localStorage
  try {
    localStorage.removeItem('cybershield_triage_data')
    localStorage.removeItem('cybershield_last_evidence_hash')
  } catch (err) {
    console.error('Failed to clear localStorage items:', err)
  }
}

// ── PDF Download via html2pdf.js ──────────────────────────────────────────────
async function downloadPDF() {
  pdfError.value    = null
  isExporting.value = true

  try {
    // Dynamically import html2pdf.js (browser-only, avoids SSR issues in Nuxt)
    const html2pdf = (await import('html2pdf.js')).default

    const element = document.getElementById('fir-document-preview')
    if (!element) throw new Error('Preview element not found in DOM.')

    const filename = `FIR_REPORT_${activeLanguage.value}_${new Date().getFullYear()}-${Date.now()}.pdf`

    const options = {
      margin:       [10, 10, 10, 10],       // mm top right bottom left
      filename,
      image:        { type: 'jpeg', quality: 0.98 },
      html2canvas:  {
        scale:       2,                      // 2× for retina-sharp text
        useCORS:     true,
        letterRendering: true,
        windowWidth: element.scrollWidth,
      },
      jsPDF: {
        unit:        'mm',
        format:      'a4',
        orientation: 'portrait',
        compress:    true,
      },
      pagebreak: { mode: ['avoid-all', 'css', 'legacy'] },
    }

    await html2pdf().set(options).from(element).save()

  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : String(err)
    pdfError.value = `PDF export failed — ${msg}`
  } finally {
    isExporting.value = false
  }
}

// ── Print ─────────────────────────────────────────────────────────────────────
function printDocument() {
  window.print()
}
</script>

<style scoped>
/* ── Fonts ─────────────────────────────────────────────────────────────────── */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+Bengali:wght@400;500;600;700&family=Libre+Baskerville:ital,wght@0,400;0,700;1,400&display=swap');

.font-bengali { font-family: 'Noto Serif Bengali', serif; }
.font-latin   { font-family: 'Libre Baskerville', Georgia, 'Times New Roman', serif; }

/* ── Form primitives ─────────────────────────────────────────────────────── */
.fir-section-label {
  @apply flex items-center gap-1.5 text-[10px] font-semibold uppercase tracking-widest text-slate-500;
}

.fir-field-label {
  @apply mb-1 block text-[11px] font-medium text-slate-400;
}

.fir-input {
  @apply w-full rounded-md border border-border bg-surface px-3 py-2 text-xs text-slate-200
         placeholder-slate-600 outline-none transition-all duration-150
         focus:border-indigo-500/50 focus:ring-1 focus:ring-indigo-500/20;
  font-family: inherit;
}

/* ── Language toggle ─────────────────────────────────────────────────────── */
.fir-lang-btn {
  @apply flex items-center justify-center gap-1.5 rounded-md px-3 py-2 text-xs font-medium
         transition-all duration-150 cursor-pointer border-0 outline-none;
}

.fir-lang-btn--on {
  @apply text-white shadow-sm;
  background: linear-gradient(135deg, #6366F1 0%, #4F46E5 100%);
  box-shadow: 0 2px 8px rgba(99,102,241,0.35);
}

.fir-lang-btn--off {
  @apply bg-transparent text-slate-500 hover:text-slate-300;
}

/* ── Action buttons ──────────────────────────────────────────────────────── */
.fir-btn-primary {
  @apply flex w-full items-center justify-center gap-2 rounded-lg py-3 text-sm font-semibold text-white
         transition-all duration-200;
  background: linear-gradient(135deg, #6366F1 0%, #06B6D4 100%);
  box-shadow: 0 2px 12px rgba(99,102,241,0.3);
}
.fir-btn-primary:hover:not(:disabled) {
  box-shadow: 0 4px 20px rgba(99,102,241,0.45);
  transform: translateY(-1px);
}
.fir-btn-primary:active:not(:disabled) { transform: translateY(0); }

.fir-btn-secondary {
  @apply flex w-full items-center justify-center gap-2 rounded-lg border border-border bg-surface py-2.5
         text-sm font-medium text-slate-400 transition-all duration-150
         hover:border-border-bright hover:text-slate-200;
}

/* ── A4 document section headings ────────────────────────────────────────── */
.fir-doc-section-heading {
  font-size: 0.625rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: #1a2e5a;
  padding-bottom: 0.25rem;
  border-bottom: 1px solid #bee3f8;
  margin-bottom: 0;
}

/* ── Print media ─────────────────────────────────────────────────────────── */
@media print {
  body * { visibility: hidden !important; }
  #fir-document-preview, #fir-document-preview * { visibility: visible !important; }
  #fir-document-preview {
    position: fixed !important;
    inset: 0 !important;
    width: 21cm !important;
    min-height: 29.7cm !important;
    margin: 0 !important;
    padding: 2cm !important;
    box-shadow: none !important;
    border: none !important;
    background: white !important;
    color: black !important;
    font-size: 11pt !important;
  }
}

/* ── Scrollbar ───────────────────────────────────────────────────────────── */
.scrollbar-thin {
  scrollbar-width: thin;
  scrollbar-color: rgba(255,255,255,0.06) transparent;
}
</style>

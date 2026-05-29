<template>
  <div class="min-h-full px-6 py-8 lg:px-10">

    <!-- ══════════════════════════════════════════════════════════════════════
         PAGE HEADER
         ══════════════════════════════════════════════════════════════════════ -->
    <div class="mb-8 animate-fade-in">
      <div class="flex items-center gap-3">
        <div class="flex h-10 w-10 items-center justify-center rounded-xl"
          style="background: linear-gradient(135deg, rgba(6,182,212,0.15), rgba(99,102,241,0.15)); border: 1px solid rgba(6,182,212,0.25);">
          <svg class="h-5 w-5 text-cyber-cyan" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
            <path stroke-linecap="round" stroke-linejoin="round"
              d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
          </svg>
        </div>
        <div>
          <h1 class="text-xl font-bold tracking-tight text-slate-100">AI Triage Engine</h1>
          <p class="text-sm text-slate-500">Powered by Spring AI · Bangladesh Cyber Security Act 2023</p>
        </div>
        <!-- AI status pill -->
        <div class="ml-auto flex items-center gap-2 rounded-full border border-cyber-cyan/20 bg-cyber-cyan/5 px-3 py-1.5">
          <span class="relative flex h-1.5 w-1.5">
            <span class="absolute inline-flex h-full w-full animate-ping rounded-full bg-cyber-cyan opacity-60" />
            <span class="relative inline-flex h-1.5 w-1.5 rounded-full bg-cyber-cyan" />
          </span>
          <span class="text-xs font-medium text-cyber-cyan">LLM Ready</span>
        </div>
      </div>
    </div>

    <!-- ── Toast Alert Banner for Chat Auto-Analysis ── -->
    <div
      v-if="showSuccessBanner"
      class="mb-6 animate-slide-up rounded-xl border border-cyber-emerald/30 bg-cyber-emerald/5 px-4 py-3.5 shadow-md"
    >
      <div class="flex items-center gap-3">
        <div class="flex h-7 w-7 items-center justify-center rounded-lg bg-cyber-emerald/10 border border-cyber-emerald/30">
          <svg class="h-4.5 w-4.5 text-cyber-emerald" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="flex-1">
          <p class="text-xs text-slate-300 font-medium">
            ✅ Case Analyzed Successfully from Chat. Please review the details below and click 'Generate FIR PDF' to finalize your document.
          </p>
        </div>
        <button
          @click="showSuccessBanner = false"
          class="rounded px-2 py-1 text-xs text-slate-500 hover:bg-surface hover:text-slate-300 transition"
        >
          Dismiss
        </button>
      </div>
    </div>

    <!-- ══════════════════════════════════════════════════════════════════════
         TWO-COLUMN GRID (stacks on mobile)
         ══════════════════════════════════════════════════════════════════════ -->
    <div class="grid gap-6 lg:grid-cols-2">

      <!-- ── LEFT: Input panel ──────────────────────────────────────────── -->
      <div class="animate-slide-up flex flex-col gap-5" style="animation-delay: 0.05s;">

        <!-- Transcript input card -->
        <div class="rounded-xl border border-border bg-surface p-5 shadow-panel">
          <div class="mb-4 flex items-center justify-between">
            <div>
              <h2 class="text-sm font-semibold text-slate-200">Incident Transcript</h2>
              <p class="mt-0.5 text-xs text-slate-600">Paste or type the raw chat history / report below</p>
            </div>
            <button
              v-if="transcript"
              @click="transcript = ''; analysis = null; apiError = null"
              class="rounded-md px-2 py-1 text-xs text-slate-600 transition hover:bg-surface-raised hover:text-slate-400"
            >
              Clear
            </button>
          </div>

          <!-- Textarea -->
          <div class="relative">
            <textarea
              id="transcript-input"
              v-model="transcript"
              rows="16"
              placeholder="Example: @darkh4ck3r told me he had stolen my password from a data breach and demanded $500 or he would post my private photos publicly…"
              class="w-full resize-none rounded-lg border border-border bg-navy-800 px-4 py-3 font-mono text-sm leading-relaxed text-slate-300 placeholder-slate-700 transition-all duration-150 focus:border-cyber-cyan/50 focus:outline-none focus:ring-1 focus:ring-cyber-cyan/30"
              style="min-height: 260px;"
              :disabled="loading"
            />
            <!-- Character counter -->
            <span class="absolute bottom-2.5 right-3 font-mono text-[10px] text-slate-700">
              {{ transcript.length }} chars
            </span>
          </div>

          <!-- Demo seeds -->
          <div class="mt-3 flex flex-wrap gap-2">
            <p class="mr-1 self-center text-xs text-slate-700">Try:</p>
            <button
              v-for="seed in DEMO_SEEDS"
              :key="seed.label"
              @click="transcript = seed.text"
              class="rounded-md border border-border px-2.5 py-1 text-xs text-slate-600 transition hover:border-border-bright hover:text-slate-400"
            >
              {{ seed.label }}
            </button>
          </div>
        </div>

        <!-- Submit button -->
        <button
          id="analyze-btn"
          @click="analyze"
          :disabled="!transcript.trim() || loading"
          class="group relative flex w-full items-center justify-center gap-3 overflow-hidden rounded-xl py-4 text-sm font-semibold text-white transition-all duration-200 disabled:cursor-not-allowed disabled:opacity-40"
          style="background: linear-gradient(135deg, #0EA5E9 0%, #6366F1 60%, #8B5CF6 100%);"
        >
          <!-- Shimmer overlay on hover -->
          <span
            class="pointer-events-none absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/10 to-transparent transition-transform duration-700 group-hover:translate-x-full"
          />

          <template v-if="loading">
            <svg class="h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
              <path class="opacity-75" fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
            <span>Analyzing with AI Engine…</span>
          </template>
          <template v-else>
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round"
                d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
            </svg>
            <span>Analyze &amp; Structure Case File</span>
          </template>
        </button>

        <!-- API error alert -->
        <div
          v-if="apiError"
          class="animate-fade-in rounded-xl border border-cyber-red/30 bg-cyber-red/5 px-4 py-3"
        >
          <div class="flex items-start gap-3">
            <svg class="mt-0.5 h-4 w-4 flex-shrink-0 text-cyber-red" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round"
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <div>
              <p class="text-sm font-medium text-cyber-red">Classification Failed</p>
              <p class="mt-0.5 text-xs text-red-400/70">{{ apiError }}</p>
            </div>
          </div>
        </div>

      </div>

      <!-- ── RIGHT: Analysis results panel ─────────────────────────────── -->
      <div class="flex flex-col gap-5">

        <!-- Empty state -->
        <div
          v-if="!analysis && !loading"
          class="animate-fade-in flex h-full min-h-64 flex-col items-center justify-center rounded-xl border border-dashed border-border text-center"
        >
          <div class="mb-3 flex h-12 w-12 items-center justify-center rounded-full"
            style="background: rgba(6,182,212,0.06); border: 1px solid rgba(6,182,212,0.15);">
            <svg class="h-6 w-6 text-slate-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round"
                d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
            </svg>
          </div>
          <p class="text-sm text-slate-600">Analysis results will appear here</p>
          <p class="mt-1 text-xs text-slate-700">Enter a transcript and click Analyze</p>
        </div>

        <!-- Loading skeleton -->
        <div v-if="loading" class="flex flex-col gap-4 animate-fade-in">
          <div v-for="i in 3" :key="i"
            class="rounded-xl border border-border bg-surface p-5"
          >
            <div class="mb-3 h-3 w-1/3 rounded-full bg-surface-raised shimmer-element" />
            <div class="space-y-2">
              <div class="h-2.5 w-full rounded-full bg-surface-raised shimmer-element" />
              <div class="h-2.5 w-3/4 rounded-full bg-surface-raised shimmer-element" />
            </div>
          </div>
        </div>

        <!-- Results panels -->
        <template v-if="analysis && !loading">

          <!-- ── 1. Classification badge card ───────────────────────────── -->
          <div class="animate-slide-up rounded-xl border bg-surface p-5 shadow-panel"
            :class="categoryStyle(analysis.category).border"
          >
            <div class="mb-4 flex items-center justify-between">
              <h3 class="text-xs font-semibold uppercase tracking-widest text-slate-600">
                Incident Category
              </h3>
              <span class="font-mono text-[10px] text-slate-700">CSA-2023</span>
            </div>

            <div class="flex items-center gap-4">
              <!-- Icon circle -->
              <div
                class="flex h-14 w-14 flex-shrink-0 items-center justify-center rounded-xl text-2xl shadow-lg"
                :class="categoryStyle(analysis.category).icon"
              >
                {{ categoryStyle(analysis.category).emoji }}
              </div>
              <div>
                <!-- Category name badge -->
                <div class="mb-2 inline-flex items-center gap-2 rounded-lg px-3 py-1.5"
                  :class="categoryStyle(analysis.category).badge"
                >
                  <span class="h-1.5 w-1.5 rounded-full" :class="categoryStyle(analysis.category).dot" />
                  <span class="text-sm font-bold tracking-wide">
                    {{ analysis.category.replace('_', ' ') }}
                  </span>
                </div>
                <!-- Offender handle -->
                <p class="text-xs text-slate-600">
                  Primary offender:
                  <code class="ml-1 rounded bg-surface-raised px-1.5 py-0.5 font-mono text-slate-300">
                    {{ analysis.primaryOffenderHandle }}
                  </code>
                </p>
              </div>
            </div>

            <!-- Severity chips row -->
            <div class="mt-4 flex items-center gap-2 border-t border-border pt-4">
              <span :class="['rounded px-2 py-1 text-xs font-semibold', severityStyle(analysis.riskScore)]">
                {{ severityLabel(analysis.riskScore) }}
              </span>
              <span v-if="analysis.riskScore >= 75"
                class="flex items-center gap-1.5 rounded border border-cyber-red/30 bg-cyber-red/10 px-2 py-1 text-xs font-semibold text-cyber-red">
                <svg class="h-3 w-3" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd"/>
                </svg>
                Escalation Required
              </span>
            </div>
          </div>

          <!-- ── 2. Risk score bar ───────────────────────────────────────── -->
          <div class="animate-slide-up rounded-xl border border-border bg-surface p-5 shadow-panel"
            style="animation-delay: 0.1s;">
            <div class="mb-4 flex items-center justify-between">
              <h3 class="text-xs font-semibold uppercase tracking-widest text-slate-600">Risk Assessment</h3>
              <span class="font-mono text-2xl font-bold" :class="riskScoreColor(analysis.riskScore)">
                {{ analysis.riskScore.toFixed(1) }}<span class="ml-0.5 text-sm text-slate-600">/100</span>
              </span>
            </div>

            <!-- Progress bar track -->
            <div class="relative h-3 overflow-hidden rounded-full bg-navy-800">
              <!-- Background gradient zones -->
              <div class="absolute inset-0 rounded-full"
                style="background: linear-gradient(90deg, #10B981 0%, #F59E0B 45%, #F97316 70%, #EF4444 100%); opacity: 0.15;" />
              <!-- Animated fill bar -->
              <div
                class="relative h-full rounded-full transition-all duration-1000 ease-out"
                :style="{
                  width: `${analysis.riskScore}%`,
                  background: riskBarGradient(analysis.riskScore),
                  boxShadow: `0 0 12px 2px ${riskGlowColor(analysis.riskScore)}`
                }"
              />
            </div>

            <!-- Scale labels -->
            <div class="mt-2 flex justify-between">
              <span class="text-[10px] font-medium text-cyber-emerald">LOW</span>
              <span class="text-[10px] font-medium text-cyber-amber">MEDIUM</span>
              <span class="text-[10px] font-medium text-cyber-orange">HIGH</span>
              <span class="text-[10px] font-medium text-cyber-red">CRITICAL</span>
            </div>

            <!-- Score breakdown grid -->
            <div class="mt-4 grid grid-cols-2 gap-3 border-t border-border pt-4">
              <div v-for="metric in riskMetrics(analysis.riskScore)" :key="metric.label"
                class="rounded-lg bg-navy-800 px-3 py-2"
              >
                <p class="text-[10px] text-slate-700">{{ metric.label }}</p>
                <p class="mt-0.5 font-mono text-sm font-semibold" :class="metric.color">{{ metric.value }}</p>
              </div>
            </div>
          </div>

          <!-- ── 3. Legal justification card ────────────────────────────── -->
          <div class="animate-slide-up rounded-xl border border-border bg-surface p-5 shadow-panel"
            style="animation-delay: 0.18s;">
            <div class="mb-4 flex items-center gap-2">
              <svg class="h-4 w-4 text-cyber-indigo" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
                <path stroke-linecap="round" stroke-linejoin="round"
                  d="M12 3v17.25m0 0c-1.472 0-2.882.265-4.185.75M12 20.25c1.472 0 2.882.265 4.185.75M18.75 4.97A48.416 48.416 0 0012 4.5c-2.291 0-4.545.16-6.75.47m13.5 0c1.01.143 2.01.317 3 .52m-3-.52l2.62 10.726c.122.499-.106 1.028-.589 1.202a5.988 5.988 0 01-2.031.352 5.988 5.988 0 01-2.031-.352c-.483-.174-.711-.703-.59-1.202L18.75 4.971zm-16.5.52c.99-.203 1.99-.377 3-.52m0 0l2.62 10.726c.122.499-.106 1.028-.589 1.202a5.989 5.989 0 01-2.031.352 5.989 5.989 0 01-2.031-.352c-.483-.174-.711-.703-.59-1.202L5.25 4.971z" />
              </svg>
              <h3 class="text-xs font-semibold uppercase tracking-widest text-slate-600">Legal Justification</h3>
              <span class="ml-auto rounded border border-cyber-indigo/25 bg-cyber-indigo/10 px-1.5 py-0.5 text-[9px] font-semibold uppercase text-cyber-indigo">
                Auto-cited
              </span>
            </div>

            <!-- Quote block -->
            <div class="relative rounded-lg bg-navy-800 px-4 py-3.5">
              <span class="absolute left-0 top-0 h-full w-0.5 rounded-l-lg" style="background: linear-gradient(180deg, #6366F1, #8B5CF6);" />
              <p class="pl-1 font-mono text-xs leading-relaxed text-slate-400">
                {{ analysis.legalJustification }}
              </p>
            </div>

            <!-- Action row -->
            <div class="mt-4 flex items-center gap-2 border-t border-border pt-4">
              <button @click="copyJustification"
                class="flex items-center gap-1.5 rounded-lg border border-border px-3 py-1.5 text-xs text-slate-500 transition hover:border-border-bright hover:text-slate-300">
                <svg class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round"
                    d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
                {{ copied ? 'Copied!' : 'Copy Citation' }}
              </button>
              
              <!-- FIR Button -->
              <button @click="openFirModal"
                class="flex items-center gap-1.5 rounded-lg border border-cyber-cyan/30 bg-cyber-cyan/5 px-3 py-1.5 text-xs text-cyber-cyan transition hover:bg-cyber-cyan/15">
                <svg class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round"
                    d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                Generate FIR
              </button>

              <NuxtLink to="/vault"
                class="ml-auto flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium text-cyber-cyan transition hover:text-cyber-blue">
                <svg class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round"
                    d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
                Secure Evidence →
              </NuxtLink>
            </div>
          </div>

        </template>
      </div>
    </div>

    <!-- FIR MODAL overlay -->
    <div v-if="firModalOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-fade-in no-print">
      <div class="relative w-full max-w-4xl max-h-[90vh] flex flex-col rounded-2xl border border-border bg-slate-900 shadow-2xl overflow-hidden">
        
        <!-- Modal header -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-border bg-navy-800">
          <div class="flex items-center gap-2">
            <svg class="h-5 w-5 text-cyber-cyan" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <span class="text-sm font-bold text-slate-100 font-sans">First Information Report (FIR) Draft</span>
          </div>
          <button @click="firModalOpen = false" class="rounded-lg p-1.5 text-slate-500 hover:bg-surface-raised hover:text-slate-300">
            <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <!-- Modal Content (The Paper FIR) -->
        <div class="flex-1 overflow-y-auto p-8 bg-slate-950 flex justify-center">
          <!-- White Legal Paper Document -->
          <div id="fir-document" class="w-full max-w-[21cm] p-[1.5cm] bg-white text-slate-900 shadow-lg rounded-sm border border-slate-200 font-serif leading-relaxed text-sm">
            
            <!-- Document header -->
            <div class="text-center border-b-2 border-slate-900 pb-4 mb-6">
              <h2 class="text-lg font-bold uppercase tracking-wider text-slate-900 font-sans">FORM No. 53</h2>
              <h3 class="text-xs font-semibold text-slate-600 tracking-wide font-sans mb-2">[Section 154 of Code of Criminal Procedure]</h3>
              <h1 class="text-2xl font-black uppercase tracking-widest text-slate-900 font-sans mb-1">FIRST INFORMATION REPORT</h1>
              <p class="text-[10px] font-bold uppercase text-slate-500 font-sans tracking-wide">Cyber Crime Investigation Division · Bangladesh Police</p>
            </div>

            <!-- Info Grid -->
            <div class="grid grid-cols-2 gap-y-3 gap-x-6 border-b border-slate-300 pb-4 mb-6 text-xs font-sans">
              <div>
                <span class="text-slate-500 block uppercase font-bold tracking-wider text-[9px] mb-0.5">1. District &amp; PS:</span>
                <span class="font-bold text-slate-800">CCID Head Office, Dhaka Metropolitan</span>
              </div>
              <div>
                <span class="text-slate-500 block uppercase font-bold tracking-wider text-[9px] mb-0.5">2. FIR Number / Reference:</span>
                <span class="font-mono font-bold text-slate-800">{{ firRefNumber }}</span>
              </div>
              <div>
                <span class="text-slate-500 block uppercase font-bold tracking-wider text-[9px] mb-0.5">3. Date &amp; Time of Report:</span>
                <span class="font-bold text-slate-800">{{ currentDateTimeString() }}</span>
              </div>
              <div>
                <span class="text-slate-500 block uppercase font-bold tracking-wider text-[9px] mb-0.5">4. Statutory Classification:</span>
                <span class="font-bold text-slate-900">
                  Act: Cyber Security Act, 2023
                </span>
              </div>
            </div>

            <!-- Sections List -->
            <div class="mb-6">
              <h4 class="font-sans font-bold text-xs uppercase text-slate-500 tracking-wider mb-2">5. Offence Details &amp; Applicable Clauses</h4>
              <table class="w-full text-xs font-sans border border-slate-200 text-left">
                <thead>
                  <tr class="bg-slate-50 border-b border-slate-200 font-bold">
                    <th class="p-2 border-r border-slate-200 text-[10px] uppercase text-slate-500 tracking-wider">Category</th>
                    <th class="p-2 border-r border-slate-200 text-[10px] uppercase text-slate-500 tracking-wider">Reported Offender</th>
                    <th class="p-2 text-[10px] uppercase text-slate-500 tracking-wider">Legal Justification / Act Citation</th>
                  </tr>
                </thead>
                <tbody>
                  <tr class="border-b border-slate-100">
                    <td class="p-2 border-r border-slate-200 font-bold text-slate-800">{{ analysis.category.replace('_', ' ') }}</td>
                    <td class="p-2 border-r border-slate-200 font-mono text-slate-800">@{{ analysis.primaryOffenderHandle }}</td>
                    <td class="p-2 text-slate-700 italic">{{ analysis.legalJustification }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Statement Section -->
            <div class="mb-6">
              <h4 class="font-sans font-bold text-xs uppercase text-slate-500 tracking-wider mb-2">6. Statement of Complaint (Victim Transcript)</h4>
              <div class="p-4 bg-slate-50 border border-slate-200 rounded font-mono text-xs text-slate-800 whitespace-pre-wrap leading-relaxed">
                {{ transcript }}
              </div>
            </div>

            <!-- Evidence Reference Section (Vault integration) -->
            <div class="mb-8">
              <h4 class="font-sans font-bold text-xs uppercase text-slate-500 tracking-wider mb-2">7. Secured Digital Evidence &amp; Blockchain Receipt</h4>
              <p class="text-xs text-slate-600 mb-2">The following digital footprint has been cryptographicially anchored as evidence under Section 43 of CSA-2023:</p>
              <div class="p-3 border border-slate-200 rounded font-sans text-xs bg-slate-50">
                <div class="flex justify-between mb-1">
                  <span class="text-slate-500">Chain Status:</span>
                  <span class="text-emerald-700 font-bold tracking-wide text-[10px]">SECURED ON ETHEREUM INTEGRITY LEDGER (VERIFIED)</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-slate-500">Timestamp:</span>
                  <span class="font-mono text-slate-800">{{ currentDateTimeString() }}</span>
                </div>
              </div>
            </div>

            <!-- Signature/Seal Blocks -->
            <div class="mt-16 pt-10 border-t border-slate-300 grid grid-cols-2 text-center text-xs font-sans gap-8">
              <div class="flex flex-col items-center">
                <div class="h-16 w-32 border-b border-dashed border-slate-400 mb-2 flex items-center justify-center text-slate-400 italic">[Signature of Complainant]</div>
                <span class="font-bold text-slate-700">Signature of Complainant / Victim</span>
              </div>
              <div class="flex flex-col items-center">
                <div class="h-16 w-32 border border-slate-300 rounded mb-2 flex items-center justify-center text-[9px] text-slate-400 uppercase tracking-widest">[OFFICIAL SEAL]</div>
                <span class="font-bold text-slate-700">Duty Officer, Cyber Crime Division</span>
              </div>
            </div>

          </div>
        </div>

        <!-- Modal Footer Actions -->
        <div class="flex items-center gap-3 px-6 py-4 border-t border-border bg-navy-800">
          <span class="text-xs text-slate-500">Ready to submit to the CCID Division of Bangladesh Police</span>
          <button @click="printFir" class="ml-auto flex items-center gap-2 rounded-xl bg-cyber-cyan px-4 py-2.5 text-sm font-semibold text-slate-950 transition hover:bg-cyber-cyan-bright">
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M17 17h2a2 2 0 002-2v-4a2 2 0 00-2-2H5a2 2 0 00-2 2v4a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-4a2 2 0 00-2-2H9a2 2 0 00-2 2v4a2 2 0 002 2zm8-12V5a2 2 0 00-2-2H9a2 2 0 00-2 2v4h10z" />
            </svg>
            Print Official FIR
          </button>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

useHead({ title: 'AI Triage Engine' })

// ── Runtime config ─────────────────────────────────────────────────────────
const config = useRuntimeConfig()

const route = useRoute()
const router = useRouter()

// ── State ──────────────────────────────────────────────────────────────────
const chatSummary  = useChatSummary()
const transcript   = ref(chatSummary.value || '')
const loading      = ref(false)
const showSuccessBanner = ref(false)
const apiError     = ref<string | null>(null)
const copied       = ref(false)
const firModalOpen = ref(false)
const firRefNumber = ref('')

interface TriageAnalysis {
  category:               string
  primaryOffenderHandle:  string
  riskScore:              number
  legalJustification:     string
}

const analysis = ref<TriageAnalysis | null>(null)

// ── Demo seed transcripts ──────────────────────────────────────────────────
const DEMO_SEEDS = [
  {
    label: 'Hacking',
    text: '@cr4ck3r_99 claimed he had obtained my passwords from a recent data breach and installed keylogging malware on my laptop. He demanded I pay 500 USD in Bitcoin or he would exploit my email credentials to access my bank accounts.',
  },
  {
    label: 'Blackmail',
    text: '"ShadowUser" got hold of private photos of me and sent a message demanding $1,000 within 24 hours or he would post them on public forums. He said he had all my contacts\' numbers and would send them to everyone.',
  },
  {
    label: 'Harassment',
    text: '@bullysquad_bd has been posting screenshots of my private messages across 12 Facebook groups and bully-tagging my coworkers. They threatened to keep posting unless I delete my profile and stop advocating online.',
  },
]

// ── API call ───────────────────────────────────────────────────────────────
async function analyze() {
  if (!transcript.value.trim()) return

  loading.value  = true
  apiError.value = null
  analysis.value = null

  try {
    const result = await $fetch<TriageAnalysis>('/api/triage/classify', {
      baseURL: config.public.apiBase,
      method:  'POST',
      headers: { 'Content-Type': 'text/plain' },
      body:    transcript.value,
    })
    analysis.value = result
  } catch (err: unknown) {
    const e = err as { data?: { message?: string }; message?: string }
    apiError.value =
      e?.data?.message ??
      e?.message ??
      'Unable to reach the CyberShield AI server. Make sure Spring Boot is running on :8080.'
  } finally {
    loading.value = false
  }
}

// ── Copy justification to clipboard ───────────────────────────────────────
async function copyJustification() {
  if (!analysis.value) return
  await navigator.clipboard.writeText(analysis.value.legalJustification)
  copied.value = true
  setTimeout(() => { copied.value = false }, 2000)
}

// ── FIR modal helpers ──────────────────────────────────────────────────────
function openFirModal() {
  if (analysis.value) {
    const triagePayload = {
      transcript: transcript.value,
      primaryOffenderHandle: analysis.value.primaryOffenderHandle || '',
      category: analysis.value.category || '',
      legalJustification: analysis.value.legalJustification || ''
    }
    localStorage.setItem('cybershield_triage_data', JSON.stringify(triagePayload))
  }
  navigateTo('/fir-compiler')
}

function printFir() {
  window.print()
}

function currentDateTimeString() {
  return new Date().toLocaleString('en-US', {
    timeZone: 'Asia/Dhaka',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: true
  }) + ' (GMT+6)'
}

// ── Visual helpers ─────────────────────────────────────────────────────────
function categoryStyle(cat: string) {
  const styles: Record<string, { border: string; icon: string; badge: string; dot: string; emoji: string }> = {
    HACKING: {
      border: 'border-cyber-cyan/30',
      icon:   'bg-cyber-cyan/10 ring-1 ring-cyber-cyan/25',
      badge:  'bg-cyber-cyan/10 border border-cyber-cyan/25 text-cyber-cyan',
      dot:    'bg-cyber-cyan',
      emoji:  '⚡',
    },
    HARASSMENT: {
      border: 'border-cyber-amber/30',
      icon:   'bg-cyber-amber/10 ring-1 ring-cyber-amber/25',
      badge:  'bg-cyber-amber/10 border border-cyber-amber/25 text-cyber-amber',
      dot:    'bg-cyber-amber',
      emoji:  '⚠️',
    },
    BLACKMAIL: {
      border: 'border-cyber-red/30',
      icon:   'bg-cyber-red/10 ring-1 ring-cyber-red/25',
      badge:  'bg-cyber-red/10 border border-cyber-red/25 text-cyber-red',
      dot:    'bg-cyber-red',
      emoji:  '🔒',
    },
    DIGITAL_ABUSE: {
      border: 'border-cyber-purple/30',
      icon:   'bg-cyber-purple/10 ring-1 ring-cyber-purple/25',
      badge:  'bg-cyber-purple/10 border border-cyber-purple/25 text-cyber-purple',
      dot:    'bg-cyber-purple',
      emoji:  '🛡️',
    },
  }
  return styles[cat] ?? styles.HARASSMENT
}

function riskScoreColor(score: number): string {
  if (score >= 85) return 'text-cyber-red'
  if (score >= 65) return 'text-cyber-orange'
  if (score >= 35) return 'text-cyber-amber'
  return 'text-cyber-emerald'
}

function riskBarGradient(score: number): string {
  if (score >= 85) return 'linear-gradient(90deg, #F97316, #EF4444)'
  if (score >= 65) return 'linear-gradient(90deg, #F59E0B, #F97316)'
  if (score >= 35) return 'linear-gradient(90deg, #10B981, #F59E0B)'
  return 'linear-gradient(90deg, #10B981, #34D399)'
}

function riskGlowColor(score: number): string {
  if (score >= 85) return 'rgba(239,68,68,0.35)'
  if (score >= 65) return 'rgba(249,115,22,0.35)'
  if (score >= 35) return 'rgba(245,158,11,0.35)'
  return 'rgba(16,185,129,0.35)'
}

function severityLabel(score: number): string {
  if (score >= 85) return 'CRITICAL'
  if (score >= 65) return 'HIGH'
  if (score >= 35) return 'MEDIUM'
  return 'LOW'
}

function severityStyle(score: number): string {
  if (score >= 85) return 'bg-cyber-red/10 border border-cyber-red/25 text-cyber-red'
  if (score >= 65) return 'bg-cyber-orange/10 border border-cyber-orange/25 text-cyber-orange'
  if (score >= 35) return 'bg-cyber-amber/10 border border-cyber-amber/25 text-cyber-amber'
  return 'bg-cyber-emerald/10 border border-cyber-emerald/25 text-cyber-emerald'
}

function riskMetrics(score: number) {
  return [
    { label: 'Risk Score',  value: `${score.toFixed(1)} / 100`, color: riskScoreColor(score) },
    { label: 'Priority',    value: severityLabel(score),         color: riskScoreColor(score) },
    { label: 'Escalate?',   value: score >= 75 ? 'Yes — Urgent' : 'No', color: score >= 75 ? 'text-cyber-red' : 'text-cyber-emerald' },
    { label: 'Review SLA',  value: score >= 85 ? 'Immediate' : score >= 65 ? '24 hrs' : score >= 35 ? '48 hrs' : '7 days', color: 'text-slate-400' },
  ]
}

onMounted(async () => {
  // If we have an incoming chat summary, inject it into the transcript input area
  if (chatSummary.value) {
    transcript.value = chatSummary.value
  }

  // If auto-analyze is requested, run analysis and clear state/query
  if (route.query.autoAnalyze === 'true') {
    if (transcript.value.trim()) {
      await analyze()
      if (analysis.value) {
        showSuccessBanner.value = true
      }
    }
    // Clean up to prevent re-triggering on subsequent page reloads
    chatSummary.value = ''
    router.replace({ path: route.path, query: {} })
  }
})
</script>

<style scoped>
.shimmer-element {
  background: linear-gradient(90deg, #141D2E 25%, #1A2436 50%, #141D2E 75%);
  background-size: 200% 100%;
  animation: shimmer 1.8s linear infinite;
}
@keyframes shimmer {
  0%   { background-position: -200% center; }
  100% { background-position:  200% center; }
}

/* ── Print Media Styles ──────────────────────────────────────────────────── */
@media print {
  body * {
    visibility: hidden !important;
  }
  #fir-document, #fir-document * {
    visibility: visible !important;
  }
  #fir-document {
    position: absolute !important;
    left: 0 !important;
    top: 0 !important;
    width: 21cm !important;
    min-height: 29.7cm !important;
    margin: 0 !important;
    padding: 2cm !important;
    box-shadow: none !important;
    border: none !important;
    background: white !important;
    color: black !important;
  }
  .no-print {
    display: none !important;
  }
}
</style>

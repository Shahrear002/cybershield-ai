<template>
  <div class="min-h-full px-6 py-8 lg:px-10">

    <!-- ══════════════════════════════════════════════════════════════════════
         PAGE HEADER
         ══════════════════════════════════════════════════════════════════════ -->
    <div class="mb-8 animate-fade-in">
      <div class="flex items-center gap-3">
        <div class="flex h-10 w-10 items-center justify-center rounded-xl"
          style="background: linear-gradient(135deg, rgba(99,102,241,0.15), rgba(139,92,246,0.15)); border: 1px solid rgba(99,102,241,0.25);">
          <svg class="h-5 w-5 text-cyber-indigo" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
            <path stroke-linecap="round" stroke-linejoin="round"
              d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
          </svg>
        </div>
        <div>
          <h1 class="text-xl font-bold tracking-tight text-slate-100">Evidence Vault (Hybrid Storage)</h1>
          <p class="text-sm text-slate-500">MCP Filesystem Storage · Blockchain Cryptographic Anchoring</p>
        </div>
        <div class="ml-auto flex items-center gap-2 rounded-full border border-cyber-indigo/20 bg-cyber-indigo/5 px-3 py-1.5">
          <svg class="h-3 w-3 text-cyber-indigo" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd" />
          </svg>
          <span class="text-xs font-medium text-cyber-indigo">Chain of Custody</span>
        </div>
      </div>
    </div>

    <div class="grid gap-6 lg:grid-cols-2">

      <!-- ── LEFT: Upload panel ─────────────────────────────────────────── -->
      <div class="animate-slide-up flex flex-col gap-5" style="animation-delay: 0.05s;">

        <!-- How it works -->
        <div class="flex gap-4 rounded-xl border border-border bg-surface p-4">
          <div v-for="step in HOW_IT_WORKS" :key="step.n"
            class="flex flex-1 flex-col items-center text-center">
            <span class="mb-2 flex h-7 w-7 items-center justify-center rounded-full border border-border-bright font-mono text-xs text-slate-500">
              {{ step.n }}
            </span>
            <p class="text-[11px] font-medium text-slate-400">{{ step.title }}</p>
            <p class="mt-0.5 text-[10px] text-slate-700">{{ step.desc }}</p>
          </div>
        </div>

        <!-- Drag-and-drop zone -->
        <div
          id="drop-zone"
          class="relative cursor-pointer rounded-xl border-2 border-dashed transition-all duration-200"
          :class="isDragging
            ? 'border-cyber-indigo/70 bg-cyber-indigo/5 scale-[1.01]'
            : 'border-border hover:border-border-bright hover:bg-surface'"
          @dragover.prevent="isDragging = true"
          @dragleave.prevent="isDragging = false"
          @drop.prevent="onDrop($event)"
          @click="fileInputRef?.click()"
        >
          <input
            ref="fileInputRef"
            type="file"
            class="sr-only"
            @change="onFileChange($event)"
          />

          <div class="flex flex-col items-center justify-center px-6 py-12 text-center">
            <div
              class="mb-4 flex h-14 w-14 items-center justify-center rounded-xl transition-all duration-200"
              :class="isDragging ? 'bg-cyber-indigo/20' : 'bg-surface-raised'"
            >
              <svg class="h-7 w-7 transition-colors" :class="isDragging ? 'text-cyber-indigo' : 'text-slate-600'"
                fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round"
                  d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
              </svg>
            </div>

            <template v-if="!selectedFile">
              <p class="text-sm font-medium text-slate-400">
                {{ isDragging ? 'Release to upload evidence' : 'Drag & drop evidence file here' }}
              </p>
              <p class="mt-1 text-xs text-slate-700">or click to browse — screenshots, PDFs, exports accepted</p>
              <p class="mt-3 text-[10px] text-slate-800">Max size: 50 MB</p>
            </template>

            <!-- File selected preview -->
            <template v-else>
              <div class="flex items-center gap-3 rounded-lg border border-border bg-surface px-4 py-2.5">
                <div class="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg"
                  style="background: linear-gradient(135deg, rgba(99,102,241,0.2), rgba(139,92,246,0.2));">
                  <svg class="h-4 w-4 text-cyber-indigo" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
                    <path stroke-linecap="round" stroke-linejoin="round"
                      d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
                  </svg>
                </div>
                <div class="min-w-0 text-left">
                  <p class="truncate text-sm font-medium text-slate-200">{{ selectedFile.name }}</p>
                  <p class="text-xs text-slate-600">{{ formatBytes(selectedFile.size) }} · {{ selectedFile.type || 'Unknown type' }}</p>
                </div>
                <button @click.stop="clearFile"
                  class="ml-3 rounded p-0.5 text-slate-700 transition hover:text-slate-400">
                  <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              <p class="mt-2 text-xs text-slate-700">Click zone again to change file</p>
            </template>
          </div>
        </div>

        <!-- Secure button -->
        <button
          id="secure-btn"
          @click="secureEvidence"
          :disabled="!selectedFile || uploading"
          class="group relative flex w-full items-center justify-center gap-3 overflow-hidden rounded-xl py-4 text-sm font-semibold text-white transition-all duration-200 disabled:cursor-not-allowed disabled:opacity-40"
          style="background: linear-gradient(135deg, #6366F1 0%, #8B5CF6 60%, #A78BFA 100%);"
        >
          <span class="pointer-events-none absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/10 to-transparent transition-transform duration-700 group-hover:translate-x-full" />

          <template v-if="uploading">
            <svg class="h-4 w-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
              <path class="opacity-75" fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
            <span>Saving to MCP & Anchoring to Ledger…</span>
          </template>
          <template v-else>
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round"
                d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
            <span>Secure to MCP & Blockchain Ledger</span>
          </template>
        </button>

        <!-- API error -->
        <div v-if="apiError"
          class="animate-fade-in rounded-xl border border-cyber-red/30 bg-cyber-red/5 px-4 py-3">
          <p class="text-sm font-medium text-cyber-red">Anchoring Failed</p>
          <p class="mt-0.5 text-xs text-red-400/70">{{ apiError }}</p>
        </div>

      </div>

      <!-- ── RIGHT: Certificate panel ───────────────────────────────────── -->
      <div class="flex flex-col gap-5 animate-slide-up" style="animation-delay: 0.1s;">

        <!-- Empty state -->
        <div
          v-if="!certificate && !uploading"
          class="flex h-full min-h-96 flex-col items-center justify-center rounded-xl border border-dashed border-border text-center"
        >
          <div class="mb-3 flex h-14 w-14 items-center justify-center rounded-2xl"
            style="background: rgba(99,102,241,0.06); border: 1px solid rgba(99,102,241,0.15);">
            <svg class="h-7 w-7 text-slate-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.25">
              <path stroke-linecap="round" stroke-linejoin="round"
                d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
            </svg>
          </div>
          <p class="text-sm text-slate-600">Chain of Custody Certificate</p>
          <p class="mt-1 text-xs text-slate-700">will be generated here after anchoring</p>
        </div>

        <!-- Uploading skeleton -->
        <div v-if="uploading" class="animate-fade-in space-y-3">
          <div class="h-28 rounded-xl border border-border bg-surface shimmer-block" />
          <div class="h-16 rounded-xl border border-border bg-surface shimmer-block" />
          <div class="h-20 rounded-xl border border-border bg-surface shimmer-block" />
        </div>

        <!-- ── THE CERTIFICATE ─────────────────────────────────────────── -->
        <div
          v-if="certificate"
          id="evidence-certificate"
          ref="certificateRef"
          class="animate-slide-up overflow-hidden rounded-2xl border print-cert"
          style="border-color: rgba(99,102,241,0.4); box-shadow: 0 0 40px rgba(99,102,241,0.12), 0 4px 24px rgba(0,0,0,0.5);"
        >
          <!-- Certificate header gradient band -->
          <div class="relative px-6 py-5" style="background: linear-gradient(135deg, #312E81 0%, #1E1B4B 60%, #0B0F1A 100%);">
            <div class="flex items-start justify-between">
              <div>
                <div class="flex items-center gap-2.5">
                  <!-- Shield icon -->
                  <div class="flex h-8 w-8 items-center justify-center rounded-lg"
                    style="background: rgba(99,102,241,0.3); border: 1px solid rgba(99,102,241,0.5);">
                    <svg class="h-4 w-4 text-cyber-indigo" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                      <path stroke-linecap="round" stroke-linejoin="round"
                        d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
                    </svg>
                  </div>
                  <div>
                    <p class="text-xs font-bold uppercase tracking-widest text-indigo-300">CyberShield AI</p>
                    <p class="text-[10px] text-indigo-500">Evidence Management System</p>
                  </div>
                </div>
                <h2 class="mt-4 text-xl font-bold tracking-tight text-white">
                  Chain of Custody
                </h2>
                <p class="text-sm font-medium text-indigo-300">Digital Evidence Certificate</p>
              </div>

              <!-- Certificate number -->
              <div class="text-right">
                <p class="text-[10px] uppercase tracking-widest text-indigo-500">Certificate No.</p>
                <p class="font-mono text-sm font-semibold text-indigo-300">
                  CSA-{{ certNumber }}
                </p>
                <div class="mt-2 flex items-center justify-end gap-1.5">
                  <span class="h-1.5 w-1.5 rounded-full bg-cyber-emerald" />
                  <p class="text-[10px] text-emerald-400">Verified</p>
                </div>
              </div>
            </div>

            <!-- Decorative circles -->
            <div class="pointer-events-none absolute -right-8 -top-8 h-28 w-28 rounded-full opacity-10"
              style="background: radial-gradient(circle, #6366F1, transparent);" />
            <div class="pointer-events-none absolute -bottom-6 right-12 h-16 w-16 rounded-full opacity-10"
              style="background: radial-gradient(circle, #8B5CF6, transparent);" />
          </div>

          <!-- Certificate body -->
          <div class="bg-surface px-6 py-6">

            <!-- File info row -->
            <div class="mb-5 flex items-center gap-3 rounded-lg border border-border bg-navy-800 px-4 py-3">
              <svg class="h-5 w-5 flex-shrink-0 text-cyber-indigo" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round"
                  d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m2.25 0H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
              </svg>
              <div>
                <p class="text-xs text-slate-600">Anchored File</p>
                <p class="text-sm font-semibold text-slate-200">{{ selectedFile?.name ?? 'Evidence File' }}</p>
              </div>
              <div class="ml-auto text-right">
                <p class="text-xs text-slate-600">File Size</p>
                <p class="text-sm font-semibold text-slate-300">{{ selectedFile ? formatBytes(selectedFile.size) : '—' }}</p>
              </div>
            </div>

            <!-- Data grid -->
            <div class="grid grid-cols-1 gap-3">

              <!-- SHA-256 hash -->
              <div class="rounded-lg border bg-navy-800 px-4 py-3.5" style="border-color: rgba(99,102,241,0.25);">
                <div class="mb-2 flex items-center gap-2">
                  <svg class="h-3.5 w-3.5 text-cyber-indigo" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round"
                      d="M7 20l4-16m2 16l4-16M6 9h14M4 15h14" />
                  </svg>
                  <p class="text-[10px] font-semibold uppercase tracking-widest text-indigo-400">SHA-256 Cryptographic Hash</p>
                  <button @click="copyHash"
                    class="ml-auto rounded px-2 py-0.5 text-[10px] text-slate-600 transition hover:bg-surface-raised hover:text-slate-400">
                    {{ hashCopied ? '✓ Copied' : 'Copy' }}
                  </button>
                </div>
                <p class="break-all font-mono text-xs leading-relaxed text-cyber-indigo/80">
                  {{ certificate.fileHash }}
                </p>
              </div>

              <!-- Transaction ID -->
              <div class="rounded-lg border bg-navy-800 px-4 py-3.5" style="border-color: rgba(139,92,246,0.25);">
                <div class="mb-2 flex items-center gap-2">
                  <svg class="h-3.5 w-3.5 text-cyber-purple" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round"
                      d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                  </svg>
                  <p class="text-[10px] font-semibold uppercase tracking-widest text-purple-400">Blockchain Transaction ID</p>
                  <button @click="copyTxId"
                    class="ml-auto rounded px-2 py-0.5 text-[10px] text-slate-600 transition hover:bg-surface-raised hover:text-slate-400">
                    {{ txCopied ? '✓ Copied' : 'Copy' }}
                  </button>
                </div>
                <p class="break-all font-mono text-xs leading-relaxed text-cyber-purple/80">
                  {{ certificate.transactionId }}
                </p>
              </div>

              <!-- MCP File Reference -->
              <div v-if="certificate.mcpFileRef" class="rounded-lg border bg-navy-800 px-4 py-3.5" style="border-color: rgba(56,189,248,0.25);">
                <div class="mb-2 flex items-center gap-2">
                  <svg class="h-3.5 w-3.5 text-sky-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M20.25 14.15v4.25c0 1.094-.787 2.036-1.872 2.18-2.087.277-4.216.42-6.378.42s-4.291-.143-6.378-.42c-1.085-.144-1.872-1.086-1.872-2.18v-4.25m16.5 0a2.18 2.18 0 00.75-1.661V8.706c0-1.081-.768-2.015-1.837-2.175a48.114 48.114 0 00-3.413-.387m4.5 8.006c-.194.027-.392.05-.59.072m-.59-.072a52.14 52.14 0 00-3.265 0m0 0a1.5 1.5 0 01-1.08-.585l-1.08-1.584a1.5 1.5 0 00-1.08-.585h-6c-.44 0-.84.212-1.08.585l-1.08 1.584a1.5 1.5 0 01-1.08.585m3.265 0a52.14 52.14 0 00-3.265 0M16.5 14.15V8.662m0 0l-3.21-3.21m3.21 3.21L13.29 5.452M16.5 8.662V8.706" />
                  </svg>
                  <p class="text-[10px] font-semibold uppercase tracking-widest text-sky-400">MCP File Reference</p>
                </div>
                <p class="break-all font-mono text-xs leading-relaxed text-sky-400/80">
                  {{ certificate.mcpFileRef }}
                </p>
              </div>

              <!-- Timestamp + short hashes -->
              <div class="grid grid-cols-2 gap-3">
                <div class="rounded-lg border border-border bg-navy-800 px-4 py-3">
                  <p class="text-[10px] font-semibold uppercase tracking-widest text-slate-600">Anchored At</p>
                  <p class="mt-1 font-mono text-xs text-slate-300">{{ certificate.timestamp }}</p>
                </div>
                <div class="rounded-lg border border-border bg-navy-800 px-4 py-3">
                  <p class="text-[10px] font-semibold uppercase tracking-widest text-slate-600">Short Hash</p>
                  <p class="mt-1 font-mono text-xs text-slate-300">{{ certificate.fileHash.substring(0, 8) }}…</p>
                </div>
              </div>

            </div>

            <!-- Certification statement -->
            <div class="mt-5 rounded-lg border border-border bg-navy-900 px-4 py-3">
              <p class="font-mono text-[10px] leading-relaxed text-slate-600">
                This certificate attests that the above digital evidence file was securely stored off-chain
                in the MCP Filesystem, cryptographically hashed using the SHA-256 algorithm, and its fingerprint
                was anchored to the CyberShield AI secure ledger at the timestamp stated herein. Any subsequent
                modification to the file will produce a different hash, making tampering immediately detectable.
                Issued under the Bangladesh Cyber Security Act 2023, Section 24.
              </p>
            </div>

            <!-- Action bar -->
            <div class="mt-5 flex items-center gap-3 border-t border-border pt-5 no-print">
              <button @click="printCertificate"
                class="flex items-center gap-2 rounded-lg border border-border-bright px-4 py-2 text-xs font-semibold text-slate-300 transition hover:border-border-accent hover:text-slate-100">
                <svg class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round"
                    d="M6.72 13.829c-.24.03-.48.062-.72.096m.72-.096a42.415 42.415 0 0110.56 0m-10.56 0L6.34 18m10.94-4.171c.24.03.48.062.72.096m-.72-.096L17.66 18m0 0l.229 2.523a1.125 1.125 0 01-1.12 1.227H7.231c-.662 0-1.18-.568-1.12-1.227L6.34 18m11.318 0h1.091A2.25 2.25 0 0021 15.75V9.456c0-1.081-.768-2.015-1.837-2.175a48.055 48.055 0 00-1.913-.247M6.34 18H5.25A2.25 2.25 0 013 15.75V9.456c0-1.081.768-2.015 1.837-2.175a48.041 48.041 0 011.913-.247m10.5 0a48.536 48.536 0 00-10.5 0m10.5 0V3.375c0-.621-.504-1.125-1.125-1.125h-8.25c-.621 0-1.125.504-1.125 1.125v3.659M18 10.5h.008v.008H18V10.5zm-3 0h.008v.008H15V10.5z" />
                </svg>
                Print Certificate
              </button>
              <button @click="resetVault"
                class="flex items-center gap-2 rounded-lg px-4 py-2 text-xs font-semibold text-slate-600 transition hover:text-slate-400">
                <svg class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round"
                    d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0l3.181 3.183a8.25 8.25 0 0013.803-3.7M4.031 9.865a8.25 8.25 0 0113.803-4.4l3.181 3.182m0-4.991v4.99" />
                </svg>
                New Evidence
              </button>
              <div class="ml-auto flex items-center gap-1.5 text-[10px] text-cyber-emerald">
                <span class="h-1.5 w-1.5 rounded-full bg-cyber-emerald" />
                Cryptographically verified
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

useHead({ title: 'Evidence Vault' })

const config = useRuntimeConfig()

// ── State ──────────────────────────────────────────────────────────────────
const isDragging   = ref(false)
const selectedFile = ref<File | null>(null)
const uploading    = ref(false)
const apiError     = ref<string | null>(null)
const hashCopied   = ref(false)
const txCopied     = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const certificateRef = ref<HTMLElement | null>(null)

interface EvidenceCertificate {
  fileHash:      string
  transactionId: string
  timestamp:     string
  mcpFileRef?:   string
}

const certificate = ref<EvidenceCertificate | null>(null)

// ── How it works steps ─────────────────────────────────────────────────────
const HOW_IT_WORKS = [
  { n: '1', title: 'Upload File',   desc: 'Any evidence file' },
  { n: '2', title: 'MCP Storage',   desc: 'Secure off-chain storage' },
  { n: '3', title: 'SHA-256 Hash',  desc: 'Cryptographic fingerprint' },
  { n: '4', title: 'Anchor Ledger', desc: 'Blockchain receipt' },
]

// ── Certificate number (short hash of timestamp) ───────────────────────────
const certNumber = computed(() => {
  if (!certificate.value) return ''
  return certificate.value.fileHash.substring(0, 6).toUpperCase()
})

// ── File handling ──────────────────────────────────────────────────────────
function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement
  if (input.files?.[0]) {
    selectedFile.value = input.files[0]
    certificate.value  = null
    apiError.value     = null
  }
}

function onDrop(event: DragEvent) {
  isDragging.value = false
  const file = event.dataTransfer?.files?.[0]
  if (file) {
    selectedFile.value = file
    certificate.value  = null
    apiError.value     = null
  }
}

function clearFile() {
  selectedFile.value = null
  certificate.value  = null
  apiError.value     = null
  if (fileInputRef.value) fileInputRef.value.value = ''
}

function resetVault() {
  clearFile()
}

// ── Upload & secure ────────────────────────────────────────────────────────
async function secureEvidence() {
  if (!selectedFile.value) return

  uploading.value = true
  apiError.value  = null
  certificate.value = null

  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)

    const result = await $fetch<EvidenceCertificate>('/api/evidence/secure', {
      baseURL: config.public.apiBase,
      method:  'POST',
      body:    formData,
    })

    certificate.value = result
    if (result && result.fileHash) {
      localStorage.setItem('cybershield_last_evidence_hash', result.fileHash)
    }
  } catch (err: unknown) {
    const e = err as { data?: { message?: string }; message?: string }
    apiError.value =
      e?.data?.message ??
      e?.message ??
      'Unable to reach the CyberShield API. Ensure Spring Boot is running on :8080.'
  } finally {
    uploading.value = false
  }
}

// ── Copy helpers ───────────────────────────────────────────────────────────
async function copyHash() {
  if (!certificate.value) return
  await navigator.clipboard.writeText(certificate.value.fileHash)
  hashCopied.value = true
  setTimeout(() => { hashCopied.value = false }, 2000)
}

async function copyTxId() {
  if (!certificate.value) return
  await navigator.clipboard.writeText(certificate.value.transactionId)
  txCopied.value = true
  setTimeout(() => { txCopied.value = false }, 2000)
}

// ── Print certificate ──────────────────────────────────────────────────────
function printCertificate() {
  window.print()
}

// ── Utilities ──────────────────────────────────────────────────────────────
function formatBytes(bytes: number): string {
  if (bytes < 1024)      return `${bytes} B`
  if (bytes < 1048576)   return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1048576).toFixed(2)} MB`
}
</script>

<style scoped>
.shimmer-block {
  background: linear-gradient(90deg, #141D2E 25%, #1A2436 50%, #141D2E 75%);
  background-size: 200% 100%;
  animation: shimmer 1.8s linear infinite;
}
@keyframes shimmer {
  0%   { background-position: -200% center; }
  100% { background-position:  200% center; }
}
</style>

<style>
/* Print styles — only the certificate renders, everything else hidden */
@media print {
  body                   { background: white !important; color: black !important; }
  aside, header, .no-print { display: none !important; }
  .print-cert            { border: 2px solid #6366F1 !important; box-shadow: none !important; }
  .print-cert *          { print-color-adjust: exact; -webkit-print-color-adjust: exact; }
}
</style>

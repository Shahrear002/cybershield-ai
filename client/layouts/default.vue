<template>
  <div class="flex h-screen overflow-hidden bg-navy-900">

    <!-- ══════════════════════════════════════════════════════════════════════
         STICKY LEFT SIDEBAR
         ══════════════════════════════════════════════════════════════════════ -->
    <aside
      class="relative flex w-64 flex-shrink-0 flex-col border-r border-border"
      style="background: linear-gradient(180deg, #080D16 0%, #0B0F1A 100%);"
    >
      <!-- Subtle left accent glow -->
      <div
        class="pointer-events-none absolute inset-y-0 left-0 w-px"
        style="background: linear-gradient(180deg, transparent 0%, #06B6D4 40%, #6366F1 70%, transparent 100%); opacity: 0.5;"
      />

      <!-- ── Logo / Brand ──────────────────────────────────────────────── -->
      <div class="flex items-center gap-3 border-b border-border px-5 py-5">
        <!-- Shield icon -->
        <div
          class="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg"
          style="background: linear-gradient(135deg, #0EA5E9 0%, #6366F1 100%);"
        >
          <svg class="h-5 w-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round"
              d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
          </svg>
        </div>
        <div>
          <p class="text-sm font-bold tracking-wide text-slate-100">CyberShield</p>
          <p class="text-[10px] font-medium uppercase tracking-widest text-slate-500">AI Platform</p>
        </div>
      </div>

      <!-- ── Navigation ────────────────────────────────────────────────── -->
      <nav class="flex flex-1 flex-col gap-1 overflow-y-auto px-3 py-4">

        <p class="mb-2 px-3 text-[10px] font-semibold uppercase tracking-widest text-slate-600">
          Workspace
        </p>

        <NavItem to="/" icon="dashboard" label="Dashboard" />
        <NavItem to="/advocate" icon="triage" label="AI Triage" badge="Live" />
        <NavItem to="/vault" icon="vault" label="Evidence Vault" />
        <NavItem to="/fir-compiler" icon="fir" label="FIR Compiler" badge="New" />

        <div class="my-3 border-t border-border" />

        <p class="mb-2 px-3 text-[10px] font-semibold uppercase tracking-widest text-slate-600">
          Case Management
        </p>

        <NavItem to="/cases" icon="cases" label="Case Files" comingSoon />
        <NavItem to="/analytics" icon="analytics" label="Analytics" comingSoon />
        <NavItem to="/reports" icon="reports" label="Reports" comingSoon />

      </nav>

      <!-- ── System Status Footer ──────────────────────────────────────── -->
      <div class="border-t border-border px-4 py-4">
        <div class="flex items-center justify-between rounded-lg bg-surface px-3 py-2.5">
          <div class="flex items-center gap-2">
            <span class="relative flex h-2 w-2">
              <span class="absolute inline-flex h-full w-full animate-ping rounded-full bg-cyber-emerald opacity-60" />
              <span class="relative inline-flex h-2 w-2 rounded-full bg-cyber-emerald" />
            </span>
            <span class="text-xs font-medium text-slate-400">AI Engine Online</span>
          </div>
          <span class="rounded px-1.5 py-0.5 text-[9px] font-semibold uppercase tracking-wide text-cyber-cyan"
            style="background: rgba(6,182,212,0.1); border: 1px solid rgba(6,182,212,0.2);">
            v0.1
          </span>
        </div>

        <div class="mt-3 flex items-center gap-2.5 px-1">
          <div class="flex h-7 w-7 flex-shrink-0 items-center justify-center rounded-full bg-surface-raised ring-1 ring-border-bright">
            <svg class="h-3.5 w-3.5 text-slate-400" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
            </svg>
          </div>
          <div class="min-w-0">
            <p class="truncate text-xs font-medium text-slate-300">Advocate</p>
            <p class="truncate text-[10px] text-slate-600">admin@cybershield.ai</p>
          </div>
        </div>
      </div>
    </aside>

    <!-- ══════════════════════════════════════════════════════════════════════
         MAIN CONTENT AREA
         ══════════════════════════════════════════════════════════════════════ -->
    <main class="flex flex-1 flex-col overflow-hidden">

      <!-- Top bar -->
      <header class="flex h-14 flex-shrink-0 items-center justify-between border-b border-border bg-navy-900 px-6">
        <div class="flex items-center gap-2 text-sm text-slate-500">
          <span>CyberShield AI</span>
          <span class="text-slate-700">/</span>
          <span class="text-slate-300">{{ pageTitle }}</span>
        </div>
        <div class="flex items-center gap-3">
          <!-- Connection indicator -->
          <div class="flex items-center gap-1.5 rounded-md border border-border px-2.5 py-1 text-xs text-slate-500">
            <span class="h-1.5 w-1.5 rounded-full bg-cyber-emerald" />
            API :8080
          </div>
          <div class="h-7 w-px bg-border" />
          <!-- Clock -->
          <span class="font-mono text-xs text-slate-600">{{ time }}</span>
        </div>
      </header>

      <!-- Scrollable page slot -->
      <div class="flex-1 overflow-y-auto">
        <slot />
      </div>

    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// ── Current time (updated every second) ────────────────────────────────────
const time = ref('')
let timer: ReturnType<typeof setInterval>

function updateTime() {
  time.value = new Date().toLocaleTimeString('en-US', {
    hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false,
  })
}

onMounted(() => { updateTime(); timer = setInterval(updateTime, 1000) })
onUnmounted(() => clearInterval(timer))

// ── Page title from route ───────────────────────────────────────────────────
const pageTitle = computed(() => {
  const map: Record<string, string> = {
    '/':              'Dashboard',
    '/advocate':      'AI Triage Engine',
    '/vault':         'Evidence Vault',
    '/fir-compiler':  'FIR Compiler',
    '/cases':         'Case Files',
    '/analytics':     'Analytics',
    '/reports':       'Reports',
  }
  return map[route.path] ?? 'Overview'
})
</script>

<!-- NavItem is auto-imported from components/NavItem.vue by Nuxt -->

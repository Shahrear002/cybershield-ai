<template>
  <div class="flex min-h-full flex-col items-center justify-center px-8 py-20">
    <div class="text-center animate-fade-in">

      <!-- Hero icon -->
      <div class="mx-auto mb-6 flex h-20 w-20 items-center justify-center rounded-2xl shadow-glow"
        style="background: linear-gradient(135deg, #0EA5E9 0%, #6366F1 100%);">
        <svg class="h-10 w-10 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
          <path stroke-linecap="round" stroke-linejoin="round"
            d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
        </svg>
      </div>

      <h1 class="mb-2 text-4xl font-bold tracking-tight text-slate-100">CyberShield AI</h1>
      <p class="mb-2 text-lg font-medium text-slate-400">Digital Rights &amp; Justice Platform</p>
      <p class="mb-10 text-sm text-slate-600">Hackathon Prototype · Bangladesh Cyber Security Act 2023</p>

      <!-- Stat strip -->
      <div class="mb-10 flex items-center justify-center gap-8">
        <div v-for="stat in STATS" :key="stat.label" class="text-center">
          <p class="text-2xl font-bold" :style="{ color: stat.color }">{{ stat.value }}</p>
          <p class="text-xs text-slate-600">{{ stat.label }}</p>
        </div>
      </div>

      <!-- CTA buttons -->
      <div class="flex flex-col items-center gap-3 sm:flex-row sm:justify-center">
        <NuxtLink to="/advocate"
          class="group relative flex items-center gap-2.5 overflow-hidden rounded-xl px-7 py-3.5 text-sm font-semibold text-white shadow-glow transition-all hover:opacity-90"
          style="background: linear-gradient(135deg, #0EA5E9, #6366F1);">
          <span class="pointer-events-none absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/10 to-transparent transition-transform duration-700 group-hover:translate-x-full" />
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round"
              d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
          </svg>
          Open AI Triage
        </NuxtLink>

        <NuxtLink to="/vault"
          class="flex items-center gap-2.5 rounded-xl border border-border-bright px-7 py-3.5 text-sm font-semibold text-slate-300 transition-all hover:border-cyber-indigo/50 hover:bg-surface hover:text-slate-100">
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round"
              d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
          </svg>
          Evidence Vault
        </NuxtLink>
      </div>

      <!-- Feature pills -->
      <div class="mt-10 flex flex-wrap items-center justify-center gap-2">
        <span v-for="feat in FEATURES" :key="feat"
          class="rounded-full border border-border px-3 py-1 text-xs text-slate-600">
          {{ feat }}
        </span>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
useHead({ title: 'Dashboard' })

const STATS = [
  { value: '4',    label: 'Threat Categories', color: '#06B6D4' },
  { value: 'AI',   label: 'Powered Analysis',  color: '#6366F1' },
  { value: '100%', label: 'SHA-256 Verified',  color: '#10B981' },
]

const FEATURES = [
  'Spring AI · OpenAI',
  'SHA-256 Evidence Hashing',
  'Bangladesh CSA 2023',
  'Chain of Custody',
  'Spring Boot 3.x',
  'Nuxt 3 · Tailwind',
]
</script>

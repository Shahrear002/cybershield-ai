<template>
  <!-- Disabled/coming-soon items render as non-links -->
  <div
    v-if="comingSoon"
    class="group flex cursor-not-allowed items-center gap-3 rounded-lg px-3 py-2.5 text-sm text-slate-600 opacity-50"
  >
    <svg class="h-4 w-4 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
      <path stroke-linecap="round" stroke-linejoin="round" :d="iconPath" />
    </svg>
    <span>{{ label }}</span>
    <span class="ml-auto rounded px-1 py-0.5 text-[9px] font-semibold uppercase tracking-wide text-slate-700"
      style="background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.06);">
      soon
    </span>
  </div>

  <!-- Active / normal nav link -->
  <NuxtLink
    v-else
    :to="to"
    class="group relative flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm transition-all duration-150"
    :class="isActive
      ? 'bg-surface-raised text-slate-100 shadow-card'
      : 'text-slate-500 hover:bg-surface hover:text-slate-300'"
  >
    <!-- Active left accent bar -->
    <span
      v-if="isActive"
      class="absolute left-0 top-1/2 h-5 w-0.5 -translate-y-1/2 rounded-r"
      style="background: linear-gradient(180deg, #06B6D4, #6366F1);"
    />

    <!-- Icon -->
    <svg
      class="h-4 w-4 flex-shrink-0 transition-colors duration-150"
      :class="isActive ? 'text-cyber-cyan' : 'text-slate-600 group-hover:text-slate-400'"
      fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75"
    >
      <path stroke-linecap="round" stroke-linejoin="round" :d="iconPath" />
    </svg>

    <!-- Label -->
    <span class="flex-1">{{ label }}</span>

    <!-- Optional badge (e.g. "Live") -->
    <span
      v-if="badge"
      class="rounded px-1.5 py-0.5 text-[9px] font-semibold uppercase tracking-wide text-cyber-cyan"
      style="background: rgba(6,182,212,0.12); border: 1px solid rgba(6,182,212,0.25);"
    >
      {{ badge }}
    </span>
  </NuxtLink>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const props = defineProps<{
  to:         string
  icon:       string
  label:      string
  badge?:     string
  comingSoon?: boolean
}>()

const route = useRoute()
const isActive = computed(() => route.path === props.to)

/** SVG path data keyed by icon name */
const PATHS: Record<string, string> = {
  dashboard: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6',
  triage:    'M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z',
  vault:     'M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z',
  fir:       'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z',
  cases:     'M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10',
  analytics: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z',
  reports:   'M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z',
}

const iconPath = computed(() => PATHS[props.icon] ?? PATHS.dashboard)
</script>

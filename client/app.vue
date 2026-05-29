<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
</template>

<style>
/* ── Global resets & base ────────────────────────────────────────────────── */
*, *::before, *::after { box-sizing: border-box; }

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  background-color: #0B0F1A;
  color: #F1F5F9;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* ── Custom scrollbar ────────────────────────────────────────────────────── */
::-webkit-scrollbar              { width: 6px; height: 6px; }
::-webkit-scrollbar-track        { background: #0B0F1A; }
::-webkit-scrollbar-thumb        { background: #1E293B; border-radius: 3px; }
::-webkit-scrollbar-thumb:hover  { background: #334155; }

/* ── Page transitions ─────────────────────────────────────────────────────── */
.page-enter-active,
.page-leave-active { transition: all 0.25s ease; }
.page-enter-from   { opacity: 0; transform: translateY(10px); }
.page-leave-to     { opacity: 0; transform: translateY(-6px); }

/* ── Focus ring ──────────────────────────────────────────────────────────── */
:focus-visible { outline: 2px solid #06B6D4; outline-offset: 2px; }

/* ── Selection highlight ─────────────────────────────────────────────────── */
::selection { background: rgba(6,182,212,0.25); color: #F1F5F9; }
</style>

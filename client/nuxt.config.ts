// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',

  // ── Modules ──────────────────────────────────────────────────────────────
  modules: ['@nuxtjs/tailwindcss'],

  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080',
    },
  },

  // ── App-level head and page transitions ──────────────────────────────────
  app: {
    pageTransition: { name: 'page', mode: 'out-in' },
    layoutTransition: { name: 'layout', mode: 'out-in' },
    head: {
      title: 'CyberShield AI',
      titleTemplate: '%s — Digital Rights & Justice Platform',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        {
          name: 'description',
          content:
            'AI-powered cybercrime triage, digital evidence management, and legal case structuring platform.',
        },
        { name: 'theme-color', content: '#0B0F1A' },
      ],
      link: [
        { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
        {
          rel: 'stylesheet',
          href: 'https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@400;500;600&display=swap',
        },
      ],
    },
  },

  // ── Tailwind CSS ──────────────────────────────────────────────────────────
  tailwindcss: {
    configPath: '~/tailwind.config.ts',
    exposeConfig: false,
  },

  devtools: { enabled: false },
})

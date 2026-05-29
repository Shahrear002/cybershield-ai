import type { Config } from 'tailwindcss'

export default {
  content: [
    './components/**/*.{vue,js,ts}',
    './layouts/**/*.vue',
    './pages/**/*.vue',
    './plugins/**/*.{js,ts}',
    './app.vue',
    './error.vue',
  ],

  theme: {
    extend: {
      // ── Enterprise color palette ─────────────────────────────────────────
      colors: {
        navy: {
          950: '#03060F',
          900: '#0B0F1A',
          800: '#0F1626',
          700: '#141D2E',
          600: '#1A2436',
        },
        surface: {
          DEFAULT: '#111827',
          raised: '#141F30',
          high:   '#1A2640',
        },
        border: {
          DEFAULT: '#1E293B',
          bright:  '#334155',
          accent:  '#0EA5E9',
        },
        cyber: {
          cyan:    '#06B6D4',
          blue:    '#3B82F6',
          indigo:  '#6366F1',
          purple:  '#8B5CF6',
          emerald: '#10B981',
          amber:   '#F59E0B',
          orange:  '#F97316',
          red:     '#EF4444',
          rose:    '#F43F5E',
        },
      },

      // ── Typography ───────────────────────────────────────────────────────
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'Consolas', '"Courier New"', 'monospace'],
      },

      // ── Custom animations ────────────────────────────────────────────────
      animation: {
        'fade-in':   'fadeIn 0.35s ease-out both',
        'slide-up':  'slideUp 0.4s cubic-bezier(0.16,1,0.3,1) both',
        'slide-in':  'slideIn 0.4s cubic-bezier(0.16,1,0.3,1) both',
        'pulse-glow':'pulseGlow 2.5s ease-in-out infinite',
        'shimmer':   'shimmer 1.8s linear infinite',
        'bar-grow':  'barGrow 0.8s cubic-bezier(0.16,1,0.3,1) both',
        'spin-slow': 'spin 2s linear infinite',
      },

      keyframes: {
        fadeIn: {
          from: { opacity: '0' },
          to:   { opacity: '1' },
        },
        slideUp: {
          from: { transform: 'translateY(18px)', opacity: '0' },
          to:   { transform: 'translateY(0)',    opacity: '1' },
        },
        slideIn: {
          from: { transform: 'translateX(-12px)', opacity: '0' },
          to:   { transform: 'translateX(0)',      opacity: '1' },
        },
        pulseGlow: {
          '0%,100%': { boxShadow: '0 0 0 0 rgba(6,182,212,0)' },
          '50%':     { boxShadow: '0 0 22px 4px rgba(6,182,212,0.22)' },
        },
        shimmer: {
          '0%':   { backgroundPosition: '-200% center' },
          '100%': { backgroundPosition:  '200% center' },
        },
        barGrow: {
          from: { width: '0%' },
          to:   { width: 'var(--bar-width, 0%)' },
        },
      },

      // ── Backdrop blur for glassmorphism panels ───────────────────────────
      backdropBlur: { xs: '2px' },

      // ── Box shadows — deep navy context ─────────────────────────────────
      boxShadow: {
        card:   '0 1px 3px rgba(0,0,0,0.5), 0 1px 2px rgba(0,0,0,0.4)',
        panel:  '0 4px 24px rgba(0,0,0,0.6)',
        glow:   '0 0 20px rgba(6,182,212,0.25)',
        'glow-purple': '0 0 20px rgba(139,92,246,0.25)',
        'glow-red':    '0 0 20px rgba(239,68,68,0.25)',
        'glow-amber':  '0 0 20px rgba(245,158,11,0.25)',
        inset:  'inset 0 1px 0 rgba(255,255,255,0.05)',
      },
    },
  },

  plugins: [],
} satisfies Config

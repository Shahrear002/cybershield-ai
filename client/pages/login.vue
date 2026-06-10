<template>
  <div class="min-h-screen flex items-center justify-center bg-slate-950 px-4 sm:px-6 lg:px-8 font-sans">
    <div class="max-w-md w-full">
      
      <!-- Secure Portal Header -->
      <div class="text-center mb-10 animate-fade-in">
        <div class="mx-auto h-16 w-16 mb-4 flex items-center justify-center rounded-xl bg-slate-900 border border-slate-800 shadow-[0_0_15px_rgba(16,185,129,0.1)]">
          <svg class="h-8 w-8 text-emerald-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
          </svg>
        </div>
        <h2 class="text-3xl font-bold tracking-tight text-slate-100">CyberShield AI</h2>
        <p class="mt-2 text-sm text-slate-500 uppercase tracking-widest font-semibold">Secure Authorization Portal</p>
      </div>

      <!-- Login Card -->
      <div class="bg-slate-900 border border-slate-800 rounded-xl shadow-2xl p-8 animate-slide-up">
        <form class="space-y-6" @submit.prevent="handleSubmit">
          
          <!-- Username Field -->
          <div>
            <label for="username" class="block text-sm font-medium text-slate-400 mb-2">Authorized Email</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-slate-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <input id="username" v-model="username" name="username" type="email" required 
                class="block w-full pl-10 pr-3 py-3 border border-slate-700 rounded-lg leading-5 bg-slate-950 text-slate-200 placeholder-slate-600 focus:outline-none focus:ring-1 focus:ring-emerald-500 focus:border-emerald-500 transition-colors sm:text-sm" 
                placeholder="admin@cybershield.gov.bd" />
            </div>
          </div>

          <!-- Password Field -->
          <div>
            <label for="password" class="block text-sm font-medium text-slate-400 mb-2">Security Credential</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-slate-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </div>
              <input id="password" v-model="password" name="password" type="password" required 
                class="block w-full pl-10 pr-3 py-3 border border-slate-700 rounded-lg leading-5 bg-slate-950 text-slate-200 placeholder-slate-600 focus:outline-none focus:ring-1 focus:ring-emerald-500 focus:border-emerald-500 transition-colors sm:text-sm" 
                placeholder="••••••••" />
            </div>
          </div>

          <!-- Actions -->
          <div class="pt-2">
            <button type="submit" :disabled="loading" 
              class="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-bold text-slate-950 bg-emerald-500 hover:bg-emerald-400 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-slate-900 focus:ring-emerald-500 transition-all disabled:opacity-50 disabled:cursor-not-allowed">
              <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-5 w-5 text-slate-900" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              {{ loading ? 'Authenticating...' : 'Authenticate' }}
            </button>
          </div>
          
          <!-- Feedback Messages -->
          <div v-if="errorMsg" class="mt-4 p-3 rounded-lg bg-red-950/50 border border-red-900 flex items-center gap-2">
            <svg class="h-5 w-5 text-red-500 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p class="text-sm text-red-400 font-medium">{{ errorMsg }}</p>
          </div>
        </form>
      </div>

      <!-- Footer Info -->
      <p class="mt-8 text-center text-xs text-slate-600">
        Access is strictly restricted to authorized personnel.<br>All activities are logged and monitored.
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

useHead({ title: 'Authentication Portal — CyberShield AI' })

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMsg = ref('')

const { login } = useAuth()
const router = useRouter()
const route = useRoute()

const handleSubmit = async () => {
  loading.value = true
  errorMsg.value = ''

  try {
    const response: any = await $fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      body: {
        username: username.value,
        password: password.value
      }
    })
    
    login(response.token, response.role, response.username)
    
    const redirectPath = route.query.redirect as string
    if (redirectPath) {
      router.push(redirectPath)
    } else if (response.role === 'ROLE_ADMIN') {
      router.push('/admin')
    } else {
      router.push('/dashboard')
    }
  } catch (err: any) {
    errorMsg.value = err.data?.message || 'Authentication failed. Invalid credentials.'
  } finally {
    loading.value = false
  }
}
</script>

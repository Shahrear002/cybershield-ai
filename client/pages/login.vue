<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900 dark:text-white">
          CyberShield AI Login
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600 dark:text-gray-400">
          Or <a @click.prevent="isRegistering = !isRegistering" href="#" class="font-medium text-indigo-600 hover:text-indigo-500">
            {{ isRegistering ? 'Sign in to your account' : 'Register a new account' }}
          </a>
        </p>
      </div>
      <form class="mt-8 space-y-6" @submit.prevent="handleSubmit">
        <input type="hidden" name="remember" value="true" />
        <div class="rounded-md shadow-sm -space-y-px">
          <div>
            <label for="username" class="sr-only">Username</label>
            <input id="username" v-model="username" name="username" type="text" required class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm" placeholder="Username" />
          </div>
          <div>
            <label for="password" class="sr-only">Password</label>
            <input id="password" v-model="password" name="password" type="password" required class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm" :class="{ 'rounded-b-md': !isRegistering }" placeholder="Password" />
          </div>
          <div v-if="isRegistering">
            <label for="role" class="sr-only">Role</label>
            <select id="role" v-model="role" name="role" class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm">
              <option value="VICTIM">Victim</option>
              <option value="ADMIN">Admin (Law Enforcement)</option>
            </select>
          </div>
        </div>

        <div>
          <button type="submit" :disabled="loading" class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50">
            {{ loading ? 'Processing...' : (isRegistering ? 'Register' : 'Sign in') }}
          </button>
        </div>
        <div v-if="errorMsg" class="text-red-500 text-sm text-center mt-2">
          {{ errorMsg }}
        </div>
        <div v-if="successMsg" class="text-green-500 text-sm text-center mt-2">
          {{ successMsg }}
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const username = ref('')
const password = ref('')
const role = ref('VICTIM')
const isRegistering = ref(false)
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

const { login } = useAuth()
const router = useRouter()

const handleSubmit = async () => {
  loading.value = true
  errorMsg.value = ''
  successMsg.value = ''

  try {
    if (isRegistering.value) {
      await $fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        body: {
          username: username.value,
          password: password.value,
          role: role.value
        }
      })
      successMsg.value = 'Registration successful! Please sign in.'
      isRegistering.value = false
    } else {
      const response: any = await $fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        body: {
          username: username.value,
          password: password.value
        }
      })
      login(response.token, response.role, response.username)
      if (response.role === 'ROLE_ADMIN') {
        router.push('/admin')
      } else {
        router.push('/dashboard')
      }
    }
  } catch (err: any) {
    errorMsg.value = err.data?.message || 'Authentication failed'
  } finally {
    loading.value = false
  }
}
</script>

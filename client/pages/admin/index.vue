<template>
  <div class="min-h-screen bg-gray-50 dark:bg-navy-950 p-6">
    <div class="max-w-7xl mx-auto">
      <div class="flex justify-between items-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">Admin Dashboard - CyberShield</h1>
        <button @click="logout" class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-lg transition">Logout</button>
      </div>

      <div v-if="loading" class="text-center py-12">
        <p class="text-gray-500 dark:text-gray-400">Loading data...</p>
      </div>
      <!-- User Provisioning Section -->
      <div class="bg-white dark:bg-navy-900 shadow rounded-lg p-6 mb-8 border border-gray-200 dark:border-navy-800">
        <h2 class="text-lg font-bold text-gray-900 dark:text-white mb-4">Provision New User</h2>
        <form @submit.prevent="provisionUser" class="flex flex-col md:flex-row gap-4 items-start md:items-end">
          <div class="flex-1 w-full">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Email / Username</label>
            <input v-model="newUserEmail" type="email" required class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-emerald-500 focus:border-emerald-500 block w-full p-2.5 dark:bg-gray-800 dark:border-gray-700 dark:placeholder-gray-400 dark:text-white" placeholder="user@example.com">
          </div>
          <div class="flex-1 w-full">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Temporary Password</label>
            <input v-model="newUserPassword" type="text" required class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-emerald-500 focus:border-emerald-500 block w-full p-2.5 dark:bg-gray-800 dark:border-gray-700 dark:placeholder-gray-400 dark:text-white" placeholder="Secret123!">
          </div>
          <div class="w-full md:w-48">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Role</label>
            <select v-model="newUserRole" class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-emerald-500 focus:border-emerald-500 block w-full p-2.5 dark:bg-gray-800 dark:border-gray-700 dark:placeholder-gray-400 dark:text-white">
              <option value="ROLE_VICTIM">Victim</option>
              <option value="ROLE_ADMIN">Admin</option>
            </select>
          </div>
          <button type="submit" :disabled="isProvisioning" class="w-full md:w-auto bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 text-white font-medium rounded-lg text-sm px-5 py-2.5 text-center transition">
            {{ isProvisioning ? 'Creating...' : 'Create User' }}
          </button>
        </form>
        <div v-if="provisionError" class="mt-3 text-sm text-red-500">{{ provisionError }}</div>
        <div v-if="provisionSuccess" class="mt-3 text-sm text-emerald-500">{{ provisionSuccess }}</div>
      </div>

      <!-- Analytics Section -->
      <div v-if="!loading && analytics" class="mb-8 grid grid-cols-1 md:grid-cols-3 gap-6 animate-fade-in">
        <!-- Total Cases -->
        <div class="bg-white dark:bg-navy-900 shadow rounded-xl p-6 border border-gray-200 dark:border-navy-800">
          <h3 class="text-sm font-medium text-gray-500 dark:text-gray-400">Cases Analyzed</h3>
          <p class="mt-2 text-4xl font-bold text-gray-900 dark:text-white">{{ analytics.totalCases }}</p>
        </div>

        <!-- Serial Threat Actors -->
        <div class="bg-white dark:bg-navy-900 shadow rounded-xl p-6 border border-gray-200 dark:border-navy-800 col-span-1 md:col-span-2">
          <h3 class="text-sm font-medium text-gray-500 dark:text-gray-400 mb-4">Serial Threat Actors (LLM Deduplication)</h3>
          <div class="space-y-3">
            <div v-for="actor in analytics.serialThreatActors" :key="actor.inferredHandle" class="flex items-center justify-between p-3 bg-red-50 dark:bg-red-900/20 rounded-lg border border-red-100 dark:border-red-900/50">
              <div>
                <p class="font-bold text-red-700 dark:text-red-400">{{ actor.inferredHandle }}</p>
                <p class="text-xs text-red-600 dark:text-red-300 mt-1">{{ actor.semanticJustification }}</p>
              </div>
              <div class="text-center bg-white dark:bg-navy-800 rounded-lg px-3 py-1 shadow-sm border border-gray-100 dark:border-navy-700">
                <span class="text-[10px] text-gray-500 dark:text-gray-400 block uppercase tracking-wider">Matches</span>
                <span class="font-bold text-gray-900 dark:text-white">{{ actor.caseCount }}</span>
              </div>
            </div>
            <div v-if="!analytics.serialThreatActors?.length" class="text-sm text-gray-500">No serial offenders detected.</div>
          </div>
        </div>

        <!-- Emerging Trends -->
        <div class="bg-white dark:bg-navy-900 shadow rounded-xl p-6 border border-gray-200 dark:border-navy-800 col-span-1 md:col-span-3">
          <h3 class="text-sm font-medium text-gray-500 dark:text-gray-400 mb-4">Emerging Localized Trends</h3>
          <div class="flex flex-wrap gap-2">
            <span v-for="trend in analytics.emergingTrends" :key="trend" class="px-3 py-1.5 bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-cyber-cyan text-sm font-medium rounded-full border border-blue-200 dark:border-blue-900/50">
              {{ trend }}
            </span>
            <div v-if="!analytics.emergingTrends?.length" class="text-sm text-gray-500">No emerging trends detected.</div>
          </div>
        </div>
      </div>
      
      <div v-if="!loading" class="bg-white dark:bg-navy-900 shadow rounded-lg overflow-x-auto border border-gray-200 dark:border-navy-800">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-navy-700">
          <thead class="bg-gray-50 dark:bg-navy-800">
            <tr>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Reference No.</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Victim / User</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Subject</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Date</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Status</th>
              <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-navy-900 divide-y divide-gray-200 dark:divide-navy-700">
            <tr v-for="fir in firs" :key="fir.id" class="hover:bg-gray-50 dark:hover:bg-navy-800/50 transition">
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-indigo-600 dark:text-cyber-cyan">{{ fir.firReferenceNumber }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ fir.user?.username || 'Unknown' }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-slate-300 truncate max-w-xs">{{ fir.subject }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-slate-400">{{ new Date(fir.createdAt).toLocaleDateString() }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <span :class="getStatusClass(fir.status)" class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full border">
                  {{ fir.status }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <select 
                  v-model="fir.status" 
                  @change="updateStatus(fir.id, fir.status)"
                  class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5 dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white dark:focus:ring-blue-500 dark:focus:border-blue-500"
                >
                  <option value="PENDING_SUBMISSION">Pending Submission</option>
                  <option value="SUBMITTED">Submitted</option>
                  <option value="ACKNOWLEDGED">Acknowledged</option>
                  <option value="CLOSED">Closed</option>
                </select>
              </td>
            </tr>
            <tr v-if="firs.length === 0">
              <td colspan="6" class="px-6 py-8 text-center text-gray-500 dark:text-gray-400">No FIRs found.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

definePageMeta({
  middleware: 'auth'
})

const { token, logout } = useAuth()
const config = useRuntimeConfig()
const firs = ref<any[]>([])
const analytics = ref<any>(null)
const loading = ref(true)

// User Provisioning State
const newUserEmail = ref('')
const newUserPassword = ref('')
const newUserRole = ref('ROLE_VICTIM')
const isProvisioning = ref(false)
const provisionError = ref('')
const provisionSuccess = ref('')

const fetchFIRs = async () => {
  try {
    firs.value = await $fetch('/api/fir', {
      baseURL: config.public.apiBase || 'http://localhost:8080',
      headers: {
        Authorization: `Bearer ${token.value}`
      }
    })
  } catch (err) {
    console.error("Failed to fetch FIRs:", err)
    alert("Failed to load data. Ensure backend is running.")
  } finally {
    loading.value = false
  }
}

const fetchAnalytics = async () => {
  try {
    analytics.value = await $fetch('/api/fir/analytics', {
      baseURL: config.public.apiBase || 'http://localhost:8080',
      headers: {
        Authorization: `Bearer ${token.value}`
      }
    })
  } catch (err) {
    console.error("Failed to fetch analytics:", err)
  }
}

const provisionUser = async () => {
  isProvisioning.value = true
  provisionError.value = ''
  provisionSuccess.value = ''

  try {
    await $fetch('/api/admin/users', {
      baseURL: config.public.apiBase || 'http://localhost:8080',
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      },
      body: {
        username: newUserEmail.value,
        password: newUserPassword.value,
        role: newUserRole.value
      }
    })
    provisionSuccess.value = `User ${newUserEmail.value} successfully provisioned!`
    newUserEmail.value = ''
    newUserPassword.value = ''
    setTimeout(() => { provisionSuccess.value = '' }, 5000)
  } catch (err: any) {
    console.error("Failed to provision user:", err)
    provisionError.value = err.data?.error || 'Failed to create user. Ensure email is unique.'
  } finally {
    isProvisioning.value = false
  }
}

const updateStatus = async (id: number, newStatus: string) => {
  try {
    await $fetch(`/api/fir/${id}/status`, {
      baseURL: config.public.apiBase || 'http://localhost:8080',
      method: 'PATCH',
      headers: {
        Authorization: `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      },
      body: { status: newStatus }
    })
    alert('Status updated successfully!')
  } catch (err) {
    console.error("Failed to update status:", err)
    alert("Failed to update status.")
  }
}

const getStatusClass = (status: string) => {
  switch (status) {
    case 'PENDING_SUBMISSION': return 'bg-yellow-100 text-yellow-800 border-yellow-200 dark:bg-yellow-900/30 dark:text-yellow-400'
    case 'SUBMITTED': return 'bg-blue-100 text-blue-800 border-blue-200 dark:bg-blue-900/30 dark:text-blue-400'
    case 'ACKNOWLEDGED': return 'bg-purple-100 text-purple-800 border-purple-200 dark:bg-purple-900/30 dark:text-purple-400'
    case 'CLOSED': return 'bg-green-100 text-green-800 border-green-200 dark:bg-green-900/30 dark:text-green-400'
    default: return 'bg-gray-100 text-gray-800 border-gray-200'
  }
}

onMounted(() => {
  fetchFIRs()
  fetchAnalytics()
})
</script>

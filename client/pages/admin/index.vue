<template>
  <div class="min-h-screen bg-gray-50 dark:bg-navy-950 p-6">
    <div class="max-w-7xl mx-auto">
      <div class="flex justify-between items-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white">Admin Dashboard - CyberShield</h1>
        <button @click="logout" class="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-lg transition">Logout</button>
      </div>

      <div v-if="loading" class="text-center py-12">
        <p class="text-gray-500 dark:text-gray-400">Loading FIRs...</p>
      </div>
      
      <div v-else class="bg-white dark:bg-navy-900 shadow rounded-lg overflow-hidden border border-gray-200 dark:border-navy-800">
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
const loading = ref(true)

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
})
</script>

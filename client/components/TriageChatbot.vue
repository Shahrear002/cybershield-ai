<template>
  <div class="flex h-full flex-col bg-navy-950">
    <!-- Messages Window -->
    <div 
      ref="messageContainer"
      class="flex-1 overflow-y-auto rounded-xl border border-border bg-surface-raised/40 p-4 space-y-4 custom-scrollbar"
    >
      <div 
        v-for="(msg, idx) in messages" 
        :key="idx"
        class="flex w-full"
        :class="msg.sender === 'user' ? 'justify-end' : 'justify-start'"
      >
        <!-- Chat bubbles -->
        <div 
          class="max-w-[85%] md:max-w-[70%] rounded-xl px-4 py-3 text-sm shadow-md animate-fade-in"
          :class="msg.sender === 'user' 
            ? 'bg-gradient-to-br from-[#0ea5e9] to-[#6366f1] text-white rounded-tr-none' 
            : 'bg-surface border border-border-bright text-slate-200 rounded-tl-none'"
        >
          <!-- Sender Badge -->
          <p class="mb-1 text-[9px] font-semibold tracking-wider uppercase opacity-55">
            {{ msg.sender === 'user' ? 'You' : 'Advocate' }}
          </p>

          <!-- Message content -->
          <div class="whitespace-pre-wrap leading-relaxed">
            {{ msg.text }}
          </div>

          <!-- Embedded summary block (Advocate finished state) -->
          <div v-if="msg.summary" class="mt-3 overflow-hidden rounded-lg border border-border bg-navy-950/80 p-3.5">
            <div class="mb-2 flex items-center justify-between border-b border-border/60 pb-1.5">
              <span class="text-[10px] font-bold uppercase tracking-wider text-cyber-cyan">Structured Incident Report</span>
              <span class="rounded bg-cyber-emerald/10 border border-cyber-emerald/20 px-1 py-0.5 text-[8px] font-semibold uppercase text-cyber-emerald">Ready</span>
            </div>
            <pre class="font-mono text-xs text-slate-300 overflow-x-auto leading-relaxed custom-scrollbar whitespace-pre-wrap">{{ msg.summary }}</pre>
          </div>
        </div>
      </div>

      <!-- File Upload Zone (Step 13) -->
      <div v-if="showFileUpload" class="flex justify-start animate-fade-in">
        <div class="w-full max-w-[85%] md:max-w-[70%] rounded-xl rounded-tl-none border border-border-bright bg-surface p-4 shadow-md">
          <p class="mb-2.5 text-[9px] font-semibold tracking-wider uppercase text-cyber-cyan">
            Evidence Vault &amp; Chain-of-Custody
          </p>
          
          <!-- Loading Spinner during upload -->
          <div v-if="uploading" class="flex flex-col items-center justify-center py-6">
            <svg class="h-8 w-8 animate-spin text-cyber-cyan" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
            <p class="mt-3 text-xs text-slate-400">Uploading and computing SHA-256 hash...</p>
          </div>

          <!-- Drop-zone UI -->
          <div 
            v-else
            @dragover.prevent="dragOver = true"
            @dragleave.prevent="dragOver = false"
            @drop.prevent="handleDrop"
            @click="triggerFileInput"
            class="flex flex-col items-center justify-center rounded-lg border-2 border-dashed px-4 py-6 text-center cursor-pointer transition duration-150"
            :class="dragOver 
              ? 'border-cyber-cyan bg-cyber-cyan/5 text-cyber-cyan' 
              : 'border-border hover:border-border-bright hover:bg-surface-raised text-slate-400'"
          >
            <svg class="h-8 w-8 mb-2 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.75">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 16.5V9.75m0 0l3 3m-3-3l-3 3M6.75 19.5a4.5 4.5 0 01-1.41-8.775 5.25 5.25 0 0110.233-2.33 3 3 0 013.758 3.848A3.752 3.752 0 0118 19.5H6.75z" />
            </svg>
            <p class="text-xs font-semibold text-slate-200">
              Drag &amp; drop screenshots/audio here, or click to browse.
            </p>
            <p class="mt-1 text-[10px] text-slate-500">
              Accepted: Images (PNG, JPEG), PDFs, and Audio files
            </p>
          </div>

          <!-- Optional Skip Button -->
          <div v-if="!uploading" class="mt-3 flex justify-end">
            <button 
              @click="skipEvidence" 
              class="rounded-lg border border-border px-3 py-1.5 text-xs font-medium text-slate-500 hover:border-border-bright hover:bg-surface-raised hover:text-slate-300 transition"
            >
              Skip Upload
            </button>
          </div>
        </div>
      </div>

      <!-- Typing Indicator -->
      <div v-if="typing" class="flex justify-start animate-fade-in">
        <div class="flex items-center gap-1.5 rounded-xl rounded-tl-none border border-border-bright bg-surface px-4 py-3 text-sm shadow-md">
          <span class="h-1.5 w-1.5 animate-bounce rounded-full bg-cyber-cyan" style="animation-delay: 0ms;" />
          <span class="h-1.5 w-1.5 animate-bounce rounded-full bg-cyber-cyan" style="animation-delay: 150ms;" />
          <span class="h-1.5 w-1.5 animate-bounce rounded-full bg-cyber-cyan" style="animation-delay: 300ms;" />
        </div>
      </div>
    </div>

    <!-- Bottom Form / CTAs -->
    <div class="mt-4 flex-shrink-0 px-4 pb-6 md:px-8">
      
      <!-- Completed state CTA box with handoff -->
      <div v-if="isCompleted" class="animate-slide-up space-y-3 rounded-xl border border-cyber-cyan/20 bg-cyber-cyan/5 p-4 text-center">
        <p class="text-sm font-semibold text-slate-200">Incident Details Collected</p>
        <p class="text-xs text-slate-400">Would you like to hand over this structured report to the AI Triage Engine to classify the offense under the Cyber Security Act 2023 and generate a formal FIR complaint?</p>
        <div class="flex flex-col justify-center gap-3 sm:flex-row">
          <button 
            @click="finalizeChatAndTransfer"
            class="group relative flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-cyber-cyan to-cyber-indigo px-6 py-3.5 text-sm font-bold text-white shadow-glow transition hover:opacity-90"
          >
            <span class="pointer-events-none absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/15 to-transparent transition-transform duration-700 group-hover:translate-x-full" />
            <span>Yes, generate FIR &amp; Analyze Case</span>
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3" />
            </svg>
          </button>
          <button 
            @click="resetChat" 
            class="rounded-xl border border-border px-6 py-3.5 text-sm font-semibold text-slate-400 transition hover:border-border-bright hover:bg-surface hover:text-slate-200"
          >
            Restart Chat
          </button>
        </div>
      </div>

      <!-- Messaging Input Form -->
      <form v-else @submit.prevent="handleSend" class="flex gap-3">
        <!-- Paperclip attachment button (Upload at any time!) -->
        <button
          type="button"
          @click="triggerFileInput"
          :disabled="typing || isCompleted || uploading"
          class="flex items-center justify-center rounded-xl border border-border-bright bg-surface px-4 py-3.5 text-slate-300 transition hover:border-cyber-cyan/50 hover:text-cyber-cyan hover:bg-cyber-cyan/5 disabled:cursor-not-allowed disabled:opacity-40"
          title="Upload Evidence screenshots/files"
        >
          <svg v-if="uploading" class="h-5 w-5 animate-spin text-cyber-cyan" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
          </svg>
          <svg v-else class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
          </svg>
        </button>

        <input
          v-model="userInput"
          type="text"
          :placeholder="uploading ? 'Uploading and anchoring evidence...' : showFileUpload ? 'Please upload your evidence or use the paperclip...' : 'Type your response here...'"
          class="flex-1 rounded-xl border border-border bg-navy-900 px-4 py-3.5 text-sm text-slate-200 placeholder-slate-700 transition focus:border-cyber-cyan/50 focus:outline-none focus:ring-1 focus:ring-cyber-cyan/30"
          :disabled="typing || isCompleted || showFileUpload || uploading"
          ref="inputField"
        />
        <button
          type="submit"
          :disabled="!userInput.trim() || typing || isCompleted || showFileUpload || uploading"
          class="flex items-center justify-center rounded-xl bg-gradient-to-br from-[#0ea5e9] to-[#6366f1] px-5 py-3.5 text-white transition hover:opacity-95 disabled:cursor-not-allowed disabled:opacity-40"
        >
          <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 12L3.269 3.126A59.768 59.768 0 0121.485 12 59.77 59.77 0 013.27 20.876L5.999 12zm0 0h7.5" />
          </svg>
        </button>
      </form>
    </div>
    <!-- Hidden File Input for Paperclip/Dropzone uploads -->
    <input 
      ref="fileInput"
      type="file"
      class="hidden"
      accept="image/png, image/jpeg, application/pdf, audio/*"
      @change="handleFileSelect"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'

const config = useRuntimeConfig()
const chatSummary = useChatSummary()

// ── Types ───────────────────────────────────────────────────────────────────
interface Message {
  sender: 'bot' | 'user'
  text: string
  summary?: string
}

// ── Conversational Intake Questions (14 steps) ──────────────────────────────
const QUESTIONS = [
  "Welcome! I am your Digital Victim Advocate. I will guide you through documenting this incident for secure local triage.\n\nFirst, **can you briefly describe what happened?**",
  "Thank you. To help us catalog this, which **social media platform or digital channel** did the incident occur on? (e.g. Facebook, Instagram, WhatsApp, Email, or SMS)",
  "Understood. Do you know any **details about the offender**? (e.g. username handle, profile URL, phone number, or email)",
  "Got it. Did the offender make **specific threats or blackmail demands**? (e.g. money demands via bKash, image leaks, or defamation?)",
  "How has this incident **impacted** you personally or professionally? (e.g. reputational damage, severe mental stress, or safety concerns)",
  "Have you shared these details with any **family members, friends, or trusted contacts**?",
  "Are there any **witnesses** who saw the messages or posts, or who can vouch for what happened?",
  "Has this occurred as a **single isolated event**, or is this a case of **ongoing harassment/stalking**?",
  "Did the incident occur inside a **private conversation** (DM) or on a **public post/comment section**?",
  "Approximately **when** did the incident start, and when was the most recent occurrence?",
  "Have you already made any **formal complaints** (e.g. reporting to the platform or contacting the police)?",
  "Are you experiencing any **direct threat to your physical safety** or are you concerned about physical retaliation?",
  "Does the offender have access to any of your **personal devices or online accounts**?",
  "To establish a legally admissible chain-of-custody, we recommend preserving screenshots or files as locked digital evidence.\n\n**Please upload any screenshots, evidence files, or PDFs below.**",
  "All details have been structured! We are ready to compile the report."
]

// ── State variables ──────────────────────────────────────────────────────────
const userInput = ref('')
const currentStep = ref(0)
const typing = ref(false)
const isCompleted = ref(false)
const messages = reactive<Message[]>([])

// File upload states
const showFileUpload = ref(false)
const uploading = ref(false)
const dragOver = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

const messageContainer = ref<HTMLDivElement | null>(null)
const inputField = ref<HTMLInputElement | null>(null)

// Session ID for evidence tracking
const sessionId = ref(`session-${UUID_like()}`)

// ── Answers collection ──────────────────────────────────────────────────────
const answers = reactive({
  q1: '', q2: '', q3: '', q4: '', q5: '',
  q6: '', q7: '', q8: '', q9: '', q10: '',
  q11: '', q12: '', q13: '',
  evidenceHash: 'No evidence attached',
  evidenceTxId: 'N/A',
  evidenceFile: 'None'
})

// Generate simple unique ID
function UUID_like() {
  return Math.random().toString(36).substring(2, 11)
}

// ── Auto-scroll ─────────────────────────────────────────────────────────────
async function scrollToBottom() {
  await nextTick()
  if (messageContainer.value) {
    messageContainer.value.scrollTo({
      top: messageContainer.value.scrollHeight,
      behavior: 'smooth'
    })
  }
}

// ── Send text replies ────────────────────────────────────────────────────────
async function handleSend() {
  const text = userInput.value.trim()
  if (!text || typing.value || isCompleted.value || showFileUpload.value) return

  // Push user answer
  messages.push({ sender: 'user', text })
  userInput.value = ''
  await scrollToBottom()

  // Save answers dynamically
  const key = `q${currentStep.value + 1}` as keyof typeof answers
  if (key in answers) {
    (answers as any)[key] = text
  }

  currentStep.value++
  await executeBotResponse()
}

// ── Bot answers scheduler ────────────────────────────────────────────────────
async function executeBotResponse() {
  typing.value = true
  await scrollToBottom()

  await new Promise((resolve) => setTimeout(resolve, 800))
  typing.value = false

  // Step 13: Evidence Upload Trigger
  if (currentStep.value === 13) {
    showFileUpload.value = true
    messages.push({
      sender: 'bot',
      text: QUESTIONS[13]
    })
  } else if (currentStep.value < QUESTIONS.length - 1) {
    // Standard questions
    messages.push({
      sender: 'bot',
      text: QUESTIONS[currentStep.value]
    })
  } else {
    // Complete state
    isCompleted.value = true
    const compiled = compileSummary()
    chatSummary.value = compiled

    messages.push({
      sender: 'bot',
      text: "I have successfully compiled all 13 details and cryptographically preserved your evidence receipts in our ledger. Here is your structured case file:",
      summary: compiled
    })
  }

  await scrollToBottom()
  nextTick(() => inputField.value?.focus())
}

// ── Trigger file upload browse ───────────────────────────────────────────────
function triggerFileInput() {
  fileInput.value?.click()
}

// ── Handle file drop ─────────────────────────────────────────────────────────
function handleDrop(event: DragEvent) {
  dragOver.value = false
  const files = event.dataTransfer?.files
  if (files && files.length > 0) {
    uploadFilePayload(files[0])
  }
}

// ── Handle file select ───────────────────────────────────────────────────────
function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (files && files.length > 0) {
    uploadFilePayload(files[0])
  }
}

// ── Upload file logic to Spring Boot `/api/evidence/upload` ───────────────────
async function uploadFilePayload(file: File) {
  // Validate file types (images, PDFs, audio)
  const allowedTypes = ['image/png', 'image/jpeg', 'application/pdf', 'audio/']
  const isValid = allowedTypes.some(type => file.type.startsWith(type))
  
  if (!isValid) {
    alert("Invalid file format. Please upload PNG, JPEG, PDF, or audio evidence.")
    return
  }

  uploading.value = true
  const formData = new FormData()
  formData.append('file', file)
  formData.append('sessionId', sessionId.value)

  try {
    const response = await $fetch<{
      fileName: string,
      fileHash: string,
      transactionId: string,
      timestamp: string,
      message: string
    }>('/api/evidence/upload', {
      baseURL: config.public.apiBase || 'http://localhost:8080',
      method: 'POST',
      body: formData
    })

    // Capture receipt details
    answers.evidenceFile = response.fileName
    answers.evidenceHash = response.fileHash
    answers.evidenceTxId = response.transactionId

    // Hide upload zone
    showFileUpload.value = false
    uploading.value = false

    // Push success conversation blocks
    messages.push({
      sender: 'user',
      text: `I have uploaded the evidence: ${file.name}`
    })
    await scrollToBottom()

    typing.value = true
    await new Promise((resolve) => setTimeout(resolve, 800))
    typing.value = false

    messages.push({
      sender: 'bot',
      text: `Evidence received and secured! 🔐\n\n**SHA-256 Hash**: \`${response.fileHash}\`\n**Blockchain Tx ID**: \`${response.transactionId}\`\n**Chain-of-Custody Timestamp**: ${response.timestamp}\n\nYour file has been anchored in the CyberShield digital vault and is fully ready for legal submission.`
    })

    // Proceed to final review step
    currentStep.value++
    await executeBotResponse()

  } catch (err: any) {
    uploading.value = false
    alert(`Failed to anchor evidence file: ${err.message || 'Server unreachable'}`)
  }
}

// ── Skip Evidence Upload ─────────────────────────────────────────────────────
async function skipEvidence() {
  showFileUpload.value = false
  messages.push({
    sender: 'user',
    text: "Skip evidence upload for now."
  })
  await scrollToBottom()

  currentStep.value++
  await executeBotResponse()
}

// ── Compile Summary ──────────────────────────────────────────────────────────
function compileSummary(): string {
  return `On social platform ${answers.q2 || 'unspecified'}, the offender ${answers.q3 || 'unknown'} targeted the informant.

Incident Details:
- Narrative: ${answers.q1}
- Specific Threats: ${answers.q4}
- Personal Impact: ${answers.q5}
- Ongoing Harassment?: ${answers.q8}
- DM or Public?: ${answers.q9}
- Date range: ${answers.q10}

Witnesses & Backing Info:
- Shared with others?: ${answers.q6}
- Witnesses present?: ${answers.q7}
- Platform complaint filed?: ${answers.q11}
- Physical threat concerns?: ${answers.q12}
- Offender has device access?: ${answers.q13}

Cryptographic Chain of Custody Proofs:
- Evidence File name: ${answers.evidenceFile}
- SHA-256 Proof Checksum: ${answers.evidenceHash}
- Blockchain Anchor Tx ID: ${answers.evidenceTxId}
`
}

// ── Handoff to Triage Page ───────────────────────────────────────────────────
function finalizeChatAndTransfer() {
  const compiledText = compileSummary()
  chatSummary.value = compiledText
  
  navigateTo({
    path: '/advocate',
    query: { autoAnalyze: 'true' }
  })
}

// ── Reset Chat ──────────────────────────────────────────────────────────────
function resetChat() {
  currentStep.value = 0
  isCompleted.value = false
  chatSummary.value = ''
  showFileUpload.value = false
  uploading.value = false
  messages.length = 0
  
  // Clear answers
  Object.keys(answers).forEach((k) => {
    (answers as any)[k] = ''
  })
  answers.evidenceHash = 'No evidence attached'
  answers.evidenceTxId = 'N/A'
  answers.evidenceFile = 'None'

  messages.push({
    sender: 'bot',
    text: QUESTIONS[0]
  })

  scrollToBottom()
  nextTick(() => inputField.value?.focus())
}

// ── Mount hook ───────────────────────────────────────────────────────────────
onMounted(() => {
  resetChat()
})
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.01);
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.1);
}
</style>

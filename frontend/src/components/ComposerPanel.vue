<template>
  <section class="compose-grid">
    <div class="compose-main tool-surface">
      <header class="panel-heading">
        <div>
          <p class="eyebrow">Compose</p>
          <h2>发布朋友圈</h2>
        </div>
        <span class="counter">{{ content.length }}/280</span>
      </header>

      <textarea
        v-model="content"
        maxlength="280"
        rows="8"
        placeholder="把今天值得记录的事情写下来"
        aria-label="朋友圈内容"
      />

      <div class="action-row split">
        <button class="button primary" type="button" :disabled="!content.trim() || saving" @click="submitPost">
          <Send class="button-icon" aria-hidden="true" />
          {{ saving ? '发布中' : '发布' }}
        </button>
        <p v-if="message" :class="messageType">{{ message }}</p>
      </div>
    </div>

    <AiDraftPanel :generate="generatePostDrafts" @select-draft="content = $event" />
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { Send } from 'lucide-vue-next'
import AiDraftPanel from './AiDraftPanel.vue'
import { generatePostDrafts, publishPost } from '../api/client'

const emit = defineEmits(['published'])
const content = ref('')
const message = ref('')
const messageType = ref('muted-message')
const saving = ref(false)

async function submitPost() {
  const trimmed = content.value.trim()
  if (!trimmed) return

  saving.value = true
  const response = await publishPost(trimmed)
  saving.value = false

  if (response.success) {
    content.value = ''
    message.value = '发布成功'
    messageType.value = 'success-message'
    emit('published')
    return
  }
  message.value = response.message || '发布失败'
  messageType.value = 'error-message'
}
</script>

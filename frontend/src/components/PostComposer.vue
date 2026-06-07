<template>
  <section class="surface composer-panel">
    <div class="panel-heading">
      <h2>发布朋友圈</h2>
      <span>{{ content.length }}/280</span>
    </div>

    <textarea v-model="content" maxlength="280" rows="6" aria-label="朋友圈内容"></textarea>

    <div class="composer-actions">
      <button class="primary" :disabled="!content.trim()" @click="submitPost">发布</button>
      <p v-if="message" class="muted">{{ message }}</p>
    </div>

    <AiDraftPanel :generate="generatePostDrafts" @select-draft="content = $event" />
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { generatePostDrafts, publishPost } from '../api/client'
import AiDraftPanel from './AiDraftPanel.vue'

const emit = defineEmits(['published'])
const content = ref('')
const message = ref('')

async function submitPost() {
  const response = await publishPost(content.value)
  if (response.success) {
    message.value = '发布成功'
    content.value = ''
    emit('published')
  } else {
    message.value = response.message || '发布失败'
  }
}
</script>

<template>
  <section class="tool-panel ai-panel">
    <div class="panel-heading">
      <h2>AI 文案助手</h2>
      <span>草稿不会自动发布</span>
    </div>

    <div class="form-grid compact">
      <label>
        主题
        <input name="topic" v-model="topic" aria-label="主题，例如数据库实验或周末复习" />
      </label>
      <label>
        语气
        <select v-model="tone">
          <option>自然</option>
          <option>轻松</option>
          <option>认真</option>
          <option>幽默</option>
        </select>
      </label>
      <label>
        长度
        <select v-model="lengthPreference">
          <option>短</option>
          <option>中等</option>
        </select>
      </label>
    </div>

    <button data-test="generate" class="primary" :disabled="loading" @click="handleGenerate">
      {{ loading ? '生成中...' : '生成文案' }}
    </button>

    <p v-if="error" class="error">{{ error }}</p>

    <ul class="draft-list">
      <li v-for="draft in drafts" :key="draft">
        <p>{{ draft }}</p>
        <button data-test="use-draft" @click="$emit('select-draft', draft)">填入发布框</button>
      </li>
    </ul>
  </section>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  generate: { type: Function, required: true }
})

defineEmits(['select-draft'])

const topic = ref('')
const tone = ref('自然')
const lengthPreference = ref('短')
const drafts = ref([])
const error = ref('')
const loading = ref(false)

async function handleGenerate() {
  error.value = ''
  if (!topic.value.trim()) {
    error.value = '主题不能为空'
    return
  }
  loading.value = true
  try {
    const response = await props.generate({
      topic: topic.value,
      tone: tone.value,
      lengthPreference: lengthPreference.value
    })
    if (!response.success) {
      error.value = response.message || 'AI 生成失败'
      return
    }
    drafts.value = response.data.drafts
  } catch {
    error.value = 'AI 生成失败，不影响手动发布'
  } finally {
    loading.value = false
  }
}
</script>

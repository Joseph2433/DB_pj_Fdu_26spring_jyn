<template>
  <section class="tool-surface ai-panel">
    <header class="panel-heading">
      <div>
        <p class="eyebrow">DeepSeek Assistant</p>
        <h2>AI 文案助手</h2>
      </div>
      <Sparkles class="panel-icon" aria-hidden="true" />
    </header>

    <div class="form-grid three-columns">
      <label>
        主题
        <input name="topic" v-model="topic" placeholder="例如：数据库实验复盘" />
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

    <div class="action-row">
      <button data-test="generate" class="button primary" type="button" :disabled="loading" @click="handleGenerate">
        <Sparkles class="button-icon" aria-hidden="true" />
        {{ loading ? '生成中' : '生成文案' }}
      </button>
      <p v-if="error" class="error-message">{{ error }}</p>
    </div>

    <ul v-if="drafts.length" class="draft-list">
      <li v-for="draft in drafts" :key="draft" class="draft-item">
        <p>{{ draft }}</p>
        <button data-test="use-draft" class="button ghost" type="button" @click="$emit('select-draft', draft)">
          采用
        </button>
      </li>
    </ul>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { Sparkles } from 'lucide-vue-next'

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
      topic: topic.value.trim(),
      tone: tone.value,
      lengthPreference: lengthPreference.value
    })
    if (!response.success) {
      error.value = response.message || 'AI 生成失败'
      return
    }
    drafts.value = response.data?.drafts || []
  } catch {
    error.value = 'AI 生成失败，不影响手动发布'
  } finally {
    loading.value = false
  }
}
</script>

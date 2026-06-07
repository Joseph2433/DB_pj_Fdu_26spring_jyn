<template>
  <form class="comment-composer" :data-test="`comment-form-${postId}`" @submit.prevent="submit">
    <input
      v-model="content"
      maxlength="160"
      :data-test="`comment-input-${postId}`"
      placeholder="写一句评论"
      aria-label="评论内容"
    />
    <button class="icon-button" type="submit" :disabled="!content.trim()" title="发送评论">
      <Send class="button-icon" aria-hidden="true" />
    </button>
  </form>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Send } from 'lucide-vue-next'

const props = defineProps({
  postId: { type: Number, required: true },
  prefill: { type: String, default: '' },
  prefillKey: { type: Number, default: 0 }
})

const emit = defineEmits(['submit'])
const content = ref('')

watch(
  () => [props.prefill, props.prefillKey],
  ([prefill]) => {
    if (prefill) content.value = prefill
  }
)

function submit() {
  const trimmed = content.value.trim()
  if (!trimmed) return
  emit('submit', props.postId, trimmed)
  content.value = ''
}
</script>

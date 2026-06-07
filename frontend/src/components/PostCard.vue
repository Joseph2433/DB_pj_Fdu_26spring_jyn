<template>
  <article class="post-card" :class="{ 'post-card-featured': featured }">
    <header class="post-card-head">
      <div class="author-block">
        <span class="author-avatar">{{ post.authorId }}</span>
        <span>
          <strong>{{ userLabel(post.authorId) }}</strong>
          <small>{{ formatDateTime(post.lastUpdatedAt) }}</small>
        </span>
      </div>
      <span class="meta-pill">
        <MessageCircle class="tiny-icon" aria-hidden="true" />
        {{ commentCount(post) }}
      </span>
      <slot name="actions" />
    </header>

    <p class="post-content">{{ post.content }}</p>

    <slot name="after-content" />

    <ul v-if="post.comments?.length" class="comment-list">
      <li v-for="comment in post.comments" :key="comment.id">
        <div class="comment-head">
          <strong>{{ userLabel(comment.authorId) }}</strong>
          <button
            v-if="canComment"
            class="text-button"
            type="button"
            :data-test="`reply-comment-${comment.id}`"
            @click="replyTo(comment)"
          >
            回复
          </button>
        </div>
        <span>{{ comment.content }}</span>
      </li>
    </ul>

    <CommentComposer
      v-if="canComment"
      :post-id="post.id"
      :prefill="replyPrefill"
      :prefill-key="replyPrefillKey"
      @submit="forwardComment"
    />
  </article>
</template>

<script setup>
import { ref } from 'vue'
import { MessageCircle } from 'lucide-vue-next'
import CommentComposer from './CommentComposer.vue'
import { commentCount, formatDateTime, userLabel } from '../utils/formatters'

defineProps({
  canComment: { type: Boolean, default: true },
  featured: { type: Boolean, default: false },
  post: { type: Object, required: true }
})

const emit = defineEmits(['comment'])
const replyPrefill = ref('')
const replyPrefillKey = ref(0)

function forwardComment(postId, content) {
  emit('comment', postId, content)
}

function replyTo(comment) {
  replyPrefill.value = `回复 ${userLabel(comment.authorId)}：`
  replyPrefillKey.value += 1
}
</script>

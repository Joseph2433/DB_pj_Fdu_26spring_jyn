<template>
  <article class="post-card" :class="{ 'post-card-featured': featured }">
    <header class="post-card-head">
      <div class="author-block">
        <span class="author-avatar">{{ post.authorId }}</span>
        <span>
          <strong>{{ authorLabel(post) }}</strong>
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
          <strong>{{ authorLabel(comment) }}</strong>
          <div
            v-if="canDeleteComment(comment) || canComment"
            class="comment-actions"
            :data-test="`comment-actions-${comment.id}`"
          >
            <button
              v-if="canDeleteComment(comment)"
              class="text-button danger-text"
              type="button"
              :data-test="`delete-comment-${comment.id}`"
              @click="deleteComment(comment)"
            >
              <Trash2 class="tiny-icon" aria-hidden="true" />
              删除
            </button>
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
import { MessageCircle, Trash2 } from 'lucide-vue-next'
import CommentComposer from './CommentComposer.vue'
import { authorLabel, commentCount, formatDateTime } from '../utils/formatters'

const props = defineProps({
  canComment: { type: Boolean, default: true },
  currentUserId: { type: [Number, String], default: null },
  featured: { type: Boolean, default: false },
  post: { type: Object, required: true }
})

const emit = defineEmits(['comment', 'delete-comment'])
const replyPrefill = ref('')
const replyPrefillKey = ref(0)

function forwardComment(postId, content) {
  emit('comment', postId, content)
}

function replyTo(comment) {
  replyPrefill.value = `回复 ${authorLabel(comment)}：`
  replyPrefillKey.value += 1
}

function canDeleteComment(comment) {
  if (props.currentUserId === null || props.currentUserId === undefined) {
    return false
  }
  const currentUserId = Number(props.currentUserId)
  return Number(comment.authorId) === currentUserId || Number(props.post.authorId) === currentUserId
}

function deleteComment(comment) {
  emit('delete-comment', props.post.id, comment.id)
}
</script>

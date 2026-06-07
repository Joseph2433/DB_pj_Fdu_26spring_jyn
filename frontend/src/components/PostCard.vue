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
    </header>

    <p class="post-content">{{ post.content }}</p>

    <ul v-if="post.comments?.length" class="comment-list">
      <li v-for="comment in post.comments" :key="comment.id">
        <strong>{{ userLabel(comment.authorId) }}</strong>
        <span>{{ comment.content }}</span>
      </li>
    </ul>

    <CommentComposer v-if="canComment" :post-id="post.id" @submit="forwardComment" />
  </article>
</template>

<script setup>
import { MessageCircle } from 'lucide-vue-next'
import CommentComposer from './CommentComposer.vue'
import { commentCount, formatDateTime, userLabel } from '../utils/formatters'

defineProps({
  canComment: { type: Boolean, default: true },
  featured: { type: Boolean, default: false },
  post: { type: Object, required: true }
})

const emit = defineEmits(['comment'])

function forwardComment(postId, content) {
  emit('comment', postId, content)
}
</script>

<template>
  <section class="magazine-hero">
    <div>
      <p class="eyebrow">Friend Feed</p>
      <h2>好友朋友圈</h2>
      <p class="hero-note">正文和评论关键词都可以命中。</p>
    </div>

    <form data-test="feed-search" class="search-bar hero-search" @submit.prevent="loadFeed">
      <div class="search-field">
        <Search class="search-icon" aria-hidden="true" />
        <input data-test="feed-keyword" v-model="keyword" placeholder="搜索正文或评论" aria-label="朋友圈关键词" />
      </div>
      <button class="button primary" type="submit">搜索</button>
    </form>
  </section>

  <p v-if="loading" class="muted-message">正在加载...</p>
  <p v-else-if="posts.length === 0" class="empty-state">没有找到可见朋友圈</p>

  <section v-else class="feed-masonry">
    <PostCard
      v-for="(post, index) in posts"
      :key="post.id"
      :post="post"
      :current-user-id="currentUserId"
      :featured="index === 0"
      @comment="submitComment"
      @delete-comment="submitDeleteComment"
    />
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Search } from 'lucide-vue-next'
import PostCard from '../components/PostCard.vue'
import { addComment, deleteComment, fetchFeed, getMe } from '../api/client'

const keyword = ref('')
const posts = ref([])
const loading = ref(false)
const currentUserId = ref(null)

async function loadCurrentUser() {
  const response = await getMe()
  if (response.success) {
    currentUserId.value = Number(response.data.id)
  }
}

async function loadFeed() {
  loading.value = true
  try {
    const response = await fetchFeed(keyword.value.trim())
    posts.value = response.success ? response.data : []
  } finally {
    loading.value = false
  }
}

async function submitComment(postId, content) {
  const response = await addComment(postId, content)
  if (response.success) await loadFeed()
}

async function submitDeleteComment(postId, commentId) {
  const response = await deleteComment(postId, commentId)
  if (response.success) await loadFeed()
}

onMounted(() => {
  loadCurrentUser()
  loadFeed()
})
</script>

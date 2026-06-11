<template>
  <section class="page-intro friend-posts-intro">
    <div>
      <p class="eyebrow">Friend Timeline</p>
      <h2>{{ friendName }} 的动态</h2>
      <p v-if="friendUsername" class="hero-note">@{{ friendUsername }}</p>
    </div>
    <RouterLink class="button ghost" to="/user/friends">返回好友</RouterLink>
  </section>

  <p v-if="loading" class="muted-message">正在加载...</p>
  <p v-else-if="posts.length === 0" class="empty-state">这个好友还没有可见动态</p>

  <section v-else class="feed-masonry">
    <PostCard
      v-for="post in posts"
      :key="post.id"
      :post="post"
      :current-user-id="currentUserId"
      @comment="submitComment"
      @delete-comment="submitDeleteComment"
    />
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PostCard from '../components/PostCard.vue'
import { addComment, deleteComment, fetchFriendPosts, getMe } from '../api/client'

const route = useRoute()
const posts = ref([])
const loading = ref(false)
const currentUserId = ref(null)

const friendId = computed(() => Number(route.params.friendId))
const friendName = computed(() => String(route.query.name || route.query.username || posts.value[0]?.authorUsername || '好友'))
const friendUsername = computed(() => route.query.username ? String(route.query.username) : '')

async function loadCurrentUser() {
  const response = await getMe()
  if (response.success) {
    currentUserId.value = Number(response.data.id)
  }
}

async function loadPosts() {
  loading.value = true
  try {
    const response = await fetchFriendPosts(friendId.value)
    posts.value = response.success ? response.data : []
  } finally {
    loading.value = false
  }
}

async function submitComment(postId, content) {
  const response = await addComment(postId, content)
  if (response.success) await loadPosts()
}

async function submitDeleteComment(postId, commentId) {
  const response = await deleteComment(postId, commentId)
  if (response.success) await loadPosts()
}

watch(friendId, loadPosts)
onMounted(() => {
  loadCurrentUser()
  loadPosts()
})
</script>

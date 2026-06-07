<template>
  <section class="surface feed-panel">
    <header class="panel-heading">
      <div>
        <h2>好友朋友圈</h2>
        <span>按正文或评论关键词筛选</span>
      </div>
      <form class="search-row" @submit.prevent="loadFeed">
        <input v-model="keyword" aria-label="朋友圈关键词" />
        <button class="primary" type="submit">搜索</button>
        <button type="button" @click="clearSearch">清空</button>
      </form>
    </header>

    <p v-if="loading" class="muted">正在加载...</p>
    <p v-else-if="posts.length === 0" class="empty">没有找到可见朋友圈</p>

    <article v-for="post in posts" :key="post.id" class="post-item">
      <div class="post-meta">
        <strong>用户 {{ post.authorId }}</strong>
        <span>{{ formatTime(post.lastUpdatedAt) }}</span>
      </div>
      <p>{{ post.content }}</p>
      <form class="comment-row" @submit.prevent="submitComment(post.id)">
        <input v-model="commentDrafts[post.id]" aria-label="评论内容" />
        <button type="submit">评论</button>
      </form>
      <ul class="comment-list">
        <li v-for="comment in post.comments || []" :key="comment.id">
          用户 {{ comment.authorId }}：{{ comment.content }}
        </li>
      </ul>
    </article>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { addComment, fetchFeed } from '../api/client'

const keyword = ref('')
const posts = ref([])
const loading = ref(false)
const commentDrafts = reactive({})

async function loadFeed() {
  loading.value = true
  try {
    const response = await fetchFeed(keyword.value)
    posts.value = response.success ? response.data : []
  } finally {
    loading.value = false
  }
}

async function submitComment(postId) {
  const content = (commentDrafts[postId] || '').trim()
  if (!content) return
  const response = await addComment(postId, content)
  if (response.success) {
    commentDrafts[postId] = ''
    await loadFeed()
  }
}

function clearSearch() {
  keyword.value = ''
  loadFeed()
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : ''
}

onMounted(loadFeed)
defineExpose({ loadFeed })
</script>

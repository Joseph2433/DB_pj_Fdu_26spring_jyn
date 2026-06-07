<template>
  <section class="page-intro">
    <div>
      <p class="eyebrow">My Posts</p>
      <h2>我的朋友圈</h2>
    </div>
    <button class="icon-button" type="button" title="刷新动态" @click="loadPosts">
      <RefreshCw class="button-icon" aria-hidden="true" />
    </button>
  </section>

  <section class="my-post-list">
    <article v-for="post in posts" :key="post.id" class="tool-surface editable-post">
      <textarea v-model="edits[post.id]" maxlength="280" rows="4" aria-label="修改朋友圈内容" />
      <div class="action-row split">
        <span class="muted-message">{{ formatDateTime(post.lastUpdatedAt) }}</span>
        <div class="button-cluster">
          <button class="button ghost" type="button" @click="save(post.id)">保存</button>
          <button class="button danger" type="button" @click="remove(post.id)">删除</button>
        </div>
      </div>
      <ul v-if="post.comments?.length" class="comment-list">
        <li v-for="comment in post.comments" :key="comment.id">
          <strong>{{ userLabel(comment.authorId) }}</strong>
          <span>{{ comment.content }}</span>
        </li>
      </ul>
    </article>
    <p v-if="posts.length === 0" class="empty-state">还没有发布朋友圈</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { RefreshCw } from 'lucide-vue-next'
import { deletePost, fetchMyPosts, updatePost } from '../api/client'
import { formatDateTime, userLabel } from '../utils/formatters'

const posts = ref([])
const edits = reactive({})

async function loadPosts() {
  const response = await fetchMyPosts()
  posts.value = response.success ? response.data : []
  posts.value.forEach((post) => {
    edits[post.id] = post.content
  })
}

async function save(postId) {
  const response = await updatePost(postId, edits[postId])
  if (response.success) await loadPosts()
}

async function remove(postId) {
  const response = await deletePost(postId)
  if (response.success) await loadPosts()
}

onMounted(loadPosts)
</script>

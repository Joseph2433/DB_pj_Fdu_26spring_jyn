<template>
  <section class="surface">
    <div class="panel-heading">
      <h2>我的朋友圈</h2>
      <button @click="loadPosts">刷新</button>
    </div>

    <p v-if="posts.length === 0" class="empty">还没有发布朋友圈</p>
    <article v-for="post in posts" :key="post.id" class="post-item compact-post">
      <textarea v-model="edits[post.id]" maxlength="280" rows="3" aria-label="修改朋友圈内容"></textarea>
      <div class="composer-actions">
        <span>{{ formatTime(post.lastUpdatedAt) }}</span>
        <button @click="save(post.id)">保存</button>
        <button class="danger" @click="remove(post.id)">删除</button>
      </div>
    </article>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { deletePost, fetchMyPosts, updatePost } from '../api/client'

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

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : ''
}

onMounted(loadPosts)
defineExpose({ loadPosts })
</script>

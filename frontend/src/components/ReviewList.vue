<template>
  <section class="review-layout">
    <div class="tool-surface review-list">
      <header class="panel-heading">
        <div>
          <p class="eyebrow">Review Queue</p>
          <h2>朋友圈审核</h2>
        </div>
        <button class="icon-button" type="button" title="刷新列表" @click="loadPosts">
          <RefreshCw class="button-icon" aria-hidden="true" />
        </button>
      </header>

      <div class="review-table">
        <article v-for="post in posts" :key="post.postId" class="review-row">
          <div>
            <strong>动态 #{{ post.postId }}</strong>
            <small>{{ authorLabel(post) }} · {{ formatDateTime(post.lastUpdatedAt) }}</small>
          </div>
          <p>{{ post.content }}</p>
          <span class="meta-pill">{{ post.status }} · 评论 {{ post.commentCount }}</span>
          <button class="button danger" type="button" @click="removePost(post.postId)">
            <Trash2 class="button-icon" aria-hidden="true" />
            删除
          </button>
          <button class="button ghost" type="button" @click="targetUser = post.authorId">选择用户</button>
        </article>
      </div>

      <p v-if="posts.length === 0" class="empty-state">暂无可审核朋友圈</p>
    </div>

    <div class="tool-surface danger-zone">
      <header class="panel-heading">
        <div>
          <p class="eyebrow">Danger Zone</p>
          <h2>注销用户</h2>
        </div>
        <ShieldAlert class="panel-icon" aria-hidden="true" />
      </header>
      <div class="inline-form">
        <input v-model.number="targetUser" type="number" min="1" placeholder="用户 ID" aria-label="用户 ID" />
        <button class="button danger" type="button" :disabled="!targetUser" @click="removeUser">
          注销
        </button>
      </div>
      <p v-if="message" :class="messageType">{{ message }}</p>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RefreshCw, ShieldAlert, Trash2 } from 'lucide-vue-next'
import { adminDeletePost, adminDeleteUser, fetchAdminPosts } from '../api/client'
import { authorLabel, formatDateTime } from '../utils/formatters'

const posts = ref([])
const targetUser = ref(null)
const message = ref('')
const messageType = ref('muted-message')

async function loadPosts() {
  const response = await fetchAdminPosts()
  posts.value = response.success ? response.data : []
}

async function removePost(postId) {
  const response = await adminDeletePost(postId)
  if (response.success) {
    await loadPosts()
  }
}

async function removeUser() {
  if (!targetUser.value) return
  const response = await adminDeleteUser(targetUser.value)
  message.value = response.success ? '用户已注销' : response.message || '注销失败'
  messageType.value = response.success ? 'success-message' : 'error-message'
  if (response.success) {
    targetUser.value = null
    await loadPosts()
  }
}

onMounted(loadPosts)
</script>

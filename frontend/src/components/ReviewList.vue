<template>
  <section class="review-layout">
    <div class="tool-surface review-list">
      <header class="panel-heading">
        <div>
          <p class="eyebrow">Review Queue</p>
          <h2>朋友圈审核</h2>
        </div>
        <div class="review-toolbar">
          <div class="segmented compact-segmented" aria-label="审核视图切换">
            <button
              data-test="review-mode-list"
              type="button"
              :class="{ active: viewMode === 'list' }"
              @click="viewMode = 'list'"
            >
              队列
            </button>
            <button
              data-test="review-mode-card"
              type="button"
              :class="{ active: viewMode === 'card' }"
              @click="viewMode = 'card'"
            >
              朋友圈
            </button>
          </div>
          <button class="icon-button" type="button" title="刷新列表" @click="loadPosts">
            <RefreshCw class="button-icon" aria-hidden="true" />
          </button>
          <button class="button ghost" data-test="audit-log-open" type="button" @click="openAuditLogs">
            <ScrollText class="button-icon" aria-hidden="true" />
            日志
          </button>
        </div>
      </header>

      <div v-if="viewMode === 'list'" class="review-table">
        <article v-for="post in posts" :key="post.postId" class="review-row">
          <div>
            <strong>动态 #{{ post.postId }}</strong>
            <small>{{ authorLabel(post) }} · {{ formatDateTime(post.lastUpdatedAt) }}</small>
          </div>
          <p>{{ post.content }}</p>
          <span class="meta-pill">{{ post.status }} · 评论 {{ post.commentCount }}</span>
          <button class="button danger" type="button" :data-test="`admin-delete-post-${post.postId}`" @click="removePost(post.postId)">
            <Trash2 class="button-icon" aria-hidden="true" />
            删除
          </button>
        </article>
      </div>
      <section v-else class="feed-masonry review-card-feed">
        <PostCard
          v-for="post in posts"
          :key="post.postId"
          :post="reviewPostCard(post)"
          :can-comment="false"
        >
          <template #actions>
            <button class="button danger" type="button" :data-test="`admin-delete-post-${post.postId}`" @click="removePost(post.postId)">
              <Trash2 class="button-icon" aria-hidden="true" />
              删除
            </button>
          </template>
          <template #after-content>
            <div class="review-card-meta">
              <span class="meta-pill review-status-pill">{{ post.status }}</span>
              <section
                v-if="reviewComments(post).length"
                class="review-comment-panel"
                :data-test="`review-comments-${post.postId}`"
              >
                <div class="review-comment-panel-head">
                  <strong>评论明细</strong>
                  <span>{{ reviewComments(post).length }} 条</span>
                </div>
                <ul class="review-comment-list">
                  <li v-for="(comment, index) in reviewComments(post)" :key="comment.id || `${post.postId}-${index}`">
                    <div class="review-comment-line">
                      <strong>{{ authorLabel(comment) }}</strong>
                      <small>{{ formatDateTime(comment.createdAt) }}</small>
                    </div>
                    <p>{{ comment.content }}</p>
                  </li>
                </ul>
              </section>
              <p
                v-else-if="commentCount(post) > 0"
                class="review-comment-missing"
                :data-test="`review-comments-${post.postId}`"
              >
                当前有 {{ commentCount(post) }} 条评论，接口未返回评论明细
              </p>
            </div>
          </template>
        </PostCard>
      </section>

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
        <input
          data-test="delete-user-account"
          v-model="targetUsername"
          type="text"
          placeholder="用户账号"
          aria-label="用户账号"
        />
        <button
          class="button danger"
          data-test="delete-user-submit"
          type="button"
          :disabled="!targetUsername.trim()"
          @click="removeUser"
        >
          注销
        </button>
      </div>
      <p v-if="message" :class="messageType">{{ message }}</p>
    </div>

    <div v-if="auditDialogOpen" class="dialog-backdrop" data-test="audit-log-dialog" @click.self="closeAuditLogs">
      <section class="friend-request-dialog audit-log-dialog" role="dialog" aria-modal="true">
        <header class="panel-heading">
          <div>
            <p class="eyebrow">Audit Logs</p>
            <h2>审计日志</h2>
          </div>
          <div class="button-cluster">
            <button class="icon-button" type="button" title="刷新日志" @click="loadAuditLogs">
              <RefreshCw class="button-icon" aria-hidden="true" />
            </button>
            <button class="icon-button" type="button" title="关闭" @click="closeAuditLogs">
              <X class="button-icon" aria-hidden="true" />
            </button>
          </div>
        </header>

        <p v-if="auditLoading" class="muted-message">正在读取日志...</p>
        <p v-else-if="auditLogs.length === 0" class="empty-state">暂无审计日志</p>

        <ul v-else class="audit-log-list">
          <li v-for="log in auditLogs" :key="log.id">
            <div class="audit-log-main">
              <strong>{{ log.action }}</strong>
              <span>{{ formatDateTime(log.createdAt) }}</span>
            </div>
            <div class="audit-log-meta">
              <span class="meta-pill">动态 {{ log.postId ?? '-' }}</span>
              <span class="meta-pill">管理员 {{ log.adminUsername || 'system' }}</span>
            </div>
            <p>{{ log.reason || '无说明' }}</p>
          </li>
        </ul>

        <p v-if="auditError" class="error-message">{{ auditError }}</p>
      </section>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RefreshCw, ScrollText, ShieldAlert, Trash2, X } from 'lucide-vue-next'
import { adminDeletePost, adminDeleteUser, fetchAdminAuditLogs, fetchAdminPosts } from '../api/client'
import PostCard from './PostCard.vue'
import { authorLabel, commentCount, formatDateTime } from '../utils/formatters'

const posts = ref([])
const auditLogs = ref([])
const auditDialogOpen = ref(false)
const auditLoading = ref(false)
const auditError = ref('')
const targetUsername = ref('')
const message = ref('')
const messageType = ref('muted-message')
const viewMode = ref('list')

async function loadPosts() {
  const response = await fetchAdminPosts()
  posts.value = response.success ? response.data : []
}

async function loadAuditLogs() {
  auditLoading.value = true
  auditError.value = ''
  try {
    const response = await fetchAdminAuditLogs()
    auditLogs.value = response.success ? response.data : []
    auditError.value = response.success ? '' : response.message || '日志读取失败'
  } finally {
    auditLoading.value = false
  }
}

async function openAuditLogs() {
  auditDialogOpen.value = true
  await loadAuditLogs()
}

function closeAuditLogs() {
  auditDialogOpen.value = false
  auditError.value = ''
}

async function removePost(postId) {
  const response = await adminDeletePost(postId)
  if (response.success) {
    await loadPosts()
  }
}

async function removeUser() {
  const username = targetUsername.value.trim()
  if (!username) return
  const response = await adminDeleteUser(username)
  message.value = response.success ? '用户已注销' : response.message || '注销失败'
  messageType.value = response.success ? 'success-message' : 'error-message'
  if (response.success) {
    targetUsername.value = ''
    await loadPosts()
  }
}

function reviewPostCard(post) {
  return {
    id: post.postId,
    authorId: post.authorId,
    authorUsername: post.authorUsername,
    content: post.content,
    lastUpdatedAt: post.lastUpdatedAt,
    commentCount: post.commentCount,
    comments: []
  }
}

function reviewComments(post) {
  return Array.isArray(post.comments) ? post.comments : []
}

onMounted(loadPosts)
</script>

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
import PostCard from './PostCard.vue'
import { authorLabel, commentCount, formatDateTime } from '../utils/formatters'

const posts = ref([])
const targetUser = ref(null)
const message = ref('')
const messageType = ref('muted-message')
const viewMode = ref('list')

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

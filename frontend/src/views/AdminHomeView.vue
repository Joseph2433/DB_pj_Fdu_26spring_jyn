<template>
  <main class="app-shell">
    <header class="topbar admin">
      <div>
        <p class="eyebrow">Admin Review</p>
        <h1>管理员审核台</h1>
      </div>
      <button @click="goHome">返回登录</button>
    </header>

    <section class="surface profile-panel">
      <div class="panel-heading">
        <h2>管理员资料</h2>
        <button @click="saveAdmin">保存</button>
      </div>
      <label>姓名<input v-model="admin.displayName" /></label>
      <p v-if="message" class="muted">{{ message }}</p>
    </section>

    <section class="surface">
      <div class="panel-heading">
        <h2>朋友圈审核</h2>
        <button @click="loadPosts">刷新</button>
      </div>
      <article v-for="post in posts" :key="post.postId" class="post-item review-item">
        <div class="post-meta">
          <strong>作者 ID {{ post.authorId }}</strong>
          <span>{{ formatTime(post.lastUpdatedAt) }} · 评论 {{ post.commentCount }}</span>
        </div>
        <p>{{ post.content }}</p>
        <div class="composer-actions">
          <button class="danger" @click="removePost(post.postId)">删除朋友圈</button>
          <button @click="targetUser = post.authorId">选择注销该用户</button>
        </div>
      </article>
    </section>

    <section class="surface user-delete">
      <h2>注销用户</h2>
      <div class="inline-form">
        <input v-model.number="targetUser" type="number" min="1" aria-label="用户 ID" />
        <button class="danger" @click="removeUser">注销</button>
      </div>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminDeletePost, adminDeleteUser, fetchAdminPosts, getAdminMe, updateAdminMe } from '../api/client'

const router = useRouter()
const posts = ref([])
const targetUser = ref(null)
const message = ref('')
const admin = reactive({ displayName: '' })

async function loadAdmin() {
  const response = await getAdminMe()
  if (response.success) Object.assign(admin, response.data)
}

async function saveAdmin() {
  const response = await updateAdminMe({ displayName: admin.displayName })
  message.value = response.success ? '已保存' : response.message
}

async function loadPosts() {
  const response = await fetchAdminPosts()
  posts.value = response.success ? response.data : []
}

async function removePost(postId) {
  const response = await adminDeletePost(postId)
  if (response.success) await loadPosts()
}

async function removeUser() {
  if (!targetUser.value) return
  const response = await adminDeleteUser(targetUser.value)
  if (response.success) {
    targetUser.value = null
    await loadPosts()
  }
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 19) : ''
}

function goHome() {
  router.push('/')
}

onMounted(() => {
  loadAdmin()
  loadPosts()
})
</script>

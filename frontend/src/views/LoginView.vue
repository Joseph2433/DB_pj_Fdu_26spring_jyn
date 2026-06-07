<template>
  <main class="login-layout">
    <section class="login-copy">
      <div class="login-kicker">
        <span class="brand-mark login-brand-mark">L5</span>
        <div>
          <p class="eyebrow">Database Lab5</p>
          <strong>Social Circle Console</strong>
        </div>
      </div>

      <div class="login-hero-copy">
        <h1>Lab5 朋友圈</h1>
        <p>一个面向数据库应用开发验收的社交工作台：好友分组、朋友圈动态、评论互动、管理员审核和 AI 文案辅助都在同一套界面里完成。</p>
      </div>

      <div class="login-badges">
        <span><Users class="tiny-icon" aria-hidden="true" />好友分组</span>
        <span><MessageCircle class="tiny-icon" aria-hidden="true" />朋友圈</span>
        <span><Sparkles class="tiny-icon" aria-hidden="true" />AI 文案</span>
      </div>

      <div class="login-showcase">
        <section class="showcase-panel showcase-flow">
          <div class="showcase-heading">
            <p class="eyebrow">Demo Flow</p>
            <h2>演示流程</h2>
          </div>
          <ol>
            <li>
              <span>01</span>
              <strong>用户登录</strong>
              <small>进入好友、分组和个人信息工作台</small>
            </li>
            <li>
              <span>02</span>
              <strong>朋友圈互动</strong>
              <small>发布动态、搜索内容、评论与回复</small>
            </li>
            <li>
              <span>03</span>
              <strong>AI 文案助手</strong>
              <small>生成候选文案，再由用户确认发布</small>
            </li>
            <li>
              <span>04</span>
              <strong>管理员审核</strong>
              <small>完成动态审核与注销流程演示</small>
            </li>
          </ol>
        </section>

        <section class="showcase-panel showcase-schema">
          <div class="showcase-heading">
            <p class="eyebrow">Database Objects</p>
            <h2>核心数据</h2>
          </div>
          <div class="schema-grid" aria-label="核心数据表">
            <span>users</span>
            <span>friendships</span>
            <span>friend_groups</span>
            <span>posts</span>
            <span>comments</span>
            <span>audit_logs</span>
          </div>
        </section>
      </div>
    </section>

    <section class="login-panel">
      <header class="login-panel-head">
        <p class="eyebrow">Sign In</p>
        <h2>{{ mode === 'register' ? '创建账号' : '进入工作台' }}</h2>
      </header>

      <div class="segmented">
        <button data-test="login-mode-user" type="button" :class="{ active: mode === 'user' }" @click="switchMode('user')">用户</button>
        <button data-test="login-mode-admin" type="button" :class="{ active: mode === 'admin' }" @click="switchMode('admin')">管理员</button>
        <button data-test="login-mode-register" type="button" :class="{ active: mode === 'register' }" @click="switchMode('register')">注册</button>
      </div>

      <form @submit.prevent="submit">
        <label>
          账号
          <input v-model="form.username" autocomplete="username" />
        </label>
        <label v-if="mode === 'register'">
          昵称
          <input v-model="form.displayName" />
        </label>
        <label>
          密码
          <input v-model="form.password" type="password" autocomplete="current-password" />
        </label>
        <button class="button primary full" type="submit">
          {{ submitText }}
        </button>
        <button
          v-if="mode !== 'register'"
          class="text-button forgot-password-link"
          data-test="forgot-password-toggle"
          type="button"
          @click="toggleReset"
        >
          忘记密码
        </button>
      </form>

      <section v-if="resetOpen && mode !== 'register'" class="password-reset-panel">
        <header>
          <p class="eyebrow">{{ mode === 'admin' ? 'Admin Recovery' : 'User Recovery' }}</p>
          <h3>重置密码</h3>
        </header>
        <label>
          账号
          <input data-test="reset-username" v-model="resetForm.username" autocomplete="username" />
        </label>
        <label>
          新密码
          <input data-test="reset-new-password" v-model="resetForm.newPassword" type="password" autocomplete="new-password" />
        </label>
        <label>
          确认新密码
          <input data-test="reset-confirm-password" v-model="resetForm.confirmPassword" type="password" autocomplete="new-password" />
        </label>
        <button class="button primary full" data-test="reset-password-submit" type="button" @click="resetPassword">
          重置密码
        </button>
        <p v-if="resetMessage" :class="resetMessageType">{{ resetMessage }}</p>
      </section>

      <p v-if="message" class="error-message">{{ message }}</p>
    </section>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MessageCircle, Sparkles, Users } from 'lucide-vue-next'
import { loginAdmin, loginUser, registerUser, resetAdminPassword, resetUserPassword } from '../api/client'

const router = useRouter()
const route = useRoute()
const mode = ref('user')
const message = ref('')
const resetOpen = ref(false)
const resetMessage = ref('')
const resetMessageType = ref('muted-message')
const form = reactive({
  username: '',
  password: '',
  displayName: ''
})
const resetForm = reactive({
  username: '',
  newPassword: '',
  confirmPassword: ''
})

const submitText = computed(() => {
  if (mode.value === 'admin') return '管理员登录'
  if (mode.value === 'register') return '注册账号'
  return '用户登录'
})

async function submit() {
  message.value = ''
  const response = mode.value === 'admin'
    ? await loginAdmin({ username: form.username, password: form.password })
    : mode.value === 'register'
      ? await registerUser({ username: form.username, password: form.password, displayName: form.displayName })
      : await loginUser({ username: form.username, password: form.password })

  if (!response.success) {
    message.value = response.message
    return
  }
  if (mode.value === 'register') {
    mode.value = 'user'
    message.value = '注册成功，请登录'
    return
  }
  const fallbackPath = mode.value === 'admin' ? '/admin/review' : '/user/feed'
  const allowedPrefix = mode.value === 'admin' ? '/admin' : '/user'
  const redirectPath = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  router.push(redirectPath.startsWith(allowedPrefix) ? redirectPath : fallbackPath)
}

function switchMode(nextMode) {
  mode.value = nextMode
  resetOpen.value = false
  resetMessage.value = ''
}

function toggleReset() {
  resetOpen.value = !resetOpen.value
  resetMessage.value = ''
  if (resetOpen.value && !resetForm.username) {
    resetForm.username = form.username
  }
}

async function resetPassword() {
  resetMessage.value = ''
  if (resetForm.newPassword !== resetForm.confirmPassword) {
    resetMessage.value = '两次输入的新密码不一致'
    resetMessageType.value = 'error-message'
    return
  }
  const payload = {
    username: resetForm.username,
    newPassword: resetForm.newPassword,
    confirmPassword: resetForm.confirmPassword
  }
  const response = mode.value === 'admin' ? await resetAdminPassword(payload) : await resetUserPassword(payload)
  resetMessage.value = response.success ? '密码已重置，请重新登录' : response.message || '密码重置失败'
  resetMessageType.value = response.success ? 'success-message' : 'error-message'
  if (response.success) {
    form.username = resetForm.username
    form.password = ''
    resetForm.newPassword = ''
    resetForm.confirmPassword = ''
  }
}
</script>

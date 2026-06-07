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

      <div class="login-preview" aria-hidden="true">
        <div class="preview-topline">
          <span>今日动态</span>
          <strong>12 条更新</strong>
        </div>
        <article class="preview-post">
          <div class="author-block">
            <span class="author-avatar">A</span>
            <span>
              <strong>Alice</strong>
              <small>刚刚发布</small>
            </span>
          </div>
          <p>数据库 Lab5 收尾：朋友圈搜索、分组管理和 AI 文案助手都跑通了。</p>
          <div class="preview-comment">
            <MessageCircle class="tiny-icon" aria-hidden="true" />
            Bob 回复：演示路径很清楚
          </div>
        </article>
        <div class="preview-metrics">
          <span><strong>8</strong> 好友</span>
          <span><strong>4</strong> 分组</span>
          <span><strong>3</strong> 待审核</span>
        </div>
      </div>
    </section>

    <section class="login-panel">
      <header class="login-panel-head">
        <p class="eyebrow">Sign In</p>
        <h2>{{ mode === 'register' ? '创建账号' : '进入工作台' }}</h2>
      </header>

      <div class="segmented">
        <button type="button" :class="{ active: mode === 'user' }" @click="mode = 'user'">用户</button>
        <button type="button" :class="{ active: mode === 'admin' }" @click="mode = 'admin'">管理员</button>
        <button type="button" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
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
      </form>

      <p v-if="message" class="error-message">{{ message }}</p>
    </section>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MessageCircle, Sparkles, Users } from 'lucide-vue-next'
import { loginAdmin, loginUser, registerUser } from '../api/client'

const router = useRouter()
const route = useRoute()
const mode = ref('user')
const message = ref('')
const form = reactive({
  username: 'alice',
  password: '123456',
  displayName: ''
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
</script>

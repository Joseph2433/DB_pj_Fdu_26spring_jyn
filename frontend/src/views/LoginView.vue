<template>
  <main class="login-layout">
    <section class="login-copy">
      <p class="eyebrow">Database Lab5</p>
      <h1>Lab5 朋友圈</h1>
      <p>登录后进入侧栏工作台，完成好友、分组、动态、评论、审核和 AI 文案演示。</p>
      <div class="login-badges">
        <span><Users class="tiny-icon" aria-hidden="true" />好友分组</span>
        <span><MessageCircle class="tiny-icon" aria-hidden="true" />朋友圈</span>
        <span><Sparkles class="tiny-icon" aria-hidden="true" />AI 文案</span>
      </div>
    </section>

    <section class="login-panel">
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
        <button class="button primary full" type="submit">{{ submitText }}</button>
      </form>

      <p v-if="message" class="error-message">{{ message }}</p>
    </section>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { MessageCircle, Sparkles, Users } from 'lucide-vue-next'
import { loginAdmin, loginUser, registerUser } from '../api/client'

const router = useRouter()
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
  router.push(mode.value === 'admin' ? '/admin/review' : '/user/feed')
}
</script>

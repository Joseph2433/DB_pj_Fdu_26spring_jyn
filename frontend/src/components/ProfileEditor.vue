<template>
  <section class="tool-surface profile-editor">
    <header class="panel-heading">
      <div>
        <p class="eyebrow">{{ mode === 'admin' ? 'Admin Profile' : 'User Profile' }}</p>
        <h2>{{ mode === 'admin' ? '管理员资料' : '个人资料' }}</h2>
      </div>
      <button class="button primary" type="button" @click="saveProfile">
        <Save class="button-icon" aria-hidden="true" />
        保存
      </button>
    </header>

    <div class="form-grid" :class="{ 'single-column': mode === 'admin' }">
      <label>
        昵称
        <input v-model="profile.displayName" />
      </label>
      <label v-if="mode === 'user'">
        性别
        <input v-model="profile.gender" />
      </label>
      <label v-if="mode === 'user'">
        生日
        <input v-model="profile.birthday" type="date" />
      </label>
      <label v-if="mode === 'user'">
        年龄
        <input v-model.number="profile.age" type="number" min="0" max="150" />
      </label>
    </div>

    <p v-if="message" :class="messageType">{{ message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Save } from 'lucide-vue-next'
import { getAdminMe, getMe, updateAdminMe, updateMe } from '../api/client'

const props = defineProps({
  mode: { type: String, default: 'user' }
})

const profile = reactive({
  displayName: '',
  gender: '',
  birthday: '',
  age: null
})
const message = ref('')
const messageType = ref('muted-message')

async function loadProfile() {
  const response = props.mode === 'admin' ? await getAdminMe() : await getMe()
  if (response.success) {
    Object.assign(profile, response.data)
  } else {
    message.value = response.message || '资料读取失败'
    messageType.value = 'error-message'
  }
}

async function saveProfile() {
  const payload = props.mode === 'admin'
    ? { displayName: profile.displayName }
    : {
        displayName: profile.displayName,
        gender: profile.gender,
        birthday: profile.birthday || null,
        age: profile.age
      }
  const response = props.mode === 'admin' ? await updateAdminMe(payload) : await updateMe(payload)
  message.value = response.success ? '已保存' : response.message || '保存失败'
  messageType.value = response.success ? 'success-message' : 'error-message'
}

onMounted(loadProfile)
</script>

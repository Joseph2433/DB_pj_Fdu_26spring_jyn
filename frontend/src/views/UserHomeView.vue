<template>
  <main class="app-shell">
    <header class="topbar">
      <div>
        <p class="eyebrow">User Workspace</p>
        <h1>Lab5 社交平台</h1>
      </div>
      <button @click="goLogout">退出</button>
    </header>

    <section class="surface profile-panel">
      <div class="panel-heading">
        <h2>个人资料</h2>
        <button @click="saveProfile">保存</button>
      </div>
      <div class="form-grid">
        <label>姓名<input v-model="profile.displayName" /></label>
        <label>性别<input v-model="profile.gender" /></label>
        <label>生日<input v-model="profile.birthday" type="date" /></label>
        <label>年龄<input v-model.number="profile.age" type="number" min="0" max="150" /></label>
      </div>
      <p v-if="profileMessage" class="muted">{{ profileMessage }}</p>
    </section>

    <div class="workspace-grid">
      <div class="left-stack">
        <PostComposer @published="reloadPosts" />
        <MyPosts ref="myPostsRef" />
      </div>
      <div class="right-stack">
        <FeedSearch ref="feedRef" />
        <FriendManager />
      </div>
    </div>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import FeedSearch from '../components/FeedSearch.vue'
import FriendManager from '../components/FriendManager.vue'
import MyPosts from '../components/MyPosts.vue'
import PostComposer from '../components/PostComposer.vue'
import { getMe, logout, updateMe } from '../api/client'

const router = useRouter()
const feedRef = ref(null)
const myPostsRef = ref(null)
const profileMessage = ref('')
const profile = reactive({
  displayName: '',
  gender: '',
  birthday: '',
  age: null
})

async function loadProfile() {
  const response = await getMe()
  if (response.success) {
    Object.assign(profile, response.data)
  }
}

async function saveProfile() {
  const response = await updateMe(profile)
  profileMessage.value = response.success ? '已保存' : response.message
}

function reloadPosts() {
  feedRef.value?.loadFeed()
  myPostsRef.value?.loadPosts()
}

async function goLogout() {
  await logout()
  router.push('/')
}

onMounted(loadProfile)
</script>

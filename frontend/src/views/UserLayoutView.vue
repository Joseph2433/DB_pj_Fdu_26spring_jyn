<template>
  <AppShell
    home-path="/user/feed"
    :nav-items="navItems"
    :profile="profile"
    role-label="用户工作台"
    title="Lab5 朋友圈"
    @logout="goLogout"
  >
    <RouterView />
  </AppShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { FileText, Search, Settings, Sparkles, Users } from 'lucide-vue-next'
import AppShell from '../components/AppShell.vue'
import { getMe, logout } from '../api/client'

const router = useRouter()
const profile = ref(null)
const navItems = [
  { to: '/user/feed', label: '好友动态', icon: Search },
  { to: '/user/compose', label: '发布', icon: Sparkles },
  { to: '/user/posts', label: '我的动态', icon: FileText },
  { to: '/user/friends', label: '好友分组', icon: Users },
  { to: '/user/profile', label: '个人资料', icon: Settings }
]

async function loadProfile() {
  const response = await getMe()
  if (response.success) profile.value = response.data
}

async function goLogout() {
  await logout()
  router.push('/')
}

onMounted(loadProfile)
</script>

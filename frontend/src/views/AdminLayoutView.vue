<template>
  <AppShell
    home-path="/admin/review"
    :nav-items="navItems"
    :profile="profile"
    role-label="管理员工作台"
    title="审核中心"
    @logout="goLogout"
  >
    <RouterView />
  </AppShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Settings, ShieldCheck } from 'lucide-vue-next'
import AppShell from '../components/AppShell.vue'
import { getAdminMe, logout } from '../api/client'

const router = useRouter()
const profile = ref(null)
const navItems = [
  { to: '/admin/review', label: '审核列表', icon: ShieldCheck },
  { to: '/admin/profile', label: '管理员资料', icon: Settings }
]

async function loadProfile() {
  const response = await getAdminMe()
  if (response.success) profile.value = response.data
}

async function goLogout() {
  await logout()
  router.push('/')
}

onMounted(loadProfile)
</script>

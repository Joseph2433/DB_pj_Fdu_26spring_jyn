<template>
  <div class="workspace-frame">
    <aside class="sidebar">
      <RouterLink class="brand-lockup" :to="homePath" aria-label="返回工作台首页">
        <span class="brand-mark">L5</span>
        <span>
          <strong>Lab5 Circle</strong>
          <small>{{ roleLabel }}</small>
        </span>
      </RouterLink>

      <nav class="side-nav" aria-label="主导航">
        <RouterLink v-for="item in navItems" :key="item.to" class="side-link" :to="item.to">
          <component :is="item.icon" class="nav-icon" aria-hidden="true" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <button class="button ghost logout-button" type="button" @click="$emit('logout')">
        <LogOut class="button-icon" aria-hidden="true" />
        退出登录
      </button>
    </aside>

    <div class="stage">
      <header class="stage-topbar">
        <div>
          <p class="eyebrow">{{ roleLabel }}</p>
          <h1>{{ title }}</h1>
        </div>
        <div class="identity-chip">
          <CircleUserRound class="identity-icon" aria-hidden="true" />
          <span>
            <strong>{{ profile?.displayName || '未命名账号' }}</strong>
            <small>{{ profile?.username || '会话读取中' }}</small>
          </span>
        </div>
      </header>

      <nav class="mobile-nav" aria-label="移动端导航">
        <RouterLink v-for="item in navItems" :key="item.to" class="mobile-link" :to="item.to">
          <component :is="item.icon" class="nav-icon" aria-hidden="true" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>

      <main class="stage-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import { CircleUserRound, LogOut } from 'lucide-vue-next'

defineProps({
  homePath: { type: String, required: true },
  navItems: { type: Array, required: true },
  profile: { type: Object, default: null },
  roleLabel: { type: String, required: true },
  title: { type: String, required: true }
})

defineEmits(['logout'])
</script>

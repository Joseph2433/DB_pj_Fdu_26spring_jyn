<template>
  <section class="directory-layout">
    <div class="tool-surface">
      <header class="panel-heading">
        <div>
          <p class="eyebrow">Groups</p>
          <h2>好友分组</h2>
        </div>
        <button class="icon-button" type="button" title="刷新好友" @click="loadAll">
          <RefreshCw class="button-icon" aria-hidden="true" />
        </button>
      </header>

      <form data-test="group-form" class="inline-form" @submit.prevent="submitGroup">
        <input data-test="group-name" v-model="newGroup" placeholder="新分组名称" />
        <button class="button primary" type="submit">
          <Plus class="button-icon" aria-hidden="true" />
          创建
        </button>
      </form>

      <div class="group-strip">
        <span v-for="group in groups" :key="group.id" class="group-chip managed-group-chip">
          <span>{{ group.name }}</span>
          <button
            class="chip-icon-button"
            type="button"
            title="删除分组，好友将归入未分组"
            :data-test="`delete-group-${group.id}`"
            @click="submitDeleteGroup(group.id)"
          >
            <Trash2 class="tiny-icon" aria-hidden="true" />
          </button>
        </span>
        <span v-if="groups.length === 0" class="empty-inline">暂无分组</span>
      </div>
    </div>

    <div class="tool-surface">
      <header class="panel-heading">
        <div>
          <p class="eyebrow">Discover</p>
          <h2>搜索用户</h2>
        </div>
      </header>

      <form data-test="user-search-form" class="search-bar" @submit.prevent="search">
        <Search class="search-icon" aria-hidden="true" />
        <input data-test="user-keyword" v-model="keyword" placeholder="输入用户名或昵称" />
        <button class="button primary" type="submit">搜索</button>
      </form>

      <ul class="search-results">
        <li v-for="user in searchResults" :key="user.id">
          <span>
            <strong>{{ user.displayName }}</strong>
            <small>{{ user.username }}</small>
          </span>
          <select v-model="selectedGroups[user.id]">
            <option :value="null">未分组</option>
            <option v-for="group in groups" :key="group.id" :value="group.id">{{ group.name }}</option>
          </select>
          <button class="button ghost" type="button" :data-test="`add-friend-${user.id}`" @click="submitAddFriend(user.id)">
            添加
          </button>
        </li>
      </ul>
    </div>

    <div class="tool-surface directory-wide">
      <header class="panel-heading">
        <div>
          <p class="eyebrow">Contacts</p>
          <h2>我的好友</h2>
        </div>
        <div class="friend-panel-tools">
          <select data-test="friend-group-filter" v-model="selectedFriendGroup" aria-label="按分组筛选好友">
            <option value="__all__">全部分组</option>
            <option value="__ungrouped__">未分组</option>
            <option v-for="group in groups" :key="group.id" :value="String(group.id)">{{ group.name }}</option>
          </select>
          <span class="counter">{{ filteredFriends.length }} / {{ friends.length }} 人</span>
        </div>
      </header>

      <div class="friend-table">
        <article v-for="friend in filteredFriends" :key="friend.friendshipId" class="friend-row">
          <div class="friend-person">
            <span class="author-avatar">{{ friend.friendId }}</span>
            <span>
              <strong>{{ friend.displayName }}</strong>
              <small>{{ friend.username }} · {{ friend.groupName || '未分组' }}</small>
            </span>
          </div>
          <select
            :data-test="`friend-group-${friend.friendId}`"
            :value="friend.groupId ?? ''"
            @change="submitMove(friend.friendId, $event.target.value)"
          >
            <option value="">未分组</option>
            <option v-for="group in groups" :key="group.id" :value="group.id">{{ group.name }}</option>
          </select>
          <button class="icon-button danger" type="button" title="删除好友" @click="submitDelete(friend.friendId)">
            <Trash2 class="button-icon" aria-hidden="true" />
          </button>
        </article>
      </div>

      <p v-if="friends.length === 0" class="empty-state">还没有好友</p>
      <p v-else-if="filteredFriends.length === 0" class="empty-state">当前分组暂无好友</p>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus, RefreshCw, Search, Trash2 } from 'lucide-vue-next'
import {
  addFriend,
  createGroup,
  deleteFriend,
  deleteGroup,
  fetchFriends,
  fetchGroups,
  moveFriend,
  searchUsers
} from '../api/client'

const groups = ref([])
const friends = ref([])
const keyword = ref('')
const newGroup = ref('')
const searchResults = ref([])
const selectedGroups = reactive({})
const selectedFriendGroup = ref('__all__')

const filteredFriends = computed(() => {
  if (selectedFriendGroup.value === '__all__') return friends.value
  if (selectedFriendGroup.value === '__ungrouped__') {
    return friends.value.filter((friend) => friend.groupId == null)
  }
  return friends.value.filter((friend) => String(friend.groupId) === selectedFriendGroup.value)
})

async function loadAll() {
  const [groupResponse, friendResponse] = await Promise.all([fetchGroups(), fetchFriends()])
  groups.value = groupResponse.success ? groupResponse.data : []
  friends.value = friendResponse.success ? friendResponse.data : []
  normalizeFriendGroupFilter()
}

async function submitGroup() {
  const name = newGroup.value.trim()
  if (!name) return
  const response = await createGroup(name)
  if (response.success) {
    newGroup.value = ''
    await loadAll()
  }
}

async function submitDeleteGroup(groupId) {
  const response = await deleteGroup(groupId)
  if (response.success) {
    if (selectedFriendGroup.value === String(groupId)) {
      selectedFriendGroup.value = '__ungrouped__'
    }
    await loadAll()
  }
}

async function search() {
  const response = await searchUsers(keyword.value.trim())
  searchResults.value = response.success ? response.data : []
}

async function submitAddFriend(friendId) {
  const groupId = selectedGroups[friendId] || null
  const response = await addFriend(friendId, groupId)
  if (response.success) await loadAll()
}

async function submitMove(friendId, rawGroupId) {
  const groupId = rawGroupId ? Number(rawGroupId) : null
  const response = await moveFriend(friendId, groupId)
  if (response.success) await loadAll()
}

async function submitDelete(friendId) {
  const response = await deleteFriend(friendId)
  if (response.success) await loadAll()
}

function normalizeFriendGroupFilter() {
  if (selectedFriendGroup.value === '__all__' || selectedFriendGroup.value === '__ungrouped__') return
  const groupStillExists = groups.value.some((group) => String(group.id) === selectedFriendGroup.value)
  if (!groupStillExists) selectedFriendGroup.value = '__all__'
}

onMounted(loadAll)
</script>

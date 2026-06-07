<template>
  <section class="surface friend-panel">
    <div class="panel-heading">
      <h2>好友与分组</h2>
      <button @click="loadAll">刷新</button>
    </div>

    <form class="inline-form" @submit.prevent="submitGroup">
      <input v-model="newGroup" aria-label="新分组名" />
      <button type="submit">创建分组</button>
    </form>

    <form class="search-row" @submit.prevent="search">
      <input v-model="keyword" aria-label="搜索用户" />
      <button class="primary" type="submit">搜索用户</button>
    </form>

    <ul class="search-results">
      <li v-for="user in searchResults" :key="user.id">
        <span>{{ user.username }} / {{ user.displayName }}</span>
        <select v-model="selectedGroups[user.id]">
          <option :value="null">未分组</option>
          <option v-for="group in groups" :key="group.id" :value="group.id">{{ group.name }}</option>
        </select>
        <button @click="submitAddFriend(user.id)">添加</button>
      </li>
    </ul>

    <div class="friend-list">
      <article v-for="friend in friends" :key="friend.friendshipId" class="friend-row">
        <div>
          <strong>{{ friend.displayName }}</strong>
          <span>{{ friend.username }} · {{ friend.groupName || '未分组' }}</span>
        </div>
        <select :value="friend.groupId" @change="submitMove(friend.friendId, $event.target.value)">
          <option value="">未分组</option>
          <option v-for="group in groups" :key="group.id" :value="group.id">{{ group.name }}</option>
        </select>
        <button class="danger" @click="submitDelete(friend.friendId)">删除</button>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  addFriend,
  createGroup,
  deleteFriend,
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

async function loadAll() {
  const [groupResponse, friendResponse] = await Promise.all([fetchGroups(), fetchFriends()])
  groups.value = groupResponse.success ? groupResponse.data : []
  friends.value = friendResponse.success ? friendResponse.data : []
}

async function submitGroup() {
  if (!newGroup.value.trim()) return
  const response = await createGroup(newGroup.value)
  if (response.success) {
    newGroup.value = ''
    await loadAll()
  }
}

async function search() {
  const response = await searchUsers(keyword.value)
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

onMounted(loadAll)
</script>

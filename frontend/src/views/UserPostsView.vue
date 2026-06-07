<template>
  <section class="page-intro">
    <div>
      <p class="eyebrow">My Posts</p>
      <h2>我的朋友圈</h2>
    </div>
    <button class="icon-button" type="button" title="刷新动态" @click="loadPosts">
      <RefreshCw class="button-icon" aria-hidden="true" />
    </button>
  </section>

  <section class="feed-masonry">
    <PostCard
      v-for="post in posts"
      :key="post.id"
      :post="post"
      @comment="submitComment"
    >
      <template #actions>
        <div class="button-cluster post-owner-actions">
          <button class="button ghost visibility-action" type="button" :data-test="`allow-post-${post.id}`" @click="openVisibility(post, 'ALLOW')">
            <Eye class="tiny-icon" aria-hidden="true" />
            设置可见
          </button>
          <button class="button ghost visibility-action" type="button" :data-test="`deny-post-${post.id}`" @click="openVisibility(post, 'DENY')">
            <EyeOff class="tiny-icon" aria-hidden="true" />
            设置不可见
          </button>
          <button class="button ghost" type="button" :data-test="`edit-post-${post.id}`" @click="startEdit(post)">
            编辑
          </button>
          <button class="button danger" type="button" :data-test="`delete-post-${post.id}`" @click="remove(post.id)">
            删除
          </button>
        </div>
      </template>

      <template #after-content>
        <form
          v-if="editingPostId === post.id"
          class="post-edit-panel"
          :data-test="`edit-form-${post.id}`"
          @submit.prevent="save(post.id)"
        >
          <textarea
            v-model="edits[post.id]"
            maxlength="280"
            rows="4"
            :data-test="`edit-content-${post.id}`"
            aria-label="修改朋友圈内容"
          />
          <div class="action-row split">
            <span class="counter">{{ edits[post.id]?.length || 0 }}/280</span>
            <div class="button-cluster">
              <button class="button primary" type="submit">保存</button>
              <button class="button ghost" type="button" @click="cancelEdit">取消</button>
            </div>
          </div>
        </form>
      </template>
    </PostCard>

    <p v-if="posts.length === 0" class="empty-state">还没有发布朋友圈</p>
  </section>

  <div v-if="visibilityDialogOpen" class="dialog-backdrop" data-test="visibility-dialog" @click.self="closeVisibility">
    <section class="friend-request-dialog visibility-dialog" role="dialog" aria-modal="true">
      <header class="panel-heading">
        <div>
          <p class="eyebrow">Visibility</p>
          <h2>{{ visibilityTitle }}</h2>
        </div>
        <button class="icon-button" type="button" title="关闭" @click="closeVisibility">
          <X class="button-icon" aria-hidden="true" />
        </button>
      </header>

      <div class="segmented visibility-tabs" aria-label="可见性对象">
        <button
          type="button"
          :class="{ active: visibilityTargetType === 'GROUP' }"
          data-test="visibility-tab-GROUP"
          @click="switchVisibilityTarget('GROUP')"
        >
          分组
        </button>
        <button
          type="button"
          :class="{ active: visibilityTargetType === 'USER' }"
          data-test="visibility-tab-USER"
          @click="switchVisibilityTarget('USER')"
        >
          某些好友
        </button>
      </div>

      <div v-if="visibilityLoading" class="empty-state">正在读取可见范围</div>

      <div v-else class="visibility-target-list">
        <template v-if="visibilityTargetType === 'GROUP'">
          <label
            v-for="group in groups"
            :key="group.id"
            class="visibility-option"
          >
            <input
              type="checkbox"
              :checked="isVisibilityTargetSelected(group.id)"
              :data-test="`visibility-target-GROUP-${group.id}`"
              @change="toggleVisibilityTarget(group.id, $event.target.checked)"
            />
            <span>
              <strong>{{ group.name }}</strong>
              <small>分组</small>
            </span>
          </label>
        </template>

        <template v-else>
          <label
            v-for="friend in friends"
            :key="friend.friendId"
            class="visibility-option"
          >
            <input
              type="checkbox"
              :checked="isVisibilityTargetSelected(friend.friendId)"
              :data-test="`visibility-target-USER-${friend.friendId}`"
              @change="toggleVisibilityTarget(friend.friendId, $event.target.checked)"
            />
            <span>
              <strong>{{ friend.displayName || friend.username }}</strong>
              <small>{{ friend.username }}</small>
            </span>
          </label>
        </template>

        <p v-if="visibilityTargetType === 'GROUP' && groups.length === 0" class="empty-state">还没有好友分组</p>
        <p v-if="visibilityTargetType === 'USER' && friends.length === 0" class="empty-state">还没有好友</p>
      </div>

      <p v-if="visibilityError" class="error-message">{{ visibilityError }}</p>

      <div class="action-row split">
        <span class="counter">{{ selectedVisibilityIds.length }} 项已选</span>
        <div class="button-cluster">
          <button class="button ghost" type="button" @click="closeVisibility">取消</button>
          <button class="button primary" type="button" data-test="save-visibility" :disabled="visibilitySaving" @click="saveVisibility">
            保存设置
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Eye, EyeOff, RefreshCw, X } from 'lucide-vue-next'
import PostCard from '../components/PostCard.vue'
import {
  addComment,
  deletePost,
  fetchFriends,
  fetchGroups,
  fetchMyPosts,
  fetchPostVisibility,
  updatePost,
  updatePostVisibility
} from '../api/client'

const posts = ref([])
const edits = reactive({})
const editingPostId = ref(null)
const groups = ref([])
const friends = ref([])
const visibilityRules = ref([])
const visibilityDialogOpen = ref(false)
const visibilityLoading = ref(false)
const visibilitySaving = ref(false)
const visibilityError = ref('')
const visibilityPost = ref(null)
const visibilityRuleType = ref('ALLOW')
const visibilityTargetType = ref('GROUP')
const selectedVisibilityIds = ref([])

const visibilityTitle = computed(() => (
  visibilityRuleType.value === 'ALLOW' ? '设置可见范围' : '设置不可见范围'
))

async function loadPosts() {
  const response = await fetchMyPosts()
  posts.value = response.success ? response.data : []
  posts.value.forEach((post) => {
    edits[post.id] = post.content
  })
}

function startEdit(post) {
  edits[post.id] = post.content
  editingPostId.value = post.id
}

function cancelEdit() {
  editingPostId.value = null
}

async function submitComment(postId, content) {
  const response = await addComment(postId, content)
  if (response.success) await loadPosts()
}

async function save(postId) {
  const response = await updatePost(postId, edits[postId])
  if (response.success) {
    editingPostId.value = null
    await loadPosts()
  }
}

async function remove(postId) {
  const response = await deletePost(postId)
  if (response.success) await loadPosts()
}

async function openVisibility(post, ruleType) {
  visibilityPost.value = post
  visibilityRuleType.value = ruleType
  visibilityTargetType.value = 'GROUP'
  visibilityRules.value = []
  selectedVisibilityIds.value = []
  visibilityError.value = ''
  visibilityDialogOpen.value = true
  visibilityLoading.value = true

  const [groupsResponse, friendsResponse, rulesResponse] = await Promise.all([
    fetchGroups(),
    fetchFriends(),
    fetchPostVisibility(post.id)
  ])

  groups.value = groupsResponse.success ? groupsResponse.data : []
  friends.value = friendsResponse.success ? friendsResponse.data : []
  visibilityRules.value = rulesResponse.success ? rulesResponse.data : []
  visibilityError.value = [groupsResponse, friendsResponse, rulesResponse]
    .find((response) => !response.success)?.message || ''
  syncSelectedVisibilityIds()
  visibilityLoading.value = false
}

function closeVisibility() {
  visibilityDialogOpen.value = false
  visibilityPost.value = null
  visibilityError.value = ''
}

function switchVisibilityTarget(targetType) {
  visibilityTargetType.value = targetType
  syncSelectedVisibilityIds()
}

function syncSelectedVisibilityIds() {
  selectedVisibilityIds.value = visibilityRules.value
    .filter((rule) => rule.ruleType === visibilityRuleType.value && rule.targetType === visibilityTargetType.value)
    .map((rule) => Number(rule.targetId))
}

function isVisibilityTargetSelected(targetId) {
  return selectedVisibilityIds.value.includes(Number(targetId))
}

function toggleVisibilityTarget(targetId, checked) {
  const normalizedId = Number(targetId)
  if (checked) {
    if (!selectedVisibilityIds.value.includes(normalizedId)) {
      selectedVisibilityIds.value = [...selectedVisibilityIds.value, normalizedId]
    }
    return
  }
  selectedVisibilityIds.value = selectedVisibilityIds.value.filter((id) => id !== normalizedId)
}

async function saveVisibility() {
  if (!visibilityPost.value) return
  visibilitySaving.value = true
  visibilityError.value = ''
  const response = await updatePostVisibility(visibilityPost.value.id, {
    ruleType: visibilityRuleType.value,
    targetType: visibilityTargetType.value,
    targetIds: selectedVisibilityIds.value
  })
  visibilitySaving.value = false

  if (response.success) {
    closeVisibility()
    return
  }
  visibilityError.value = response.message || '可见范围保存失败'
}

onMounted(loadPosts)
</script>

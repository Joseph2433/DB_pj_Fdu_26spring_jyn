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
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { RefreshCw } from 'lucide-vue-next'
import PostCard from '../components/PostCard.vue'
import { addComment, deletePost, fetchMyPosts, updatePost } from '../api/client'

const posts = ref([])
const edits = reactive({})
const editingPostId = ref(null)

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

onMounted(loadPosts)
</script>

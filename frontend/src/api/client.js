import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  withCredentials: true
})

http.interceptors.response.use(
  (response) => response,
  (error) => Promise.resolve(error.response || {
    data: { success: false, data: null, message: error.message || '请求失败' }
  })
)

export async function registerUser(payload) {
  const { data } = await http.post('/auth/register', payload)
  return data
}

export async function loginUser(payload) {
  const { data } = await http.post('/auth/login', payload)
  return data
}

export async function loginAdmin(payload) {
  const { data } = await http.post('/admin/auth/login', payload)
  return data
}

export async function logout() {
  const { data } = await http.post('/auth/logout')
  return data
}

export async function getMe() {
  const { data } = await http.get('/users/me')
  return data
}

export async function updateMe(payload) {
  const { data } = await http.put('/users/me', payload)
  return data
}

export async function searchUsers(keyword) {
  const { data } = await http.get('/friends/search', { params: { keyword } })
  return data
}

export async function fetchFriends() {
  const { data } = await http.get('/friends')
  return data
}

export async function fetchGroups() {
  const { data } = await http.get('/friend-groups')
  return data
}

export async function createGroup(name) {
  const { data } = await http.post('/friend-groups', { name })
  return data
}

export async function addFriend(friendId, groupId = null) {
  const { data } = await http.post('/friends', { friendId, groupId })
  return data
}

export async function deleteFriend(friendId) {
  const { data } = await http.delete(`/friends/${friendId}`)
  return data
}

export async function moveFriend(friendId, groupId) {
  const { data } = await http.put(`/friends/${friendId}/group`, { groupId })
  return data
}

export async function fetchFeed(keyword = '') {
  const { data } = await http.get('/posts/feed', { params: { keyword } })
  return data
}

export async function fetchMyPosts() {
  const { data } = await http.get('/posts/mine')
  return data
}

export async function generatePostDrafts(payload) {
  const { data } = await http.post('/ai/post-drafts', payload)
  return data
}

export async function publishPost(content) {
  const { data } = await http.post('/posts', { content })
  return data
}

export async function updatePost(postId, content) {
  const { data } = await http.put(`/posts/${postId}`, { content })
  return data
}

export async function deletePost(postId) {
  const { data } = await http.delete(`/posts/${postId}`)
  return data
}

export async function addComment(postId, content) {
  const { data } = await http.post(`/posts/${postId}/comments`, { content })
  return data
}

export async function getAdminMe() {
  const { data } = await http.get('/admin/me')
  return data
}

export async function updateAdminMe(payload) {
  const { data } = await http.put('/admin/me', payload)
  return data
}

export async function fetchAdminPosts() {
  const { data } = await http.get('/admin/posts')
  return data
}

export async function adminDeletePost(postId) {
  const { data } = await http.delete(`/admin/posts/${postId}`)
  return data
}

export async function adminDeleteUser(userId) {
  const { data } = await http.delete(`/admin/users/${userId}`)
  return data
}

export default http

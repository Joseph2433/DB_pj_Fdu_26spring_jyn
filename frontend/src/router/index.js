import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import UserLayoutView from '../views/UserLayoutView.vue'
import UserFeedView from '../views/UserFeedView.vue'
import UserComposeView from '../views/UserComposeView.vue'
import UserFriendPostsView from '../views/UserFriendPostsView.vue'
import UserPostsView from '../views/UserPostsView.vue'
import UserFriendsView from '../views/UserFriendsView.vue'
import UserProfileView from '../views/UserProfileView.vue'
import AdminLayoutView from '../views/AdminLayoutView.vue'
import AdminReviewView from '../views/AdminReviewView.vue'
import AdminProfileView from '../views/AdminProfileView.vue'
import { getAdminMe, getMe } from '../api/client'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginView },
    {
      path: '/user',
      component: UserLayoutView,
      redirect: '/user/feed',
      meta: { requiresAuth: 'user' },
      children: [
        { path: 'feed', component: UserFeedView },
        { path: 'compose', component: UserComposeView },
        { path: 'posts', component: UserPostsView },
        { path: 'friends', component: UserFriendsView },
        { path: 'friends/:friendId/posts', component: UserFriendPostsView },
        { path: 'profile', component: UserProfileView }
      ]
    },
    {
      path: '/admin',
      component: AdminLayoutView,
      redirect: '/admin/review',
      meta: { requiresAuth: 'admin' },
      children: [
        { path: 'review', component: AdminReviewView },
        { path: 'profile', component: AdminProfileView }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const requiredAuth = to.matched.find((route) => route.meta.requiresAuth)?.meta.requiresAuth
  if (!requiredAuth) return true

  const response = requiredAuth === 'admin' ? await getAdminMe() : await getMe()
  if (response.success) return true

  return { path: '/login', query: { redirect: to.fullPath } }
})

export default router

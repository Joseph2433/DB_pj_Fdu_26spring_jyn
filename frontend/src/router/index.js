import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import UserLayoutView from '../views/UserLayoutView.vue'
import UserFeedView from '../views/UserFeedView.vue'
import UserComposeView from '../views/UserComposeView.vue'
import UserPostsView from '../views/UserPostsView.vue'
import UserFriendsView from '../views/UserFriendsView.vue'
import UserProfileView from '../views/UserProfileView.vue'
import AdminLayoutView from '../views/AdminLayoutView.vue'
import AdminReviewView from '../views/AdminReviewView.vue'
import AdminProfileView from '../views/AdminProfileView.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: LoginView },
    {
      path: '/user',
      component: UserLayoutView,
      redirect: '/user/feed',
      children: [
        { path: 'feed', component: UserFeedView },
        { path: 'compose', component: UserComposeView },
        { path: 'posts', component: UserPostsView },
        { path: 'friends', component: UserFriendsView },
        { path: 'profile', component: UserProfileView }
      ]
    },
    {
      path: '/admin',
      component: AdminLayoutView,
      redirect: '/admin/review',
      children: [
        { path: 'review', component: AdminReviewView },
        { path: 'profile', component: AdminProfileView }
      ]
    }
  ]
})

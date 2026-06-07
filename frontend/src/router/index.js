import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import UserHomeView from '../views/UserHomeView.vue'
import AdminHomeView from '../views/AdminHomeView.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: LoginView },
    { path: '/user', component: UserHomeView },
    { path: '/admin', component: AdminHomeView }
  ]
})

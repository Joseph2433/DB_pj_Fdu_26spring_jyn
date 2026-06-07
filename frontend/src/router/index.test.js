import { describe, expect, it } from 'vitest'
import router from './index'

describe('router', () => {
  it('uses /login as the login entry and redirects / to it', () => {
    const routes = router.options.routes

    expect(routes.find((route) => route.path === '/login')?.component).toBeTruthy()
    expect(routes.find((route) => route.path === '/')?.redirect).toBe('/login')
  })

  it('marks user and admin workspaces as protected routes', () => {
    const routes = router.options.routes

    expect(routes.find((route) => route.path === '/user')?.meta?.requiresAuth).toBe('user')
    expect(routes.find((route) => route.path === '/admin')?.meta?.requiresAuth).toBe('admin')
  })
})

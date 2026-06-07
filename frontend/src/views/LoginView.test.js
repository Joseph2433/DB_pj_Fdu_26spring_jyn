import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import LoginView from './LoginView.vue'
import * as api from '../api/client'

const { routerPush } = vi.hoisted(() => ({
  routerPush: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ query: {} }),
  useRouter: () => ({ push: routerPush })
}))

vi.mock('../api/client', () => ({
  loginAdmin: vi.fn(),
  loginUser: vi.fn(),
  registerUser: vi.fn(),
  resetAdminPassword: vi.fn(),
  resetUserPassword: vi.fn()
}))

describe('LoginView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    routerPush.mockClear()
  })

  it('starts with blank credentials and no today activity block', () => {
    const wrapper = mount(LoginView)
    const inputs = wrapper.findAll('input')

    expect(inputs[0].element.value).toBe('')
    expect(inputs[1].element.value).toBe('')
    expect(wrapper.text()).not.toContain('今日动态')
    expect(wrapper.text()).toContain('演示流程')
  })

  it('lets a user reset a forgotten password from the login page', async () => {
    api.resetUserPassword.mockResolvedValue({ success: true })
    const wrapper = mount(LoginView)

    await wrapper.find('[data-test="forgot-password-toggle"]').trigger('click')
    await wrapper.find('[data-test="reset-username"]').setValue('alice')
    await wrapper.find('[data-test="reset-new-password"]').setValue('newpass')
    await wrapper.find('[data-test="reset-confirm-password"]').setValue('newpass')
    await wrapper.find('[data-test="reset-password-submit"]').trigger('click')
    await flushPromises()

    expect(api.resetUserPassword).toHaveBeenCalledWith({
      username: 'alice',
      newPassword: 'newpass',
      confirmPassword: 'newpass'
    })
    expect(wrapper.text()).toContain('密码已重置，请重新登录')
  })

  it('lets an admin reset a forgotten password from the login page', async () => {
    api.resetAdminPassword.mockResolvedValue({ success: true })
    const wrapper = mount(LoginView)

    await wrapper.find('[data-test="login-mode-admin"]').trigger('click')
    await wrapper.find('[data-test="forgot-password-toggle"]').trigger('click')
    await wrapper.find('[data-test="reset-username"]').setValue('admin')
    await wrapper.find('[data-test="reset-new-password"]').setValue('newpass')
    await wrapper.find('[data-test="reset-confirm-password"]').setValue('newpass')
    await wrapper.find('[data-test="reset-password-submit"]').trigger('click')
    await flushPromises()

    expect(api.resetAdminPassword).toHaveBeenCalledWith({
      username: 'admin',
      newPassword: 'newpass',
      confirmPassword: 'newpass'
    })
  })
})

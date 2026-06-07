import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ProfileEditor from './ProfileEditor.vue'
import * as api from '../api/client'

const { routerPush } = vi.hoisted(() => ({
  routerPush: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: routerPush })
}))

vi.mock('../api/client', () => ({
  getAdminMe: vi.fn(),
  getMe: vi.fn(),
  logout: vi.fn(),
  updateAdminPassword: vi.fn(),
  updateAdminMe: vi.fn(),
  updatePassword: vi.fn(),
  updateMe: vi.fn()
}))

describe('ProfileEditor', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    routerPush.mockClear()
  })

  it('shows admin account details and saves the display name', async () => {
    api.getAdminMe.mockResolvedValue({
      success: true,
      data: { id: 1, username: 'admin', displayName: 'System Admin' }
    })
    api.updateAdminMe.mockResolvedValue({ success: true })

    const wrapper = mount(ProfileEditor, {
      props: { mode: 'admin' }
    })
    await flushPromises()

    expect(wrapper.text()).toContain('管理员账号')
    expect(wrapper.find('[data-test="profile-username"]').element.value).toBe('admin')
    expect(wrapper.text()).toContain('审核与注销权限')

    await wrapper.find('[data-test="profile-display-name"]').setValue('Lead Admin')
    await wrapper.find('[data-test="profile-save"]').trigger('click')
    await flushPromises()

    expect(api.updateAdminMe).toHaveBeenCalledWith({ displayName: 'Lead Admin' })
    expect(wrapper.text()).toContain('已保存')
  })

  it('updates the user password from the profile page', async () => {
    api.getMe.mockResolvedValue({
      success: true,
      data: { id: 2, username: 'alice', displayName: 'Alice', gender: '', birthday: '', age: null }
    })
    api.updatePassword.mockResolvedValue({ success: true })
    api.logout.mockResolvedValue({ success: true })

    const wrapper = mount(ProfileEditor, {
      props: { mode: 'user' }
    })
    await flushPromises()

    expect(wrapper.text()).toContain('修改密码')

    await wrapper.find('[data-test="password-current"]').setValue('oldpass')
    await wrapper.find('[data-test="password-new"]').setValue('newpass')
    await wrapper.find('[data-test="password-confirm"]').setValue('newpass')
    await wrapper.find('[data-test="password-save"]').trigger('click')
    await flushPromises()

    expect(api.updatePassword).toHaveBeenCalledWith({
      currentPassword: 'oldpass',
      newPassword: 'newpass',
      confirmPassword: 'newpass'
    })
    expect(api.logout).toHaveBeenCalled()
    expect(routerPush).toHaveBeenCalledWith('/login')
  })

  it('updates the admin password from the admin profile page', async () => {
    api.getAdminMe.mockResolvedValue({
      success: true,
      data: { id: 1, username: 'admin', displayName: 'System Admin' }
    })
    api.updateAdminPassword.mockResolvedValue({ success: true })
    api.logout.mockResolvedValue({ success: true })

    const wrapper = mount(ProfileEditor, {
      props: { mode: 'admin' }
    })
    await flushPromises()

    await wrapper.find('[data-test="password-current"]').setValue('oldpass')
    await wrapper.find('[data-test="password-new"]').setValue('newpass')
    await wrapper.find('[data-test="password-confirm"]').setValue('newpass')
    await wrapper.find('[data-test="password-save"]').trigger('click')
    await flushPromises()

    expect(api.updateAdminPassword).toHaveBeenCalledWith({
      currentPassword: 'oldpass',
      newPassword: 'newpass',
      confirmPassword: 'newpass'
    })
    expect(api.logout).toHaveBeenCalled()
    expect(routerPush).toHaveBeenCalledWith('/login')
  })
})

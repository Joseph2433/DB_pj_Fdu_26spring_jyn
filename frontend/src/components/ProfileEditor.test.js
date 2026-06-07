import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import ProfileEditor from './ProfileEditor.vue'
import * as api from '../api/client'

vi.mock('../api/client', () => ({
  getAdminMe: vi.fn(),
  getMe: vi.fn(),
  updateAdminMe: vi.fn(),
  updateMe: vi.fn()
}))

describe('ProfileEditor', () => {
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
})

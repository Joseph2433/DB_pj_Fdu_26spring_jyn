import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import ReviewList from './ReviewList.vue'
import * as api from '../api/client'

vi.mock('../api/client', () => ({
  adminDeletePost: vi.fn(),
  adminDeleteUser: vi.fn(),
  fetchAdminAuditLogs: vi.fn(),
  fetchAdminPosts: vi.fn()
}))

describe('ReviewList', () => {
  it('shows post author username in the review queue', async () => {
    api.fetchAdminPosts.mockResolvedValue({
      success: true,
      data: [
        {
          postId: 8,
          authorId: 1,
          authorUsername: 'alice',
          content: 'Need review',
          status: 'VISIBLE',
          lastUpdatedAt: '2026-06-07T12:00:00',
          commentCount: 1,
          comments: [
            { id: 3, authorId: 2, authorUsername: 'bob', content: 'Looks good', createdAt: '2026-06-07T12:10:00' },
            { id: 4, authorId: 4, authorUsername: 'carol', content: 'Please review this comment', createdAt: '2026-06-07T12:12:00' }
          ]
        }
      ]
    })
    api.adminDeletePost.mockResolvedValue({ success: true })

    const wrapper = mount(ReviewList)
    await flushPromises()

    expect(wrapper.text()).toContain('alice')
    expect(wrapper.text()).not.toContain('用户 #')
    expect(wrapper.text()).not.toContain('选择用户')

    await wrapper.find('[data-test="review-mode-card"]').trigger('click')

    expect(wrapper.find('.post-card').exists()).toBe(true)
    expect(wrapper.find('[data-test="review-comments-8"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Looks good')
    expect(wrapper.text()).toContain('Please review this comment')
    expect(wrapper.find('[data-test="comment-input-8"]').exists()).toBe(false)

    await wrapper.find('[data-test="admin-delete-post-8"]').trigger('click')
    await flushPromises()

    expect(api.adminDeletePost).toHaveBeenCalledWith(8)
  })

  it('deletes a user by account name instead of numeric id', async () => {
    api.fetchAdminPosts.mockResolvedValue({ success: true, data: [] })
    api.adminDeleteUser.mockResolvedValue({ success: true })

    const wrapper = mount(ReviewList)
    await flushPromises()

    expect(wrapper.text()).not.toContain('用户 ID')

    const accountInput = wrapper.find('[data-test="delete-user-account"]')
    expect(accountInput.exists()).toBe(true)
    expect(accountInput.attributes('type')).toBe('text')
    expect(accountInput.attributes('placeholder')).toBe('用户账号')

    await accountInput.setValue('bob')
    await wrapper.find('[data-test="delete-user-submit"]').trigger('click')
    await flushPromises()

    expect(api.adminDeleteUser).toHaveBeenCalledWith('bob')
  })

  it('opens an audit log dialog from the review toolbar', async () => {
    api.fetchAdminPosts.mockResolvedValue({ success: true, data: [] })
    api.fetchAdminAuditLogs.mockResolvedValue({
      success: true,
      data: [
        {
          id: 5,
          postId: 8,
          adminId: 99,
          adminUsername: 'admin',
          action: 'ADMIN_DELETE_POST',
          reason: 'moderated',
          createdAt: '2026-06-07T12:30:00'
        }
      ]
    })

    const wrapper = mount(ReviewList)
    await flushPromises()

    await wrapper.find('[data-test="audit-log-open"]').trigger('click')
    await flushPromises()

    expect(api.fetchAdminAuditLogs).toHaveBeenCalled()
    expect(wrapper.find('[data-test="audit-log-dialog"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('ADMIN_DELETE_POST')
    expect(wrapper.text()).toContain('moderated')
    expect(wrapper.text()).toContain('admin')
  })
})

import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import UserFeedView from './UserFeedView.vue'
import * as api from '../api/client'

vi.mock('../api/client', () => ({
  addComment: vi.fn(),
  deleteComment: vi.fn(),
  fetchFeed: vi.fn(),
  getMe: vi.fn()
}))

describe('UserFeedView', () => {
  it('searches feed and submits a comment without showing a clear button', async () => {
    const postResult = {
      success: true,
      data: [
        {
          id: 7,
          authorId: 2,
          authorUsername: 'bob',
          content: '数据库实验记录',
          lastUpdatedAt: '2026-06-07T10:00:00',
          comments: [{ id: 3, authorId: 1, authorUsername: 'alice', content: '关键词命中', createdAt: '2026-06-07T10:10:00' }]
        }
      ]
    }
    api.fetchFeed
      .mockResolvedValueOnce(postResult)
      .mockResolvedValueOnce(postResult)
      .mockResolvedValueOnce(postResult)
      .mockResolvedValueOnce(postResult)
    api.getMe.mockResolvedValue({ success: true, data: { id: 1 } })
    api.addComment.mockResolvedValue({ success: true })
    api.deleteComment.mockResolvedValue({ success: true })

    const wrapper = mount(UserFeedView)
    await flushPromises()

    await wrapper.find('[data-test="feed-keyword"]').setValue('数据库')
    await wrapper.find('[data-test="feed-search"]').trigger('submit')
    await flushPromises()
    expect(api.fetchFeed).toHaveBeenLastCalledWith('数据库')

    await wrapper.find('[data-test="comment-input-7"]').setValue('收到')
    await wrapper.find('[data-test="comment-form-7"]').trigger('submit')
    await flushPromises()
    expect(api.addComment).toHaveBeenCalledWith(7, '收到')

    await wrapper.find('[data-test="delete-comment-3"]').trigger('click')
    await flushPromises()

    expect(api.deleteComment).toHaveBeenCalledWith(7, 3)

    expect(wrapper.text()).toContain('bob')
    expect(wrapper.text()).toContain('alice')
    expect(wrapper.text()).not.toContain('用户 #')
    expect(wrapper.find('[data-test="feed-clear"]').exists()).toBe(false)
  })
})

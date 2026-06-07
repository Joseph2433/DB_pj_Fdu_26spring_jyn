import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import UserFeedView from './UserFeedView.vue'
import * as api from '../api/client'

vi.mock('../api/client', () => ({
  addComment: vi.fn(),
  fetchFeed: vi.fn()
}))

describe('UserFeedView', () => {
  it('searches feed, clears keyword, and submits a comment', async () => {
    const postResult = {
      success: true,
      data: [
        {
          id: 7,
          authorId: 2,
          content: '数据库实验记录',
          lastUpdatedAt: '2026-06-07T10:00:00',
          comments: [{ id: 3, authorId: 1, content: '关键词命中', createdAt: '2026-06-07T10:10:00' }]
        }
      ]
    }
    api.fetchFeed
      .mockResolvedValueOnce(postResult)
      .mockResolvedValueOnce(postResult)
      .mockResolvedValueOnce(postResult)
      .mockResolvedValueOnce({ success: true, data: [] })
    api.addComment.mockResolvedValue({ success: true })

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

    await wrapper.find('[data-test="feed-clear"]').trigger('click')
    await flushPromises()
    expect(api.fetchFeed).toHaveBeenLastCalledWith('')
  })
})

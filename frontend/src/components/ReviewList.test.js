import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import ReviewList from './ReviewList.vue'
import * as api from '../api/client'

vi.mock('../api/client', () => ({
  adminDeletePost: vi.fn(),
  adminDeleteUser: vi.fn(),
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
          commentCount: 0
        }
      ]
    })

    const wrapper = mount(ReviewList)
    await flushPromises()

    expect(wrapper.text()).toContain('alice')
    expect(wrapper.text()).not.toContain('用户 #')
  })
})

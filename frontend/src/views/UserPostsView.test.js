import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import UserPostsView from './UserPostsView.vue'
import * as api from '../api/client'

vi.mock('../api/client', () => ({
  deletePost: vi.fn(),
  fetchMyPosts: vi.fn(),
  updatePost: vi.fn()
}))

describe('UserPostsView', () => {
  it('shows comments for each post', async () => {
    api.fetchMyPosts.mockResolvedValue({
      success: true,
      data: [
        {
          id: 12,
          content: 'My post',
          lastUpdatedAt: '2026-06-07T12:00:00',
          comments: [
            { id: 3, authorId: 2, content: 'First comment', createdAt: '2026-06-07T12:10:00' }
          ]
        }
      ]
    })

    const wrapper = mount(UserPostsView)
    await flushPromises()

    expect(wrapper.text()).toContain('First comment')
  })
})

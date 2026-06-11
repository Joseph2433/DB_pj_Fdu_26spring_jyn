import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import UserFriendPostsView from './UserFriendPostsView.vue'
import * as api from '../api/client'

vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: { friendId: '2' },
    query: { name: 'Bob', username: 'bob' }
  })
}))

vi.mock('../api/client', () => ({
  addComment: vi.fn(),
  deleteComment: vi.fn(),
  fetchFriendPosts: vi.fn(),
  getMe: vi.fn()
}))

describe('UserFriendPostsView', () => {
  it('loads one friend posts and submits comments', async () => {
    api.fetchFriendPosts.mockResolvedValue({
      success: true,
      data: [
        {
          id: 21,
          authorId: 2,
          authorUsername: 'bob',
          content: 'Bob timeline',
          lastUpdatedAt: '2026-06-07T12:00:00',
          comments: [{ id: 4, authorId: 1, authorUsername: 'alice', content: 'Nice', createdAt: '2026-06-07T12:10:00' }]
        }
      ]
    })
    api.getMe.mockResolvedValue({ success: true, data: { id: 1 } })
    api.addComment.mockResolvedValue({ success: true })
    api.deleteComment.mockResolvedValue({ success: true })

    const wrapper = mount(UserFriendPostsView, {
      global: {
        stubs: {
          RouterLink: {
            props: ['to'],
            template: '<a :href="to"><slot /></a>'
          }
        }
      }
    })
    await flushPromises()

    expect(api.fetchFriendPosts).toHaveBeenCalledWith(2)
    expect(wrapper.text()).toContain('Bob')
    expect(wrapper.text()).toContain('bob')
    expect(wrapper.text()).toContain('alice')
    expect(wrapper.text()).toContain('Bob timeline')
    expect(wrapper.text()).not.toContain('用户 #')

    await wrapper.find('[data-test="comment-input-21"]').setValue('Great')
    await wrapper.find('[data-test="comment-form-21"]').trigger('submit')
    await flushPromises()

    expect(api.addComment).toHaveBeenCalledWith(21, 'Great')

    await wrapper.find('[data-test="delete-comment-4"]').trigger('click')
    await flushPromises()

    expect(api.deleteComment).toHaveBeenCalledWith(21, 4)
    expect(api.fetchFriendPosts).toHaveBeenCalledTimes(3)
  })
})

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
  fetchFriendPosts: vi.fn()
}))

describe('UserFriendPostsView', () => {
  it('loads one friend posts and submits comments', async () => {
    api.fetchFriendPosts.mockResolvedValue({
      success: true,
      data: [
        {
          id: 21,
          authorId: 2,
          content: 'Bob timeline',
          lastUpdatedAt: '2026-06-07T12:00:00',
          comments: [{ id: 4, authorId: 1, content: 'Nice', createdAt: '2026-06-07T12:10:00' }]
        }
      ]
    })
    api.addComment.mockResolvedValue({ success: true })

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
    expect(wrapper.text()).toContain('Bob timeline')

    await wrapper.find('[data-test="comment-input-21"]').setValue('Great')
    await wrapper.find('[data-test="comment-form-21"]').trigger('submit')
    await flushPromises()

    expect(api.addComment).toHaveBeenCalledWith(21, 'Great')
    expect(api.fetchFriendPosts).toHaveBeenCalledTimes(2)
  })
})

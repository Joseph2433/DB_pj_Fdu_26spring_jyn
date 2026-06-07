import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import UserPostsView from './UserPostsView.vue'
import * as api from '../api/client'

vi.mock('../api/client', () => ({
  addComment: vi.fn(),
  deletePost: vi.fn(),
  fetchFriends: vi.fn(),
  fetchGroups: vi.fn(),
  fetchMyPosts: vi.fn(),
  fetchPostVisibility: vi.fn(),
  updatePostVisibility: vi.fn(),
  updatePost: vi.fn()
}))

function mockMyPosts() {
  api.fetchMyPosts.mockResolvedValue({
    success: true,
    data: [
      {
        id: 12,
        authorId: 1,
        authorUsername: 'alice',
        content: 'My post',
        lastUpdatedAt: '2026-06-07T12:00:00',
        comments: [
          { id: 3, authorId: 2, authorUsername: 'bob', content: 'First comment', createdAt: '2026-06-07T12:10:00' }
        ]
      }
    ]
  })
}

describe('UserPostsView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockMyPosts()
    api.addComment.mockResolvedValue({ success: true })
    api.deletePost.mockResolvedValue({ success: true })
    api.fetchGroups.mockResolvedValue({
      success: true,
      data: [{ id: 7, userId: 1, name: 'Project Team' }]
    })
    api.fetchFriends.mockResolvedValue({
      success: true,
      data: [{ friendshipId: 1, friendId: 2, username: 'bob', displayName: 'Bob', groupId: 7, groupName: 'Project Team' }]
    })
    api.fetchPostVisibility.mockResolvedValue({ success: true, data: [] })
    api.updatePostVisibility.mockResolvedValue({ success: true })
    api.updatePost.mockResolvedValue({ success: true })
  })

  it('shows my posts with feed-card comments and can add a comment', async () => {
    const wrapper = mount(UserPostsView)
    await flushPromises()

    expect(wrapper.find('.post-card').exists()).toBe(true)
    expect(wrapper.text()).toContain('First comment')
    expect(wrapper.text()).toContain('alice')
    expect(wrapper.text()).toContain('bob')
    expect(wrapper.text()).not.toContain('用户 #')

    await wrapper.find('[data-test="comment-input-12"]').setValue('My own follow-up')
    await wrapper.find('[data-test="comment-form-12"]').trigger('submit')
    await flushPromises()

    expect(api.addComment).toHaveBeenCalledWith(12, 'My own follow-up')
    expect(api.fetchMyPosts).toHaveBeenCalledTimes(2)
  })

  it('keeps edit and delete actions for my posts', async () => {
    const wrapper = mount(UserPostsView)
    await flushPromises()

    await wrapper.find('[data-test="edit-post-12"]').trigger('click')
    await wrapper.find('[data-test="edit-content-12"]').setValue('Updated post')
    await wrapper.find('[data-test="edit-form-12"]').trigger('submit')
    await flushPromises()

    expect(api.updatePost).toHaveBeenCalledWith(12, 'Updated post')

    await wrapper.find('[data-test="delete-post-12"]').trigger('click')
    await flushPromises()

    expect(api.deletePost).toHaveBeenCalledWith(12)
  })

  it('sets group allow visibility for my posts', async () => {
    const wrapper = mount(UserPostsView)
    await flushPromises()

    await wrapper.find('[data-test="allow-post-12"]').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('设置可见范围')
    expect(api.fetchPostVisibility).toHaveBeenCalledWith(12)

    await wrapper.find('[data-test="visibility-target-GROUP-7"]').setValue(true)
    await wrapper.find('[data-test="save-visibility"]').trigger('click')
    await flushPromises()

    expect(api.updatePostVisibility).toHaveBeenCalledWith(12, {
      ruleType: 'ALLOW',
      targetType: 'GROUP',
      targetIds: [7]
    })
  })

  it('disables the opposite visibility mode when a post already has one mode', async () => {
    api.fetchPostVisibility.mockResolvedValue({
      success: true,
      data: [
        { id: 1, postId: 12, ruleType: 'ALLOW', targetType: 'GROUP', targetId: 7, targetName: 'Project Team' }
      ]
    })
    const wrapper = mount(UserPostsView)
    await flushPromises()

    expect(wrapper.find('[data-test="allow-post-12"]').element.disabled).toBe(false)
    expect(wrapper.find('[data-test="deny-post-12"]').element.disabled).toBe(true)
  })
})

import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import UserFriendsView from './UserFriendsView.vue'
import * as api from '../api/client'

vi.mock('../api/client', () => ({
  addFriend: vi.fn(),
  createGroup: vi.fn(),
  deleteFriend: vi.fn(),
  fetchFriends: vi.fn(),
  fetchGroups: vi.fn(),
  moveFriend: vi.fn(),
  searchUsers: vi.fn()
}))

describe('UserFriendsView', () => {
  it('creates a group, searches users, adds a friend, and moves a friend', async () => {
    api.fetchGroups.mockResolvedValue({
      success: true,
      data: [{ id: 5, userId: 1, name: '项目组' }]
    })
    api.fetchFriends.mockResolvedValue({
      success: true,
      data: [{ friendshipId: 9, friendId: 2, username: 'bob', displayName: 'Bob', groupId: null, groupName: null }]
    })
    api.createGroup.mockResolvedValue({ success: true })
    api.searchUsers.mockResolvedValue({
      success: true,
      data: [{ id: 3, username: 'cathy', displayName: 'Cathy' }]
    })
    api.addFriend.mockResolvedValue({ success: true })
    api.moveFriend.mockResolvedValue({ success: true })
    api.deleteFriend.mockResolvedValue({ success: true })

    const wrapper = mount(UserFriendsView)
    await flushPromises()

    await wrapper.find('[data-test="group-name"]').setValue('实验搭子')
    await wrapper.find('[data-test="group-form"]').trigger('submit')
    await flushPromises()
    expect(api.createGroup).toHaveBeenCalledWith('实验搭子')

    await wrapper.find('[data-test="user-keyword"]').setValue('cathy')
    await wrapper.find('[data-test="user-search-form"]').trigger('submit')
    await flushPromises()
    expect(api.searchUsers).toHaveBeenCalledWith('cathy')

    await wrapper.find('[data-test="add-friend-3"]').trigger('click')
    await flushPromises()
    expect(api.addFriend).toHaveBeenCalledWith(3, null)

    await wrapper.find('[data-test="friend-group-2"]').setValue('5')
    await flushPromises()
    expect(api.moveFriend).toHaveBeenCalledWith(2, 5)
  })
})

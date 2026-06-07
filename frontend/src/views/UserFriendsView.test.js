import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import UserFriendsView from './UserFriendsView.vue'
import * as api from '../api/client'

vi.mock('../api/client', () => ({
  addFriend: vi.fn(),
  createGroup: vi.fn(),
  deleteFriend: vi.fn(),
  deleteGroup: vi.fn(),
  fetchFriends: vi.fn(),
  fetchGroups: vi.fn(),
  moveFriend: vi.fn(),
  searchUsers: vi.fn()
}))

function mockDirectoryData() {
  api.fetchGroups.mockResolvedValue({
    success: true,
    data: [
      { id: 5, userId: 1, name: 'Project' },
      { id: 6, userId: 1, name: 'Classmates' }
    ]
  })
  api.fetchFriends.mockResolvedValue({
    success: true,
    data: [
      { friendshipId: 9, friendId: 2, username: 'bob', displayName: 'Bob', groupId: 5, groupName: 'Project' },
      { friendshipId: 10, friendId: 4, username: 'dana', displayName: 'Dana', groupId: null, groupName: null },
      { friendshipId: 11, friendId: 7, username: 'evan', displayName: 'Evan', groupId: 6, groupName: 'Classmates' }
    ]
  })
}

describe('UserFriendsView', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockDirectoryData()
    api.createGroup.mockResolvedValue({ success: true })
    api.searchUsers.mockResolvedValue({
      success: true,
      data: [{ id: 3, username: 'cathy', displayName: 'Cathy' }]
    })
    api.addFriend.mockResolvedValue({ success: true })
    api.moveFriend.mockResolvedValue({ success: true })
    api.deleteFriend.mockResolvedValue({ success: true })
    api.deleteGroup.mockResolvedValue({ success: true })
  })

  it('creates a group, searches users, adds a friend, and moves a friend', async () => {
    const wrapper = mount(UserFriendsView)
    await flushPromises()

    await wrapper.find('[data-test="group-name"]').setValue('Lab partners')
    await wrapper.find('[data-test="group-form"]').trigger('submit')
    await flushPromises()
    expect(api.createGroup).toHaveBeenCalledWith('Lab partners')

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

  it('deletes a group and refreshes the friend directory', async () => {
    const wrapper = mount(UserFriendsView)
    await flushPromises()

    await wrapper.find('[data-test="delete-group-5"]').trigger('click')
    await flushPromises()

    expect(api.deleteGroup).toHaveBeenCalledWith(5)
    expect(api.fetchGroups).toHaveBeenCalledTimes(2)
    expect(api.fetchFriends).toHaveBeenCalledTimes(2)
  })

  it('filters friends by the selected group', async () => {
    const wrapper = mount(UserFriendsView)
    await flushPromises()

    await wrapper.find('[data-test="friend-group-filter"]').setValue('5')
    await flushPromises()
    expect(wrapper.text()).toContain('Bob')
    expect(wrapper.text()).not.toContain('Dana')
    expect(wrapper.text()).not.toContain('Evan')

    await wrapper.find('[data-test="friend-group-filter"]').setValue('__ungrouped__')
    await flushPromises()
    expect(wrapper.text()).not.toContain('Bob')
    expect(wrapper.text()).toContain('Dana')
    expect(wrapper.text()).not.toContain('Evan')
  })
})

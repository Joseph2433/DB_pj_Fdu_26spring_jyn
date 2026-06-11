import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import PostCard from './PostCard.vue'

describe('PostCard', () => {
  it('prefills the comment composer when replying to a comment', async () => {
    const wrapper = mount(PostCard, {
      props: {
        post: {
          id: 12,
          authorId: 1,
          authorUsername: 'alice',
          content: 'My post',
          lastUpdatedAt: '2026-06-07T12:00:00',
          comments: [
            { id: 3, authorId: 2, authorUsername: 'bob', content: 'First comment', createdAt: '2026-06-07T12:10:00' }
          ]
        }
      }
    })

    expect(wrapper.text()).toContain('alice')
    expect(wrapper.text()).toContain('bob')
    expect(wrapper.text()).not.toContain('用户 #')

    await wrapper.find('[data-test="reply-comment-3"]').trigger('click')

    const input = wrapper.find('[data-test="comment-input-12"]')
    expect(input.element.value).toContain('回复 bob：')

    const submittedText = `${input.element.value} thanks`.trim()
    await input.setValue(submittedText)
    await wrapper.find('[data-test="comment-form-12"]').trigger('submit')

    expect(wrapper.emitted('comment')[0]).toEqual([12, submittedText])
  })

  it('shows a delete comment button before reply for manageable comments', async () => {
    const wrapper = mount(PostCard, {
      props: {
        currentUserId: 1,
        post: {
          id: 12,
          authorId: 2,
          authorUsername: 'bob',
          content: 'Friend post',
          lastUpdatedAt: '2026-06-07T12:00:00',
          comments: [
            { id: 3, authorId: 1, authorUsername: 'alice', content: 'My comment', createdAt: '2026-06-07T12:10:00' }
          ]
        }
      }
    })

    const actionButtons = wrapper.findAll('[data-test="comment-actions-3"] button')
    expect(actionButtons.map((button) => button.attributes('data-test'))).toEqual([
      'delete-comment-3',
      'reply-comment-3'
    ])

    await wrapper.find('[data-test="delete-comment-3"]').trigger('click')

    expect(wrapper.emitted('delete-comment')[0]).toEqual([12, 3])
  })
})

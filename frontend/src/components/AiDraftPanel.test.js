import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import AiDraftPanel from './AiDraftPanel.vue'

describe('AiDraftPanel', () => {
  it('emits selected draft when user chooses a generated draft', async () => {
    const generate = vi.fn().mockResolvedValue({
      success: true,
      data: { drafts: ['今天的数据库实验很顺利'] }
    })
    const wrapper = mount(AiDraftPanel, {
      props: { generate }
    })

    await wrapper.find('input[name="topic"]').setValue('数据库实验')
    await wrapper.find('button[data-test="generate"]').trigger('click')
    await flushPromises()
    await wrapper.find('button[data-test="use-draft"]').trigger('click')

    expect(wrapper.emitted('select-draft')[0]).toEqual(['今天的数据库实验很顺利'])
  })
})

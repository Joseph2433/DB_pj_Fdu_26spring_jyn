import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import LoginView from './LoginView.vue'

vi.mock('vue-router', () => ({
  useRoute: () => ({ query: {} }),
  useRouter: () => ({ push: vi.fn() })
}))

describe('LoginView', () => {
  it('starts with blank credentials and no today activity block', () => {
    const wrapper = mount(LoginView)
    const inputs = wrapper.findAll('input')

    expect(inputs[0].element.value).toBe('')
    expect(inputs[1].element.value).toBe('')
    expect(wrapper.text()).not.toContain('今日动态')
    expect(wrapper.text()).toContain('演示流程')
  })
})

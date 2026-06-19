import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import Hearts from './Hearts'

describe('Hearts', () => {
  it('renders total slots with the filled ones reflecting remaining lives', () => {
    render(<Hearts remaining={3} total={6} />)
    const region = screen.getByRole('status')
    expect(region).toHaveAccessibleName('3 of 6 lives remaining')
    expect(region.querySelectorAll('[aria-hidden="true"]')).toHaveLength(6)
    expect(region.querySelectorAll('[data-filled="true"]')).toHaveLength(3)
    expect(region.querySelectorAll('[data-filled="false"]')).toHaveLength(3)
  })

  it('shows all slots dimmed when no lives remain', () => {
    render(<Hearts remaining={0} total={4} />)
    const region = screen.getByRole('status')
    expect(region).toHaveAccessibleName('0 of 4 lives remaining')
    expect(region.querySelectorAll('[data-filled="true"]')).toHaveLength(0)
    expect(region.querySelectorAll('[data-filled="false"]')).toHaveLength(4)
  })
})

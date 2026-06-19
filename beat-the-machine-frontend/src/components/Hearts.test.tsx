import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import Hearts from './Hearts'

describe('Hearts', () => {
  it('renders one heart glyph per remaining life with an accurate label', () => {
    render(<Hearts remaining={3} />)
    const region = screen.getByRole('status')
    expect(region).toHaveAccessibleName('3 lives remaining')
    expect(region.querySelectorAll('[aria-hidden="true"]')).toHaveLength(3)
  })

  it('renders no heart glyphs when no lives remain', () => {
    render(<Hearts remaining={0} />)
    expect(screen.getByRole('status')).toHaveAccessibleName('0 lives remaining')
  })
})

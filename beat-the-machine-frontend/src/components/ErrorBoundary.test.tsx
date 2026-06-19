import { render, screen } from '@testing-library/react'
import { afterAll, beforeAll, describe, expect, it, vi } from 'vitest'
import ErrorBoundary from './ErrorBoundary'

function Boom(): never {
  throw new Error('kaboom')
}

describe('ErrorBoundary', () => {
  let spy: ReturnType<typeof vi.spyOn>
  beforeAll(() => {
    spy = vi.spyOn(console, 'error').mockImplementation(() => {})
  })
  afterAll(() => spy.mockRestore())

  it('renders children when there is no error', () => {
    render(
      <ErrorBoundary>
        <div>safe content</div>
      </ErrorBoundary>,
    )
    expect(screen.getByText('safe content')).toBeInTheDocument()
  })

  it('renders a fallback with a Back to start link when a child throws', () => {
    render(
      <ErrorBoundary>
        <Boom />
      </ErrorBoundary>,
    )
    expect(screen.getByText(/something went wrong/i)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /back to start/i })).toHaveAttribute('href', '/app/')
  })
})

import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { QueryClientProvider } from '@tanstack/react-query'
import { afterEach, expect, it, vi } from 'vitest'
import { QueryClient } from '@tanstack/react-query'
import LandingScreen from './LandingScreen'
import * as api from '../api/challenges'

const renderLanding = () =>
  render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter>
        <LandingScreen />
      </MemoryRouter>
    </QueryClientProvider>,
  )

afterEach(() => vi.restoreAllMocks())

it('shows the three difficulties', () => {
  renderLanding()
  expect(screen.getByRole('button', { name: /easy/i })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: /medium/i })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: /hard/i })).toBeInTheDocument()
})

it('starts a challenge and stores its id when Start is pressed', async () => {
  const spy = vi
    .spyOn(api, 'startChallenge')
    .mockResolvedValue({ id: 'new-id', maskedPrompt: [], livesRemaining: 5, status: 'IN_PROGRESS', picture: { status: 'PENDING', url: null } } as never)
  renderLanding()
  await userEvent.click(screen.getByRole('button', { name: /start/i }))
  expect(spy).toHaveBeenCalledWith('MEDIUM')
  expect(localStorage.getItem('btm.activeChallengeId')).toBe('new-id')
})

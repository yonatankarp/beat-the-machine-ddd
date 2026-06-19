import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { afterEach, expect, it, vi } from 'vitest'
import { ChallengeStatus } from '../generated'
import { makeChallenge } from '../test/fixtures'
import LandingScreen from './LandingScreen'
import * as api from '../api/challenges'

afterEach(() => {
  vi.restoreAllMocks()
  localStorage.clear()
})

const renderLanding = () =>
  render(
    <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/" element={<LandingScreen />} />
          <Route path="/play/:id" element={<div>play page</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )

it('shows the three difficulties', () => {
  renderLanding()
  expect(screen.getByRole('button', { name: /easy/i })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: /medium/i })).toBeInTheDocument()
  expect(screen.getByRole('button', { name: /hard/i })).toBeInTheDocument()
})

it('starts a challenge and stores its id when Start is pressed', async () => {
  const spy = vi
    .spyOn(api, 'startChallenge')
    .mockResolvedValue(makeChallenge({ id: 'new-id' }))
  renderLanding()
  await userEvent.click(screen.getByRole('button', { name: /start/i }))
  expect(spy).toHaveBeenCalledWith('MEDIUM')
  expect(localStorage.getItem('btm.activeChallengeId')).toBe('new-id')
})

it('selecting Hard then Start starts a HARD challenge', async () => {
  const spy = vi
    .spyOn(api, 'startChallenge')
    .mockResolvedValue(makeChallenge({ id: 'hard-id' }))
  renderLanding()
  await userEvent.click(screen.getByRole('button', { name: /hard/i }))
  await userEvent.click(screen.getByRole('button', { name: /start/i }))
  expect(spy).toHaveBeenCalledWith('HARD')
})

it('a failed start shows an alert and writes nothing to storage', async () => {
  vi.spyOn(api, 'startChallenge').mockRejectedValue({ status: 500, message: 'boom' })
  renderLanding()
  await userEvent.click(screen.getByRole('button', { name: /start/i }))
  expect(await screen.findByRole('alert')).toBeInTheDocument()
  expect(localStorage.getItem('btm.activeChallengeId')).toBeNull()
})

it('offers Resume when a stored IN_PROGRESS challenge exists and navigates to it', async () => {
  localStorage.setItem('btm.activeChallengeId', 'stored-1')
  vi.spyOn(api, 'getChallenge').mockResolvedValue(
    makeChallenge({ id: 'stored-1', status: ChallengeStatus.InProgress }),
  )
  renderLanding()
  const resume = await screen.findByRole('button', { name: /resume/i })
  await userEvent.click(resume)
  expect(await screen.findByText('play page')).toBeInTheDocument()
})

it('clears a stored id and hides Resume when the stored challenge is terminal', async () => {
  localStorage.setItem('btm.activeChallengeId', 'stored-2')
  vi.spyOn(api, 'getChallenge').mockResolvedValue(
    makeChallenge({ id: 'stored-2', status: ChallengeStatus.Lost }),
  )
  renderLanding()
  await screen.findByRole('button', { name: /start/i })
  await waitFor(() => expect(localStorage.getItem('btm.activeChallengeId')).toBeNull())
  expect(screen.queryByRole('button', { name: /resume/i })).toBeNull()
})

it('clears a stored id and hides Resume when the stored challenge errors', async () => {
  localStorage.setItem('btm.activeChallengeId', 'stored-3')
  vi.spyOn(api, 'getChallenge').mockRejectedValue({ status: 404, message: 'gone' })
  renderLanding()
  await screen.findByRole('button', { name: /start/i })
  await waitFor(() => expect(localStorage.getItem('btm.activeChallengeId')).toBeNull())
  expect(screen.queryByRole('button', { name: /resume/i })).toBeNull()
})

it('How to play toggle exposes aria-expanded and controls a panel', async () => {
  renderLanding()
  const toggle = screen.getByRole('button', { name: /how to play/i })
  expect(toggle).toHaveAttribute('aria-expanded', 'false')
  const panelId = toggle.getAttribute('aria-controls')
  expect(panelId).toBeTruthy()
  await userEvent.click(toggle)
  expect(toggle).toHaveAttribute('aria-expanded', 'true')
  expect(document.getElementById(panelId!)).toBeInTheDocument()
})

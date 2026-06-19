import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { afterEach, expect, it, vi } from 'vitest'
import { ChallengeStatus, PictureStatus } from '../generated'
import { makeChallenge } from '../test/fixtures'
import ResultScreen from './ResultScreen'
import * as api from '../api/challenges'

afterEach(() => {
  vi.restoreAllMocks()
  localStorage.clear()
})

const renderResult = (id = 'r1') =>
  render(
    <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
      <MemoryRouter initialEntries={[`/result/${id}`]}>
        <Routes>
          <Route path="/result/:id" element={<ResultScreen />} />
          <Route path="/" element={<div>landing page</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )

const beaten = makeChallenge({
  id: 'r1',
  maskedPrompt: [
    { revealed: true, word: 'fear', length: 4 },
    { revealed: true, word: 'dark', length: 4 },
  ],
  livesRemaining: 3,
  status: ChallengeStatus.Beaten,
})

it('shows the win outcome, the revealed prompt, and resets on Play Again', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(beaten)
  localStorage.setItem('btm.activeChallengeId', 'r1')

  renderResult()

  expect(await screen.findByText(/you beat the machine/i)).toBeInTheDocument()
  expect(screen.getByText(/fear dark/i)).toBeInTheDocument()
  await userEvent.click(screen.getByRole('button', { name: /play again/i }))
  expect(await screen.findByText('landing page')).toBeInTheDocument()
  expect(localStorage.getItem('btm.activeChallengeId')).toBeNull()
})

it('shows the lose outcome and full prompt when the machine wins', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(
    makeChallenge({
      id: 'r1',
      maskedPrompt: [
        { revealed: true, word: 'lost', length: 4 },
        { revealed: true, word: 'cause', length: 5 },
      ],
      livesRemaining: 0,
      status: ChallengeStatus.Lost,
    }),
  )

  renderResult()
  expect(await screen.findByText(/the machine won this time/i)).toBeInTheDocument()
  expect(screen.getByText(/lost cause/i)).toBeInTheDocument()
})

it('renders no img when the picture is not READY', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(
    makeChallenge({
      id: 'r1',
      maskedPrompt: [{ revealed: true, word: 'gone', length: 4 }],
      status: ChallengeStatus.Lost,
      picture: { status: PictureStatus.Failed, url: null },
    }),
  )

  renderResult()
  expect(await screen.findByText(/the machine won this time/i)).toBeInTheDocument()
  expect(screen.queryByRole('img')).toBeNull()
})

it('shows an error state with a Back to start button when getChallenge fails', async () => {
  vi.spyOn(api, 'getChallenge').mockRejectedValue({ status: 500, message: 'boom' })
  renderResult()
  expect(await screen.findByRole('button', { name: /back to start/i })).toBeInTheDocument()
  expect(screen.queryByText('Loading…')).toBeNull()
})

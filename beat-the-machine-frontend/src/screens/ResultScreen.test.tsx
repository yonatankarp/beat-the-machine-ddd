import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { afterEach, expect, it, vi } from 'vitest'
import ResultScreen from './ResultScreen'
import * as api from '../api/challenges'

afterEach(() => vi.restoreAllMocks())
afterEach(() => localStorage.clear())

const beaten = {
  id: 'r1',
  maskedPrompt: [
    { revealed: true, word: 'fear', length: 4 },
    { revealed: true, word: 'dark', length: 4 },
  ],
  livesRemaining: 3,
  status: 'BEATEN',
  picture: { status: 'READY', url: 'https://example.com/x.png' },
} as never

it('shows the win outcome, the revealed prompt, and resets on Play Again', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(beaten)
  localStorage.setItem('btm.activeChallengeId', 'r1')

  render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter initialEntries={['/result/r1']}>
        <Routes>
          <Route path="/result/:id" element={<ResultScreen />} />
          <Route path="/" element={<div>landing page</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )

  expect(await screen.findByText(/you beat the machine/i)).toBeInTheDocument()
  expect(screen.getByText(/fear dark/i)).toBeInTheDocument()
  await userEvent.click(screen.getByRole('button', { name: /play again/i }))
  expect(await screen.findByText('landing page')).toBeInTheDocument()
  expect(localStorage.getItem('btm.activeChallengeId')).toBeNull()
})

import { render, screen } from '@testing-library/react'
import { expect, it } from 'vitest'
import MaskedPrompt from '../components/MaskedPrompt'

it('renders revealed words and blanks sized by length', () => {
  render(
    <MaskedPrompt
      tokens={[
        { revealed: true, word: 'fear', length: 4 },
        { revealed: false, word: null, length: 2 },
        { revealed: false, word: null, length: 3 },
      ]}
    />,
  )
  expect(screen.getByText('fear')).toBeInTheDocument()
  // Two hidden words: one with 2 blanks, one with 3 blanks => 5 blank cells total.
  expect(screen.getAllByTestId('blank')).toHaveLength(5)
})

import { render as renderScreen, screen as screen2 } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { afterEach, vi } from 'vitest'
import GameScreen from './GameScreen'
import * as api from '../api/challenges'

afterEach(() => {
  vi.restoreAllMocks()
  localStorage.clear()
})

const ready = {
  id: 'g1',
  maskedPrompt: [{ revealed: false, word: null, length: 4 }],
  livesRemaining: 5,
  status: 'IN_PROGRESS',
  picture: { status: 'READY', url: 'https://example.com/x.png' },
} as never

it('a 422 rejection shows an inline error without costing a life', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(ready)
  vi.spyOn(api, 'makeGuess').mockRejectedValue({ status: 422, message: 'invalid guess' })

  renderScreen(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter initialEntries={['/play/g1']}>
        <Routes>
          <Route path="/play/:id" element={<GameScreen />} />
          <Route path="/result/:id" element={<div>result page</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )

  await screen2.findByRole('img')
  await userEvent.type(screen2.getByRole('textbox'), 'bad')
  await userEvent.click(screen2.getByRole('button', { name: /guess/i }))

  expect(await screen2.findByRole('alert')).toBeInTheDocument()
  expect(screen2.getByLabelText('5 lives remaining')).toBeInTheDocument()
  expect(screen2.queryByText('result page')).not.toBeInTheDocument()
})

it('409 already-over navigates to result', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(ready)
  vi.spyOn(api, 'makeGuess').mockRejectedValue({ status: 409, message: 'challenge is already over' })

  renderScreen(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter initialEntries={['/play/g1']}>
        <Routes>
          <Route path="/play/:id" element={<GameScreen />} />
          <Route path="/result/:id" element={<div>result page</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )

  await screen2.findByRole('img')
  await userEvent.type(screen2.getByRole('textbox'), 'over')
  await userEvent.click(screen2.getByRole('button', { name: /guess/i }))
  expect(await screen2.findByText('result page')).toBeInTheDocument()
})

it('409 concurrent-modification retries silently then succeeds', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(ready)

  let resolveRetry!: (v: never) => void
  const retryPromise = new Promise<never>((res) => { resolveRetry = res })

  const makeGuessSpy = vi.spyOn(api, 'makeGuess')
    .mockRejectedValueOnce({ status: 409, message: 'challenge was modified concurrently; retry the operation' })
    .mockReturnValueOnce(retryPromise)

  renderScreen(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter initialEntries={['/play/g1']}>
        <Routes>
          <Route path="/play/:id" element={<GameScreen />} />
          <Route path="/result/:id" element={<div>result page</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )

  await screen2.findByRole('img')
  await userEvent.type(screen2.getByRole('textbox'), 'fear')
  await userEvent.click(screen2.getByRole('button', { name: /guess/i }))

  // Wait until both calls have been issued (first rejected, retry pending).
  await vi.waitFor(() => expect(makeGuessSpy).toHaveBeenCalledTimes(2))

  // While the retry is still in-flight, the result page must NOT be shown.
  expect(screen2.queryByText('result page')).toBeNull()

  // Now resolve the retry with a terminal state.
  resolveRetry({
    ...(ready as object),
    maskedPrompt: [{ revealed: true, word: 'fear', length: 4 }],
    status: 'BEATEN',
  } as never)

  expect(await screen2.findByText('result page')).toBeInTheDocument()
  expect(makeGuessSpy).toHaveBeenNthCalledWith(1, 'g1', 'fear')
  expect(makeGuessSpy).toHaveBeenNthCalledWith(2, 'g1', 'fear')
})

it('unexpected status shows inline error with lives unchanged and no navigation', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(ready)
  vi.spyOn(api, 'makeGuess').mockRejectedValue({ status: 500, message: 'boom' })

  renderScreen(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter initialEntries={['/play/g1']}>
        <Routes>
          <Route path="/play/:id" element={<GameScreen />} />
          <Route path="/result/:id" element={<div>result page</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )

  await screen2.findByRole('img')
  await userEvent.type(screen2.getByRole('textbox'), 'bad')
  await userEvent.click(screen2.getByRole('button', { name: /guess/i }))

  expect(await screen2.findByRole('alert')).toBeInTheDocument()
  expect(screen2.getByLabelText('5 lives remaining')).toBeInTheDocument()
  expect(screen2.queryByText('result page')).not.toBeInTheDocument()
})

it('submits a guess and shows the updated state', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(ready)
  vi.spyOn(api, 'makeGuess').mockResolvedValue({
    ...(ready as object),
    maskedPrompt: [{ revealed: true, word: 'fear', length: 4 }],
    status: 'BEATEN',
  } as never)

  renderScreen(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter initialEntries={['/play/g1']}>
        <Routes>
          <Route path="/play/:id" element={<GameScreen />} />
          <Route path="/result/:id" element={<div>result page</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )

  await screen2.findByRole('img')
  await userEvent.type(screen2.getByRole('textbox'), 'fear')
  await userEvent.click(screen2.getByRole('button', { name: /guess/i }))
  expect(await screen2.findByText('result page')).toBeInTheDocument()
})

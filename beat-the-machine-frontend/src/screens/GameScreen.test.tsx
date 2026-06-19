import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { afterEach, expect, it, vi } from 'vitest'
import { ChallengeStatus, PictureStatus } from '../generated'
import { makeChallenge } from '../test/fixtures'
import GameScreen from './GameScreen'
import * as api from '../api/challenges'

afterEach(() => {
  vi.restoreAllMocks()
  localStorage.clear()
})

const renderGameScreen = (id = 'g1') =>
  render(
    <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
      <MemoryRouter initialEntries={[`/play/${id}`]}>
        <Routes>
          <Route path="/play/:id" element={<GameScreen />} />
          <Route path="/result/:id" element={<div>result page</div>} />
          <Route path="/" element={<div>landing page</div>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )

const guessWith = async (text: string) => {
  await userEvent.type(screen.getByRole('textbox'), text)
  await userEvent.click(screen.getByRole('button', { name: /^guess$/i }))
}

it('a 422 rejection shows an inline error without costing a life', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(makeChallenge())
  vi.spyOn(api, 'makeGuess').mockRejectedValue({ status: 422, message: 'invalid guess' })

  renderGameScreen()
  await screen.findByRole('img')
  await guessWith('bad')

  expect(await screen.findByRole('alert')).toHaveTextContent('That guess is not valid.')
  expect(screen.getByLabelText('5 lives remaining')).toBeInTheDocument()
  expect(screen.queryByText('result page')).not.toBeInTheDocument()
})

it('409 already-over navigates to result', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(makeChallenge())
  vi.spyOn(api, 'makeGuess').mockRejectedValue({ status: 409, message: 'challenge is already over' })

  renderGameScreen()
  await screen.findByRole('img')
  await guessWith('over')
  expect(await screen.findByText('result page')).toBeInTheDocument()
})

it('409 concurrent-modification retries silently then succeeds', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(makeChallenge())

  let resolveRetry!: (v: ReturnType<typeof makeChallenge>) => void
  const retryPromise = new Promise<ReturnType<typeof makeChallenge>>((res) => {
    resolveRetry = res
  })

  const makeGuessSpy = vi
    .spyOn(api, 'makeGuess')
    .mockRejectedValueOnce({ status: 409, message: 'challenge was modified concurrently; retry the operation' })
    .mockReturnValueOnce(retryPromise)

  renderGameScreen()
  await screen.findByRole('img')
  await guessWith('fear')

  await vi.waitFor(() => expect(makeGuessSpy).toHaveBeenCalledTimes(2))
  expect(screen.queryByText('result page')).toBeNull()

  resolveRetry(
    makeChallenge({ maskedPrompt: [{ revealed: true, word: 'fear', length: 4 }], status: ChallengeStatus.Beaten }),
  )

  expect(await screen.findByText('result page')).toBeInTheDocument()
  expect(makeGuessSpy).toHaveBeenNthCalledWith(1, 'g1', 'fear')
  expect(makeGuessSpy).toHaveBeenNthCalledWith(2, 'g1', 'fear')
})

it('a second 409 conflict shows a generic alert after exactly two attempts', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(makeChallenge())
  const makeGuessSpy = vi
    .spyOn(api, 'makeGuess')
    .mockRejectedValue({ status: 409, message: 'modified concurrently; retry the operation' })

  renderGameScreen()
  await screen.findByRole('img')
  await guessWith('fear')

  expect(await screen.findByRole('alert')).toBeInTheDocument()
  expect(makeGuessSpy).toHaveBeenCalledTimes(2)
  expect(screen.queryByText('result page')).toBeNull()
})

it('a 404 clears the stored id and returns to landing', async () => {
  localStorage.setItem('btm.activeChallengeId', 'g1')
  vi.spyOn(api, 'getChallenge').mockResolvedValue(makeChallenge())
  vi.spyOn(api, 'makeGuess').mockRejectedValue({ status: 404, message: 'not found' })

  renderGameScreen()
  await screen.findByRole('img')
  await guessWith('gone')

  expect(await screen.findByText('landing page')).toBeInTheDocument()
  expect(localStorage.getItem('btm.activeChallengeId')).toBeNull()
})

it('unexpected status shows inline error with lives unchanged and no navigation', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(makeChallenge())
  vi.spyOn(api, 'makeGuess').mockRejectedValue({ status: 500, message: 'boom' })

  renderGameScreen()
  await screen.findByRole('img')
  await guessWith('bad')

  expect(await screen.findByRole('alert')).toBeInTheDocument()
  expect(screen.getByLabelText('5 lives remaining')).toBeInTheDocument()
  expect(screen.queryByText('result page')).not.toBeInTheDocument()
})

it('submits a guess and shows the updated state', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(makeChallenge())
  vi.spyOn(api, 'makeGuess').mockResolvedValue(
    makeChallenge({ maskedPrompt: [{ revealed: true, word: 'fear', length: 4 }], status: ChallengeStatus.Beaten }),
  )

  renderGameScreen()
  await screen.findByRole('img')
  await guessWith('fear')
  expect(await screen.findByText('result page')).toBeInTheDocument()
})

it('shows "Image unavailable" when the picture FAILED', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(
    makeChallenge({ picture: { status: PictureStatus.Failed, url: null } }),
  )
  renderGameScreen()
  expect(await screen.findByText('Image unavailable')).toBeInTheDocument()
  expect(screen.queryByRole('img')).toBeNull()
})

it('shows "Generating image…" and no img while the picture is PENDING', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(
    makeChallenge({ picture: { status: PictureStatus.Pending, url: null } }),
  )
  renderGameScreen()
  expect(await screen.findByText('Generating image…')).toBeInTheDocument()
  expect(screen.queryByRole('img')).toBeNull()
})

it('shows an error state with a Back to start button when getChallenge fails', async () => {
  vi.spyOn(api, 'getChallenge').mockRejectedValue({ status: 500, message: 'boom' })
  renderGameScreen()
  expect(await screen.findByRole('button', { name: /back to start/i })).toBeInTheDocument()
  expect(screen.queryByText('Loading…')).toBeNull()
})

it('a 404 from getChallenge clears the stored id and returns to landing', async () => {
  localStorage.setItem('btm.activeChallengeId', 'g1')
  vi.spyOn(api, 'getChallenge').mockRejectedValue({ status: 404, message: 'gone' })
  renderGameScreen()
  expect(await screen.findByText('landing page')).toBeInTheDocument()
  expect(localStorage.getItem('btm.activeChallengeId')).toBeNull()
})

it('confirming Give up navigates to the result screen', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(makeChallenge())
  vi.spyOn(api, 'forfeitChallenge').mockResolvedValue(makeChallenge({ status: ChallengeStatus.Lost }))

  renderGameScreen()
  await screen.findByRole('img')
  await userEvent.click(screen.getByRole('button', { name: /give up/i }))
  // First click only reveals the confirmation, it does not forfeit yet.
  expect(screen.queryByText('result page')).toBeNull()
  await userEvent.click(screen.getByRole('button', { name: /^yes$/i }))
  expect(await screen.findByText('result page')).toBeInTheDocument()
})

it('a 409 on forfeit still navigates to the result screen', async () => {
  vi.spyOn(api, 'getChallenge').mockResolvedValue(makeChallenge())
  vi.spyOn(api, 'forfeitChallenge').mockRejectedValue({ status: 409, message: 'challenge is already over' })

  renderGameScreen()
  await screen.findByRole('img')
  await userEvent.click(screen.getByRole('button', { name: /give up/i }))
  await userEvent.click(screen.getByRole('button', { name: /^yes$/i }))
  expect(await screen.findByText('result page')).toBeInTheDocument()
})

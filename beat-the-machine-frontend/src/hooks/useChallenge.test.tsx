import { renderHook } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ChallengeStatus, PictureStatus } from '../generated'
import { makeChallenge } from '../test/fixtures'
import * as api from '../api/challenges'
import { useChallenge } from './useChallenge'

const wrapper = (client: QueryClient) =>
  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={client}>{children}</QueryClientProvider>
  }

const newClient = () =>
  new QueryClient({ defaultOptions: { queries: { retry: false } } })

beforeEach(() => vi.useFakeTimers())
afterEach(() => {
  vi.useRealTimers()
  vi.restoreAllMocks()
})

describe('useChallenge polling', () => {
  it('polls every 1500ms while IN_PROGRESS + picture PENDING', async () => {
    const spy = vi.spyOn(api, 'getChallenge').mockResolvedValue(
      makeChallenge({ picture: { status: PictureStatus.Pending, url: null } }),
    )
    const client = newClient()
    renderHook(() => useChallenge('p1'), { wrapper: wrapper(client) })

    await vi.waitFor(() => expect(spy).toHaveBeenCalledTimes(1))
    await vi.advanceTimersByTimeAsync(1500)
    expect(spy).toHaveBeenCalledTimes(2)
    await vi.advanceTimersByTimeAsync(1500)
    expect(spy).toHaveBeenCalledTimes(3)
  })

  it('stops polling once the picture is READY', async () => {
    const spy = vi.spyOn(api, 'getChallenge').mockResolvedValue(
      makeChallenge({ picture: { status: PictureStatus.Ready, url: 'https://x/y.png' } }),
    )
    const client = newClient()
    renderHook(() => useChallenge('p2'), { wrapper: wrapper(client) })

    await vi.waitFor(() => expect(spy).toHaveBeenCalledTimes(1))
    await vi.advanceTimersByTimeAsync(5000)
    expect(spy).toHaveBeenCalledTimes(1)
  })

  it('stops polling on a terminal status even if picture is PENDING', async () => {
    const spy = vi.spyOn(api, 'getChallenge').mockResolvedValue(
      makeChallenge({ status: ChallengeStatus.Lost, picture: { status: PictureStatus.Pending, url: null } }),
    )
    const client = newClient()
    renderHook(() => useChallenge('p3'), { wrapper: wrapper(client) })

    await vi.waitFor(() => expect(spy).toHaveBeenCalledTimes(1))
    await vi.advanceTimersByTimeAsync(5000)
    expect(spy).toHaveBeenCalledTimes(1)
  })

  it('caps polling so it cannot run forever', async () => {
    const spy = vi.spyOn(api, 'getChallenge').mockResolvedValue(
      makeChallenge({ picture: { status: PictureStatus.Pending, url: null } }),
    )
    const client = newClient()
    renderHook(() => useChallenge('p4'), { wrapper: wrapper(client) })

    await vi.waitFor(() => expect(spy).toHaveBeenCalledTimes(1))
    // Drive well past the cap (~40 polls). Count should plateau, not grow unbounded.
    await vi.advanceTimersByTimeAsync(1500 * 60)
    const capped = spy.mock.calls.length
    expect(capped).toBeLessThanOrEqual(41)
    await vi.advanceTimersByTimeAsync(1500 * 10)
    expect(spy).toHaveBeenCalledTimes(capped)
  })
})

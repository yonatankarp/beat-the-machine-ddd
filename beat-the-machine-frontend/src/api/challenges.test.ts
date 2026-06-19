import { describe, expect, it } from 'vitest'
import { ResponseError } from '../generated'
import { classifyGuessError, unwrap } from './challenges'

describe('classifyGuessError', () => {
  it('maps 422 to invalid', () => {
    expect(classifyGuessError({ status: 422, message: 'bad' })).toBe('invalid')
  })

  it('maps 409 with "already over" (case-insensitive) to gameOver', () => {
    expect(classifyGuessError({ status: 409, message: 'Challenge is ALREADY OVER' })).toBe('gameOver')
  })

  it('maps other 409 to conflictRetry', () => {
    expect(classifyGuessError({ status: 409, message: 'modified concurrently; retry' })).toBe('conflictRetry')
  })

  it('maps 404 to notFound', () => {
    expect(classifyGuessError({ status: 404, message: 'gone' })).toBe('notFound')
  })

  it('maps anything else to unknown', () => {
    expect(classifyGuessError({ status: 500, message: 'boom' })).toBe('unknown')
  })
})

const jsonResponse = (body: unknown, status: number) =>
  new Response(JSON.stringify(body), { status, headers: { 'content-type': 'application/json' } })

describe('unwrap', () => {
  it('returns the resolved value on success', async () => {
    await expect(unwrap(Promise.resolve('ok'))).resolves.toBe('ok')
  })

  it('extracts {status, message} from a JSON error body', async () => {
    const err = new ResponseError(jsonResponse({ message: 'x' }, 409))
    await expect(unwrap(Promise.reject(err))).rejects.toEqual({ status: 409, message: 'x' })
  })

  it('falls back to "unexpected error" when JSON body has no message', async () => {
    const err = new ResponseError(jsonResponse({ other: 1 }, 422))
    await expect(unwrap(Promise.reject(err))).rejects.toEqual({ status: 422, message: 'unexpected error' })
  })

  it('keeps "unexpected error" when the body is not JSON', async () => {
    const err = new ResponseError(new Response('<html>nope</html>', { status: 500 }))
    await expect(unwrap(Promise.reject(err))).rejects.toEqual({ status: 500, message: 'unexpected error' })
  })

  it('maps a non-ResponseError throw to a network error', async () => {
    await expect(unwrap(Promise.reject(new TypeError('Failed to fetch')))).rejects.toEqual({
      status: 0,
      message: 'network error',
    })
  })
})

import { beforeEach, describe, expect, it } from 'vitest'
import { clearChallengeId, loadChallengeId, saveChallengeId } from './challengeStore'

describe('challengeStore', () => {
  beforeEach(() => localStorage.clear())

  it('returns null when nothing is stored', () => {
    expect(loadChallengeId()).toBeNull()
  })

  it('round-trips a saved id', () => {
    saveChallengeId('abc-123')
    expect(loadChallengeId()).toBe('abc-123')
  })

  it('clears the stored id', () => {
    saveChallengeId('abc-123')
    clearChallengeId()
    expect(loadChallengeId()).toBeNull()
  })
})

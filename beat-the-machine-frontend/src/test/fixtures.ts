import { ChallengeStatus, PictureStatus } from '../generated'
import type { ChallengeResponse } from '../generated'

export function makeChallenge(overrides: Partial<ChallengeResponse> = {}): ChallengeResponse {
  return {
    id: 'c1',
    maskedPrompt: [{ revealed: false, word: null, length: 4 }],
    livesRemaining: 5,
    maxLives: 6,
    status: ChallengeStatus.InProgress,
    picture: { status: PictureStatus.Ready, url: 'https://example.com/x.png' },
    ...overrides,
  }
}

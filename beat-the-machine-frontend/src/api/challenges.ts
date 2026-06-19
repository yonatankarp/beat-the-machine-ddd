import {
  ChallengeResponse,
  Difficulty,
  ForfeitChallengeApi,
  GetChallengeApi,
  MakeGuessApi,
  ResponseError,
  StartChallengeApi,
} from '../generated'
import { apiConfig } from './client'

export type ApiError = { status: number; message: string }

const start = new StartChallengeApi(apiConfig)
const get = new GetChallengeApi(apiConfig)
const guess = new MakeGuessApi(apiConfig)
const forfeit = new ForfeitChallengeApi(apiConfig)

async function unwrap<T>(call: Promise<T>): Promise<T> {
  try {
    return await call
  } catch (e) {
    if (e instanceof ResponseError) {
      let message = 'unexpected error'
      try {
        message = (await e.response.clone().json()).message ?? message
      } catch {
        // non-JSON body; keep the default message
      }
      throw { status: e.response.status, message } satisfies ApiError
    }
    throw { status: 0, message: 'network error' } satisfies ApiError
  }
}

export const startChallenge = (difficulty: Difficulty) =>
  unwrap(start.startChallenge({ difficulty }))

export const getChallenge = (id: string) =>
  unwrap(get.getChallenge({ id }))

export const makeGuess = (id: string, word: string) =>
  unwrap(guess.makeGuess({ id, guessRequest: { word } }))

export const forfeitChallenge = (id: string) =>
  unwrap(forfeit.forfeitChallenge({ id }))

export type { ChallengeResponse, Difficulty }

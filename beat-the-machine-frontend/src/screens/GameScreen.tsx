import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { motion, useAnimationControls, useReducedMotion } from 'motion/react'
import { useChallenge } from '../hooks/useChallenge'
import { useHeadingFocus } from '../hooks/useHeadingFocus'
import { classifyGuessError, forfeitChallenge, makeGuess } from '../api/challenges'
import { clearChallengeId } from '../api/challengeStore'
import { ChallengeStatus } from '../generated'
import MaskedPrompt from '../components/MaskedPrompt'
import ChallengePicture from '../components/ChallengePicture'
import Hearts from '../components/Hearts'
import { focusRing } from '../components/focusRing'
import type { ApiError } from '../api/challenges'

export default function GameScreen() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { data: challenge, isLoading, isError, error } = useChallenge(id)
  const headingRef = useHeadingFocus<HTMLHeadingElement>()
  const reduceMotion = useReducedMotion()
  const formControls = useAnimationControls()
  const shake = () => {
    if (!reduceMotion) formControls.start({ x: [0, -8, 8, -6, 6, 0], transition: { duration: 0.4 } })
  }
  const [word, setWord] = useState('')
  const [inlineError, setInlineError] = useState<string | null>(null)
  const [announcement, setAnnouncement] = useState('')
  const [confirmingForfeit, setConfirmingForfeit] = useState(false)
  const retryAttempts = useRef(0)

  const livesRef = useRef(challenge?.livesRemaining ?? 0)
  useEffect(() => {
    if (challenge) livesRef.current = challenge.livesRemaining
  }, [challenge])

  useEffect(() => {
    if (challenge && (challenge.status === ChallengeStatus.Beaten || challenge.status === ChallengeStatus.Lost)) {
      navigate(`/result/${id}`)
    }
  }, [challenge, id, navigate])

  const staleId = isError && !challenge && error?.status === 404
  useEffect(() => {
    if (staleId) {
      clearChallengeId()
      navigate('/')
    }
  }, [staleId, navigate])

  const backToStart = () => {
    clearChallengeId()
    navigate('/')
  }

  const guess = useMutation({
    mutationFn: (w: string) => makeGuess(id, w),
    onSuccess: (updated) => {
      const before = livesRef.current
      qc.setQueryData(['challenge', id], updated)
      setWord('')
      setInlineError(null)
      retryAttempts.current = 0
      if (updated.livesRemaining < before) {
        setAnnouncement(`Wrong, ${updated.livesRemaining} lives left`)
      } else {
        setAnnouncement('Correct')
      }
    },
    onError: (e: ApiError, variables: string) => {
      switch (classifyGuessError(e)) {
        case 'invalid':
          setInlineError('That guess is not valid.')
          setAnnouncement('That guess is not valid.')
          shake()
          retryAttempts.current = 0
          break
        case 'gameOver':
          qc.invalidateQueries({ queryKey: ['challenge', id] })
          navigate(`/result/${id}`)
          break
        case 'conflictRetry':
          if (retryAttempts.current < 1) {
            retryAttempts.current += 1
            guess.mutate(variables)
          } else {
            setInlineError('Something went wrong. Try again.')
            retryAttempts.current = 0
          }
          break
        case 'notFound':
          retryAttempts.current = 0
          backToStart()
          break
        default:
          setInlineError('Something went wrong. Try again.')
          shake()
          retryAttempts.current = 0
      }
    },
  })

  const forfeit = useMutation({
    mutationFn: () => forfeitChallenge(id),
    onSuccess: (final) => {
      qc.setQueryData(['challenge', id], final)
      navigate(`/result/${id}`)
    },
    onError: (e: ApiError) => {
      const kind = classifyGuessError(e)
      if (kind === 'gameOver') {
        navigate(`/result/${id}`)
      } else if (kind === 'notFound') {
        backToStart()
      } else {
        setConfirmingForfeit(false)
        setInlineError('Could not give up. Try again.')
      }
    },
  })

  if (staleId) return null

  if (isError && !challenge) {
    return (
      <main className="grid min-h-screen place-items-center p-6 text-center">
        <div className="flex flex-col items-center gap-5">
          <h1 ref={headingRef} tabIndex={-1} className="font-display text-3xl font-bold">
            We couldn’t load your challenge
          </h1>
          <button
            type="button"
            onClick={backToStart}
            className={`rounded-2xl bg-primary px-7 py-3 font-bold text-on-primary transition hover:bg-primary-hi ${focusRing}`}
          >
            Back to start
          </button>
        </div>
      </main>
    )
  }

  if (isLoading || !challenge) {
    return (
      <main role="status" className="grid min-h-screen place-items-center font-display text-ink-dim">
        Loading…
      </main>
    )
  }

  const submit = (e: React.FormEvent) => {
    e.preventDefault()
    if (word.trim()) guess.mutate(word.trim())
  }

  return (
    <main className="mx-auto flex min-h-screen max-w-3xl flex-col items-center gap-7 p-6">
      <h1 ref={headingRef} tabIndex={-1} className="sr-only">
        Beat the Machine challenge
      </h1>

      <div className="sr-only" role="status" aria-live="polite">
        {announcement}
      </div>

      <div className="flex w-full items-center justify-between">
        <Hearts remaining={challenge.livesRemaining} total={challenge.maxLives} />
        {confirmingForfeit ? (
          <div className="flex items-center gap-3 text-sm">
            <span className="text-ink-dim">Give up?</span>
            <button
              type="button"
              onClick={() => forfeit.mutate()}
              disabled={forfeit.isPending}
              className={`rounded-md px-1 font-semibold text-loss underline underline-offset-4 disabled:opacity-60 ${focusRing}`}
            >
              Yes
            </button>
            <button
              type="button"
              onClick={() => setConfirmingForfeit(false)}
              className={`rounded-md px-1 text-ink-dim underline underline-offset-4 hover:text-ink ${focusRing}`}
            >
              Cancel
            </button>
          </div>
        ) : (
          <button
            type="button"
            onClick={() => setConfirmingForfeit(true)}
            className={`rounded-md px-1 text-sm text-ink-dim underline underline-offset-4 transition hover:text-ink ${focusRing}`}
          >
            Give up
          </button>
        )}
      </div>

      <ChallengePicture picture={challenge.picture} priority />

      <MaskedPrompt tokens={challenge.maskedPrompt} />

      <motion.form
        onSubmit={submit}
        animate={formControls}
        className="flex w-full max-w-md gap-2"
      >
        <input
          value={word}
          onChange={(e) => setWord(e.target.value)}
          maxLength={100}
          placeholder="Guess a word"
          aria-label="Guess a word"
          className={`flex-1 rounded-2xl border border-hairline bg-surface-2 px-4 py-3 text-ink outline-none placeholder:text-ink-dim/70 ${focusRing}`}
        />
        <motion.button
          whileHover={reduceMotion ? undefined : { scale: 1.04 }}
          whileTap={reduceMotion ? undefined : { scale: 0.96 }}
          type="submit"
          disabled={guess.isPending}
          className={`rounded-2xl bg-primary px-6 py-3 font-bold text-on-primary shadow-[0_6px_20px_rgba(245,185,66,0.25)] transition hover:bg-primary-hi disabled:opacity-60 ${focusRing}`}
        >
          Guess
        </motion.button>
      </motion.form>
      {inlineError && (
        <p role="alert" className="text-loss">
          {inlineError}
        </p>
      )}
    </main>
  )
}

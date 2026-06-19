import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { motion } from 'motion/react'
import { useChallenge } from '../hooks/useChallenge'
import { forfeitChallenge, makeGuess } from '../api/challenges'
import { clearChallengeId } from '../api/challengeStore'
import MaskedPrompt from '../components/MaskedPrompt'
import Hearts from '../components/Hearts'
import type { ApiError } from '../api/challenges'

export default function GameScreen() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { data: challenge, isLoading } = useChallenge(id)
  const [word, setWord] = useState('')
  const [shake, setShake] = useState(0)
  const [inlineError, setInlineError] = useState<string | null>(null)

  useEffect(() => {
    if (challenge && challenge.status !== 'IN_PROGRESS') {
      navigate(`/result/${id}`)
    }
  }, [challenge, id, navigate])

  const guess = useMutation({
    mutationFn: (w: string) => makeGuess(id, w),
    onSuccess: (updated) => {
      qc.setQueryData(['challenge', id], updated)
      setWord('')
      setInlineError(null)
    },
    onError: (e: ApiError) => {
      if (e.status === 422) {
        setInlineError('That guess is not valid.')
        setShake((n) => n + 1)
      } else if (e.status === 409) {
        navigate(`/result/${id}`)
      } else if (e.status === 404) {
        clearChallengeId()
        navigate('/')
      }
    },
  })

  const forfeit = useMutation({
    mutationFn: () => forfeitChallenge(id),
    onSuccess: () => navigate(`/result/${id}`),
  })

  if (isLoading || !challenge) {
    return <main className="grid min-h-screen place-items-center text-slate-300">Loading…</main>
  }

  const submit = (e: React.FormEvent) => {
    e.preventDefault()
    if (word.trim()) guess.mutate(word.trim())
  }

  return (
    <main className="mx-auto flex min-h-screen max-w-3xl flex-col items-center gap-6 p-6 text-slate-100">
      <div className="flex w-full items-center justify-between">
        <Hearts remaining={challenge.livesRemaining} />
        <button type="button" onClick={() => forfeit.mutate()} className="text-sm text-slate-400 underline">
          Give up
        </button>
      </div>

      <div className="aspect-square w-full max-w-md overflow-hidden rounded-2xl bg-slate-800 shadow-2xl ring-1 ring-white/10">
        {challenge.picture.status === 'READY' && challenge.picture.url ? (
          <img src={challenge.picture.url} alt="AI generated challenge" className="h-full w-full object-cover" />
        ) : challenge.picture.status === 'FAILED' ? (
          <div className="grid h-full place-items-center text-slate-400">Image unavailable</div>
        ) : (
          <div className="grid h-full animate-pulse place-items-center text-slate-400">Generating image…</div>
        )}
      </div>

      <MaskedPrompt tokens={challenge.maskedPrompt} />

      <motion.form
        key={shake}
        onSubmit={submit}
        animate={shake ? { x: [0, -8, 8, -6, 6, 0] } : {}}
        transition={{ duration: 0.4 }}
        className="flex w-full max-w-md gap-2"
      >
        <input
          value={word}
          onChange={(e) => setWord(e.target.value)}
          maxLength={100}
          placeholder="Guess a word"
          className="flex-1 rounded-lg bg-slate-700 px-4 py-2 text-slate-100 outline-none"
        />
        <button
          type="submit"
          disabled={guess.isPending}
          className="rounded-lg bg-amber-400 px-5 py-2 font-black text-slate-900 disabled:opacity-60"
        >
          Guess
        </button>
      </motion.form>
      {inlineError && (
        <p role="alert" className="text-rose-400">
          {inlineError}
        </p>
      )}
    </main>
  )
}

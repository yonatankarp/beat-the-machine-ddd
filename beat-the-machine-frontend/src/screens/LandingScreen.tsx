import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { startChallenge } from '../api/challenges'
import { saveChallengeId } from '../api/challengeStore'
import type { Difficulty } from '../api/challenges'

const DIFFICULTIES: Difficulty[] = ['EASY', 'MEDIUM', 'HARD']

export default function LandingScreen() {
  const navigate = useNavigate()
  const [difficulty, setDifficulty] = useState<Difficulty>('MEDIUM')
  const [showHelp, setShowHelp] = useState(false)

  const start = useMutation({
    mutationFn: () => startChallenge(difficulty),
    onSuccess: (challenge) => {
      saveChallengeId(challenge.id)
      navigate(`/play/${challenge.id}`)
    },
  })

  return (
    <main className="mx-auto flex min-h-screen max-w-2xl flex-col items-center justify-center gap-8 p-6 text-center text-slate-100">
      <h1 className="text-5xl font-black tracking-tight">Beat the Machine!</h1>
      <p className="text-slate-300">
        Guess the secret prompt behind the AI image before you run out of lives.
      </p>

      <div className="flex gap-3">
        {DIFFICULTIES.map((d) => (
          <button
            key={d}
            type="button"
            onClick={() => setDifficulty(d)}
            aria-pressed={difficulty === d}
            className={`rounded-full px-5 py-2 font-bold transition ${
              difficulty === d ? 'bg-amber-400 text-slate-900' : 'bg-slate-700 text-slate-200'
            }`}
          >
            {d.charAt(0) + d.slice(1).toLowerCase()}
          </button>
        ))}
      </div>

      <button
        type="button"
        onClick={() => start.mutate()}
        disabled={start.isPending}
        className="rounded-xl bg-emerald-500 px-8 py-3 text-lg font-black text-slate-900 transition hover:bg-emerald-400 disabled:opacity-60"
      >
        {start.isPending ? 'Starting…' : "Let's Start!"}
      </button>

      {start.isError && (
        <p role="alert" className="text-rose-400">
          Could not start a challenge. Try again.
        </p>
      )}

      <button type="button" onClick={() => setShowHelp((v) => !v)} className="text-sm text-slate-400 underline">
        How to play
      </button>
      {showHelp && (
        <div className="rounded-lg bg-slate-800 p-4 text-left text-sm text-slate-300">
          <p>
            You see an AI-generated image and a row of blanks, one group per word.
            Type a word and submit it: correct words fill their blanks, wrong
            guesses cost a life. Reveal the whole prompt to win.
          </p>
        </div>
      )}
    </main>
  )
}

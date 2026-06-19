import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { motion, useReducedMotion } from 'motion/react'
import { getChallenge, startChallenge } from '../api/challenges'
import { clearChallengeId, loadChallengeId, saveChallengeId } from '../api/challengeStore'
import { useHeadingFocus } from '../hooks/useHeadingFocus'
import { ChallengeStatus, Difficulty } from '../generated'
import { focusRing } from '../components/focusRing'

const DIFFICULTIES: Difficulty[] = [Difficulty.Easy, Difficulty.Medium, Difficulty.Hard]

// Lives per difficulty mirror the backend policy (domain Lives.initialFor).
const DIFFICULTY_INFO: Record<Difficulty, { lives: number; blurb: string }> = {
  [Difficulty.Easy]: { lives: 8, blurb: 'Most forgiving' },
  [Difficulty.Medium]: { lives: 6, blurb: 'A balanced duel' },
  [Difficulty.Hard]: { lives: 4, blurb: 'For prompt whisperers' },
}

export default function LandingScreen() {
  const navigate = useNavigate()
  const headingRef = useHeadingFocus<HTMLHeadingElement>()
  const [difficulty, setDifficulty] = useState<Difficulty>(Difficulty.Medium)
  const [showHelp, setShowHelp] = useState(false)
  const [storedId] = useState(() => loadChallengeId())

  const resume = useQuery({
    queryKey: ['challenge', storedId],
    queryFn: () => getChallenge(storedId as string),
    enabled: !!storedId,
    retry: false,
  })

  const canResume = resume.data?.status === ChallengeStatus.InProgress
  const resumeSettled = resume.isError || (resume.isSuccess && !canResume)

  useEffect(() => {
    if (storedId && resumeSettled) clearChallengeId()
  }, [storedId, resumeSettled])

  const start = useMutation({
    mutationFn: () => startChallenge(difficulty),
    onSuccess: (challenge) => {
      saveChallengeId(challenge.id)
      navigate(`/play/${challenge.id}`)
    },
  })

  const reduceMotion = useReducedMotion()
  const rise = reduceMotion
    ? {}
    : { initial: { opacity: 0, y: 16 }, animate: { opacity: 1, y: 0 } }

  return (
    <main className="mx-auto flex min-h-screen max-w-2xl flex-col items-center justify-center gap-8 p-6 text-center">
      <motion.div {...rise} transition={{ duration: 0.5 }} className="flex flex-col items-center gap-3">
        <span className="font-display text-sm font-medium uppercase tracking-[0.35em] text-accent">
          Human vs. machine
        </span>
        <h1
          ref={headingRef}
          tabIndex={-1}
          className="font-display text-6xl font-bold leading-[0.95] tracking-tight sm:text-7xl"
        >
          Beat the<br />
          <span className="text-primary">Machine!</span>
        </h1>
      </motion.div>

      <motion.p
        {...rise}
        transition={{ duration: 0.5, delay: 0.08 }}
        className="max-w-md text-lg text-ink-dim"
      >
        Crack the secret prompt behind the AI image before you run out of lives.
      </motion.p>

      {canResume && storedId && (
        <button
          type="button"
          onClick={() => navigate(`/play/${storedId}`)}
          className={`rounded-2xl border border-hairline bg-surface-2 px-8 py-3 text-lg font-semibold text-ink transition hover:bg-surface ${focusRing}`}
        >
          Resume your challenge
        </button>
      )}

      <motion.div
        {...rise}
        transition={{ duration: 0.5, delay: 0.16 }}
        className="flex flex-col items-center gap-3"
      >
        <span className="text-xs font-medium uppercase tracking-widest text-ink-dim">Difficulty</span>
        <div className="flex gap-2 rounded-full border border-hairline bg-surface/60 p-1.5">
          {DIFFICULTIES.map((d) => (
            <button
              key={d}
              type="button"
              onClick={() => setDifficulty(d)}
              aria-pressed={difficulty === d}
              className={`rounded-full px-5 py-2 font-semibold transition ${focusRing} ${
                difficulty === d
                  ? 'bg-primary text-on-primary shadow-[0_0_18px_rgba(245,185,66,0.35)]'
                  : 'text-ink-dim hover:text-ink'
              }`}
            >
              {d.charAt(0) + d.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
        <p className="text-sm text-ink-dim">
          {DIFFICULTY_INFO[difficulty].blurb} · {DIFFICULTY_INFO[difficulty].lives} lives
        </p>
      </motion.div>

      <motion.button
        {...rise}
        transition={{ duration: 0.5, delay: 0.24 }}
        whileHover={reduceMotion ? undefined : { scale: 1.03 }}
        whileTap={reduceMotion ? undefined : { scale: 0.97 }}
        type="button"
        onClick={() => start.mutate()}
        disabled={start.isPending}
        className={`rounded-2xl bg-primary px-10 py-4 text-xl font-bold text-on-primary shadow-[0_8px_30px_rgba(245,185,66,0.3)] transition hover:bg-primary-hi disabled:opacity-60 ${focusRing}`}
      >
        {start.isPending ? 'Starting…' : "Let's Start!"}
      </motion.button>

      {start.isError && (
        <p role="alert" className="text-loss">
          Could not start a challenge. Try again.
        </p>
      )}

      <button
        type="button"
        onClick={() => setShowHelp((v) => !v)}
        aria-expanded={showHelp}
        aria-controls="how-to-play-panel"
        className={`rounded-md text-sm text-ink-dim underline decoration-ink-dim/40 underline-offset-4 transition hover:text-ink ${focusRing}`}
      >
        How to play
      </button>
      {showHelp && (
        <div
          id="how-to-play-panel"
          className="rounded-2xl border border-hairline bg-surface p-5 text-left text-sm leading-relaxed text-ink-dim"
        >
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

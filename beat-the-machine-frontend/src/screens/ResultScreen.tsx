import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { motion, useReducedMotion } from 'motion/react'
import { useChallenge } from '../hooks/useChallenge'
import { useHeadingFocus } from '../hooks/useHeadingFocus'
import { clearChallengeId } from '../api/challengeStore'
import { ChallengeStatus } from '../generated'
import ChallengePicture from '../components/ChallengePicture'
import Confetti from '../components/Confetti'
import { focusRing } from '../components/focusRing'

export default function ResultScreen() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { data: challenge, isLoading, isError, error } = useChallenge(id)
  const headingRef = useHeadingFocus<HTMLHeadingElement>()
  const reduceMotion = useReducedMotion()

  const backToStart = () => {
    clearChallengeId()
    qc.removeQueries({ queryKey: ['challenge'] })
    navigate('/')
  }

  const staleId = isError && !challenge && error?.status === 404
  useEffect(() => {
    if (staleId) {
      clearChallengeId()
      navigate('/')
    }
  }, [staleId, navigate])

  if (staleId) return null

  if (isError && !challenge) {
    return (
      <main className="grid min-h-screen place-items-center p-6 text-center">
        <div className="flex flex-col items-center gap-5">
          <h1 ref={headingRef} tabIndex={-1} className="font-display text-3xl font-bold">
            We couldn’t load your result
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

  const won = challenge.status === ChallengeStatus.Beaten
  const prompt = challenge.maskedPrompt.map((t) => t.word ?? '').join(' ').trim()

  // Staggered entrance: each child rises slightly after the last.
  const container = reduceMotion
    ? {}
    : {
        initial: 'hidden',
        animate: 'show',
        variants: { show: { transition: { staggerChildren: 0.12 } } },
      }
  const item = reduceMotion
    ? {}
    : {
        variants: {
          hidden: { opacity: 0, y: 18 },
          show: { opacity: 1, y: 0, transition: { type: 'spring', stiffness: 320, damping: 26 } },
        },
      }

  return (
    <motion.main
      {...container}
      className="relative mx-auto flex min-h-screen max-w-2xl flex-col items-center justify-center gap-6 p-6 text-center"
    >
      {won && <Confetti />}

      <motion.h1
        {...item}
        ref={headingRef}
        tabIndex={-1}
        className={`font-display text-5xl font-bold ${won ? 'text-win' : 'text-loss'}`}
      >
        {won ? 'You beat the machine!' : 'The machine won this time.'}
      </motion.h1>

      {/* On a win the hero pulses with an emerald glow; on a loss it sits still and desaturated. */}
      <motion.div
        {...item}
        animate={
          reduceMotion || !won
            ? item.variants?.show
            : {
                opacity: 1,
                y: 0,
                scale: [1, 1.015, 1],
                transition: { scale: { duration: 2.4, repeat: Infinity, ease: 'easeInOut' } },
              }
        }
        className="w-full max-w-lg"
      >
        <ChallengePicture picture={challenge.picture} tone={won ? 'win' : 'loss'} />
      </motion.div>

      <motion.p {...item} className="text-lg text-ink-dim">
        The prompt was:{' '}
        <span className="font-display font-semibold text-ink">{prompt}</span>
      </motion.p>

      <motion.button
        {...item}
        whileHover={reduceMotion ? undefined : { scale: 1.03 }}
        whileTap={reduceMotion ? undefined : { scale: 0.97 }}
        type="button"
        onClick={backToStart}
        className={`rounded-2xl bg-primary px-10 py-4 text-xl font-bold text-on-primary shadow-[0_8px_30px_rgba(245,185,66,0.3)] transition hover:bg-primary-hi ${focusRing}`}
      >
        Play again
      </motion.button>
    </motion.main>
  )
}

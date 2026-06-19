import { motion, useReducedMotion } from 'motion/react'
import type { MaskedToken } from '../generated'

export default function MaskedPrompt({ tokens }: { tokens: MaskedToken[] }) {
  const reduceMotion = useReducedMotion()
  // Stagger the reveal across words for a satisfying left-to-right cascade.
  let revealIndex = 0

  return (
    <div
      role="group"
      aria-label="Prompt to guess"
      className="flex flex-wrap items-end justify-center gap-x-4 gap-y-4 rounded-2xl border border-hairline bg-surface/60 px-5 py-4"
    >
      {tokens.map((token, i) => {
        if (token.revealed && token.word) {
          const order = revealIndex++
          return (
            <motion.span
              key={i}
              initial={reduceMotion ? false : { opacity: 0, scale: 0.5, y: 6 }}
              animate={
                reduceMotion
                  ? { opacity: 1, scale: 1 }
                  : { opacity: 1, scale: [0.5, 1.18, 1], y: 0 }
              }
              transition={
                reduceMotion
                  ? { duration: 0 }
                  : { type: 'spring', stiffness: 520, damping: 16, delay: order * 0.06 }
              }
              className="whitespace-nowrap font-display text-2xl font-bold text-win"
            >
              {token.word}
            </motion.span>
          )
        }
        return (
          <span
            key={i}
            aria-label={`${token.length}-letter word, hidden`}
            className="flex gap-1.5 whitespace-nowrap"
          >
            {Array.from({ length: token.length }).map((_, j) => (
              <motion.span
                key={j}
                aria-hidden="true"
                data-testid="blank"
                animate={reduceMotion ? undefined : { opacity: [0.55, 0.9, 0.55] }}
                transition={
                  reduceMotion
                    ? undefined
                    : { duration: 2.4, repeat: Infinity, delay: (i + j) * 0.12, ease: 'easeInOut' }
                }
                className="inline-block w-5 rounded-[3px] border-b-2 border-accent/60 bg-accent/5 text-center font-display text-2xl"
              >
                &nbsp;
              </motion.span>
            ))}
          </span>
        )
      })}
    </div>
  )
}

import { motion, useReducedMotion } from 'motion/react'

// Renders remaining out of total: filled hearts for lives left, dimmed hearts
// for lives lost, so the player can always judge their margin. The fill -> empty
// transition animates on a lost life (gated by prefers-reduced-motion).
export default function Hearts({ remaining, total }: { remaining: number; total: number }) {
  const reduceMotion = useReducedMotion()
  const slots = Array.from({ length: Math.max(total, remaining) }, (_, i) => i)

  return (
    <div
      role="status"
      aria-label={`${remaining} of ${total} lives remaining`}
      className="flex gap-1.5 text-2xl"
    >
      {slots.map((i) => {
        const filled = i < remaining
        return (
          <motion.span
            key={i}
            aria-hidden="true"
            data-filled={filled}
            animate={
              filled
                ? { opacity: 1, scale: 1, filter: 'grayscale(0)' }
                : { opacity: 0.25, scale: 0.9, filter: 'grayscale(1)' }
            }
            transition={
              reduceMotion ? { duration: 0 } : { type: 'spring', stiffness: 500, damping: 22 }
            }
            className={
              filled ? 'inline-block drop-shadow-[0_0_8px_rgba(251,93,110,0.45)]' : 'inline-block'
            }
          >
            ❤️
          </motion.span>
        )
      })}
    </div>
  )
}

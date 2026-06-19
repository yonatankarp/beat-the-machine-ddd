import { AnimatePresence, motion, useReducedMotion } from 'motion/react'

export default function Hearts({ remaining }: { remaining: number }) {
  const reduceMotion = useReducedMotion()
  // Capacity (max lives) is not in the API, so we only render — and animate the
  // loss of — the hearts that actually exist. No fabricated denominator.
  const slots = Array.from({ length: remaining }, (_, i) => i)

  return (
    <div
      role="status"
      aria-label={`${remaining} lives remaining`}
      className="flex gap-1.5 text-2xl"
    >
      <AnimatePresence mode="popLayout" initial={false}>
        {slots.map((slot) => (
          <motion.span
            key={slot}
            aria-hidden="true"
            initial={reduceMotion ? false : { scale: 0.4, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={reduceMotion ? { opacity: 0 } : { scale: 1.6, opacity: 0, rotate: -20, y: -8 }}
            transition={
              reduceMotion ? { duration: 0 } : { type: 'spring', stiffness: 600, damping: 24 }
            }
            className="inline-block drop-shadow-[0_0_8px_rgba(251,93,110,0.45)]"
          >
            ❤️
          </motion.span>
        ))}
      </AnimatePresence>
    </div>
  )
}

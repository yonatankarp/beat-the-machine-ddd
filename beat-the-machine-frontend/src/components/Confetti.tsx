import { motion, useReducedMotion } from 'motion/react'

const COLORS = ['#f5b942', '#34d399', '#7c6bff', '#ffce5e', '#fb5d6e']
const PIECES = 28

// A small, deliberately cheap burst — decorative only, so it is hidden from
// assistive tech and skipped entirely when motion is reduced.
export default function Confetti() {
  const reduceMotion = useReducedMotion()
  if (reduceMotion) return null

  return (
    <div aria-hidden="true" className="pointer-events-none absolute inset-0 overflow-hidden">
      {Array.from({ length: PIECES }).map((_, i) => {
        const left = (i / PIECES) * 100
        const delay = (i % 7) * 0.08
        const drift = (i % 2 === 0 ? 1 : -1) * (20 + (i % 5) * 14)
        const size = 7 + (i % 3) * 3
        return (
          <motion.span
            key={i}
            initial={{ top: '-8%', left: `${left}%`, opacity: 1, rotate: 0 }}
            animate={{ top: '108%', x: drift, opacity: [1, 1, 0], rotate: 540 }}
            transition={{ duration: 1.8 + (i % 4) * 0.3, delay, ease: 'easeIn' }}
            style={{
              position: 'absolute',
              width: size,
              height: size * 1.6,
              borderRadius: 2,
              backgroundColor: COLORS[i % COLORS.length],
            }}
          />
        )
      })}
    </div>
  )
}

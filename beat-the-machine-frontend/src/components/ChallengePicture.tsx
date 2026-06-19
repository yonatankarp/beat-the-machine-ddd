import { PictureStatus } from '../generated'
import type { Picture } from '../generated'

type Tone = 'neutral' | 'win' | 'loss'

const frameTone: Record<Tone, string> = {
  neutral: 'from-accent/40 via-accent/10 to-primary/30',
  win: 'from-win/60 via-win/20 to-accent/30',
  loss: 'from-loss/40 via-loss/10 to-night/40',
}

export default function ChallengePicture({
  picture,
  priority = false,
  tone = 'neutral',
}: {
  picture: Picture
  priority?: boolean
  tone?: Tone
}) {
  const lost = tone === 'loss'
  return (
    // Gradient ring frames the hero; padding creates the glow border.
    <div className={`w-full max-w-lg rounded-2xl bg-gradient-to-br p-px ${frameTone[tone]}`}>
      <div className="aspect-square w-full overflow-hidden rounded-[15px] bg-surface ring-1 ring-hairline">
        {picture.status === PictureStatus.Ready && picture.url ? (
          <img
            src={picture.url}
            alt="AI generated challenge"
            decoding="async"
            fetchPriority={priority ? 'high' : 'auto'}
            className={`h-full w-full object-cover transition duration-500 ${
              lost ? 'opacity-70 saturate-50' : ''
            }`}
          />
        ) : picture.status === PictureStatus.Failed ? (
          <div role="status" className="grid h-full place-items-center font-display text-ink-dim">
            Image unavailable
          </div>
        ) : (
          <div
            role="status"
            className="grid h-full animate-pulse place-items-center font-display text-ink-dim"
          >
            Generating image…
          </div>
        )}
      </div>
    </div>
  )
}

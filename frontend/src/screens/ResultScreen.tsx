import { useNavigate, useParams } from 'react-router-dom'
import { useChallenge } from '../hooks/useChallenge'
import { clearChallengeId } from '../api/challengeStore'

export default function ResultScreen() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const { data: challenge, isLoading } = useChallenge(id)

  if (isLoading || !challenge) {
    return <main className="grid min-h-screen place-items-center text-slate-300">Loading…</main>
  }

  const won = challenge.status === 'BEATEN'
  const prompt = challenge.maskedPrompt.map((t) => t.word ?? '???').join(' ')

  const playAgain = () => {
    clearChallengeId()
    navigate('/')
  }

  return (
    <main className="mx-auto flex min-h-screen max-w-2xl flex-col items-center justify-center gap-6 p-6 text-center text-slate-100">
      <h1 className={`text-4xl font-black ${won ? 'text-emerald-400' : 'text-rose-400'}`}>
        {won ? 'You beat the machine!' : 'The machine won this time.'}
      </h1>

      {challenge.picture.status === 'READY' && challenge.picture.url && (
        <img
          src={challenge.picture.url}
          alt="AI generated challenge"
          className="aspect-square w-full max-w-sm rounded-2xl object-cover ring-1 ring-white/10"
        />
      )}

      <p className="text-lg text-slate-300">
        The prompt was: <span className="font-bold text-slate-100">{prompt}</span>
      </p>

      <button
        type="button"
        onClick={playAgain}
        className="rounded-xl bg-emerald-500 px-8 py-3 text-lg font-black text-slate-900 hover:bg-emerald-400"
      >
        Play again
      </button>
    </main>
  )
}

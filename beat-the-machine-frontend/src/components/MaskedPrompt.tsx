import type { MaskedToken } from '../generated'

export default function MaskedPrompt({ tokens }: { tokens: MaskedToken[] }) {
  return (
    <div className="flex flex-wrap items-end justify-center gap-x-4 gap-y-3">
      {tokens.map((token, i) => (
        <span key={i} className="flex gap-1">
          {token.revealed && token.word ? (
            <span className="text-2xl font-black text-emerald-400">{token.word}</span>
          ) : (
            Array.from({ length: token.length }).map((_, j) => (
              <span
                key={j}
                data-testid="blank"
                className="inline-block w-5 border-b-2 border-slate-400 text-center text-2xl"
              >
                &nbsp;
              </span>
            ))
          )}
        </span>
      ))}
    </div>
  )
}

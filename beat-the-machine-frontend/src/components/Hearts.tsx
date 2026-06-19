export default function Hearts({ remaining }: { remaining: number }) {
  return (
    <div className="flex gap-1 text-2xl" aria-label={`${remaining} lives remaining`}>
      {Array.from({ length: remaining }).map((_, i) => (
        <span key={i}>❤️</span>
      ))}
    </div>
  )
}

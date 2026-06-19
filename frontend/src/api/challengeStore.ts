const KEY = 'btm.activeChallengeId'

export function saveChallengeId(id: string): void {
  localStorage.setItem(KEY, id)
}

export function loadChallengeId(): string | null {
  return localStorage.getItem(KEY)
}

export function clearChallengeId(): void {
  localStorage.removeItem(KEY)
}

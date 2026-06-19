import { useEffect, useRef } from 'react'

// Moves focus to the screen's heading on mount so keyboard and screen-reader
// users land on the new content after a route change instead of a stale node.
export function useHeadingFocus<T extends HTMLElement>() {
  const ref = useRef<T>(null)
  useEffect(() => {
    ref.current?.focus()
  }, [])
  return ref
}

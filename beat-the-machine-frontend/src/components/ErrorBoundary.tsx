import { Component, type ReactNode } from 'react'
import { focusRing } from './focusRing'

type Props = { children: ReactNode }
type State = { hasError: boolean }

export default class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false }

  static getDerivedStateFromError(): State {
    return { hasError: true }
  }

  render() {
    if (this.state.hasError) {
      return (
        <main className="grid min-h-screen place-items-center p-6 text-center">
          <div className="flex flex-col items-center gap-5">
            <h1 className="font-display text-3xl font-bold">Something went wrong</h1>
            <a
              href="/app/"
              className={`rounded-2xl bg-primary px-7 py-3 font-bold text-on-primary transition hover:bg-primary-hi ${focusRing}`}
            >
              Back to start
            </a>
          </div>
        </main>
      )
    }
    return this.props.children
  }
}

import { QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'
import AppRoutes from './routes'
import { queryClient } from './api/queryClient'
import ErrorBoundary from './components/ErrorBoundary'

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ErrorBoundary>
        <BrowserRouter basename="/app">
          <div className="min-h-screen bg-slate-900">
            <AppRoutes />
          </div>
        </BrowserRouter>
      </ErrorBoundary>
    </QueryClientProvider>
  )
}

import { QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'
import AppRoutes from './routes'
import { queryClient } from './api/queryClient'

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter basename="/app">
        <div className="min-h-screen bg-slate-900">
          <AppRoutes />
        </div>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

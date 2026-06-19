import { Navigate, Route, Routes } from 'react-router-dom'
import LandingScreen from './screens/LandingScreen'
import GameScreen from './screens/GameScreen'
import ResultScreen from './screens/ResultScreen'

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<LandingScreen />} />
      <Route path="/play/:id" element={<GameScreen />} />
      <Route path="/result/:id" element={<ResultScreen />} />
      <Route path="/profile" element={<div className="p-6 text-slate-300">Profiles coming soon.</div>} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

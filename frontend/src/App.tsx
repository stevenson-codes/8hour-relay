import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import AddTeamPage from './pages/AddTeamPage'
import TeamEditPage from './pages/TeamEditPage'
import LeaderboardPage from './pages/LeaderboardPage'
import { READ_ONLY } from './config'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        {!READ_ONLY && (
          <>
            <Route path="/add-team" element={<AddTeamPage />} />
            <Route path="/teams/:teamId" element={<TeamEditPage />} />
          </>
        )}
        <Route path="/leaderboard" element={<LeaderboardPage />} />
        {READ_ONLY && (
          <Route path="*" element={<Navigate to="/" replace />} />
        )}
      </Routes>
    </BrowserRouter>
  )
}

export default App

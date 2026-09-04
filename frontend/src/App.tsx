import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import AddTeamPage from './pages/AddTeamPage'
import TeamEditPage from './pages/TeamEditPage'
import LeaderboardPage from './pages/LeaderboardPage'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/add-team" element={<AddTeamPage />} />
        <Route path="/teams/:teamId" element={<TeamEditPage />} />
        <Route path="/leaderboard" element={<LeaderboardPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App

import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import AddTeamPage from './pages/AddTeamPage'
import TeamEditPage from './pages/TeamEditPage'
import RunnersBoardPage from './pages/RunnersBoardPage'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/add-team" element={<AddTeamPage />} />
        <Route path="/teams/:teamId" element={<TeamEditPage />} />
        <Route path="/board" element={<RunnersBoardPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App

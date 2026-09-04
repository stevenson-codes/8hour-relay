import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import AddTeamPage from './pages/AddTeamPage'
import TeamEditPage from './pages/TeamEditPage'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/add-team" element={<AddTeamPage />} />
        <Route path="/teams/:teamId" element={<TeamEditPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App

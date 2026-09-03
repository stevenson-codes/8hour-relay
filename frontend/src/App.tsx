import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import AddTeamPage from './pages/AddTeamPage'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/add-team" element={<AddTeamPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App

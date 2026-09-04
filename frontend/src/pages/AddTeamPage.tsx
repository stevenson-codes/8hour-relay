import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import type { ApiError, CreateTeamResponse } from '../types/TeamAdmin'
import '../App.css'
import './AddTeamPage.css'

async function postJson<T>(url: string, body: unknown): Promise<T> {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const data = await res.json()
  if (!res.ok) {
    throw new Error((data as ApiError).error ?? `Request failed: ${res.status}`)
  }
  return data as T
}

function AddTeamPage() {
  const navigate = useNavigate()

  const [teamName, setTeamName] = useState('')
  const [teamDivision, setTeamDivision] = useState('')
  const [teamEpcHex, setTeamEpcHex] = useState('')
  const [teamError, setTeamError] = useState<string | null>(null)
  const [teamPending, setTeamPending] = useState(false)

  function handleCreateTeam(e: FormEvent) {
    e.preventDefault()
    if (teamPending) return

    if (!teamName.trim() || !teamDivision.trim() || !teamEpcHex.trim()) {
      setTeamError('Please fill out all required fields.')
      return
    }

    setTeamPending(true)
    setTeamError(null)
    postJson<CreateTeamResponse>('/api/teams', {
      name: teamName,
      division: teamDivision,
      epcHex: teamEpcHex,
    })
      .then((created) => {
        navigate(`/teams/${created.id}`)
      })
      .catch((err: Error) => {
        setTeamError(err.message)
        setTeamPending(false)
      })
  }

  return (
    <div className="board">
      <header className="board-header">
        <div className="brand">
          <svg className="brand-mark" width="34" height="34" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M2 18 8 8l3.2 5L14 9l8 9z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
          </svg>
          <span className="brand-name">
            VANCOUVER
            <br />
            RUNAHOLICS
          </span>
        </div>

        <div className="event-title">
          <h1>Add Team</h1>
          <p>Race Setup</p>
        </div>

        <div className="header-right">
          <Link to="/" className="nav-link-button">
            ← Back to Standings
          </Link>
        </div>
      </header>

      <main className="add-team-page">
        <section className="form-card">
          <h2>Team Details</h2>
          <form onSubmit={handleCreateTeam} className="admin-form" noValidate>
            <label className="form-field">
              <span>Team Name</span>
              <input
                type="text"
                value={teamName}
                onChange={(e) => setTeamName(e.target.value)}
                required
                placeholder="Team A"
              />
            </label>
            <label className="form-field">
              <span>Division</span>
              <input
                type="text"
                value={teamDivision}
                onChange={(e) => setTeamDivision(e.target.value)}
                required
                placeholder="Open"
              />
            </label>
            <label className="form-field">
              <span>Team Tag EPC Hex</span>
              <input
                type="text"
                value={teamEpcHex}
                onChange={(e) => setTeamEpcHex(e.target.value)}
                required
                placeholder="E28069150000700ED5465091"
              />
            </label>
            {teamError && <p className="form-error">{teamError}</p>}
            <button type="submit" className="start-button" disabled={teamPending}>
              {teamPending ? 'Creating…' : 'Create Team'}
            </button>
          </form>
        </section>
      </main>
    </div>
  )
}

export default AddTeamPage

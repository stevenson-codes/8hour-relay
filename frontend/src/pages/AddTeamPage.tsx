import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import type { ApiError, CreateRunnerResponse, CreateTeamResponse } from '../types/TeamAdmin'
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
  const [team, setTeam] = useState<CreateTeamResponse | null>(null)
  const [teamName, setTeamName] = useState('')
  const [teamDivision, setTeamDivision] = useState('')
  const [teamEpcHex, setTeamEpcHex] = useState('')
  const [teamError, setTeamError] = useState<string | null>(null)
  const [teamPending, setTeamPending] = useState(false)

  const [runners, setRunners] = useState<CreateRunnerResponse[]>([])
  const [runnerName, setRunnerName] = useState('')
  const [runnerLeg, setRunnerLeg] = useState('1')
  const [runnerBib, setRunnerBib] = useState('')
  const [runnerSex, setRunnerSex] = useState('')
  const [runnerEpcHex, setRunnerEpcHex] = useState('')
  const [runnerError, setRunnerError] = useState<string | null>(null)
  const [runnerPending, setRunnerPending] = useState(false)

  function handleCreateTeam(e: FormEvent) {
    e.preventDefault()
    if (teamPending) return

    if (!teamName.trim() || !teamEpcHex.trim()) {
      setTeamError('Please fill out all required fields.')
      return
    }

    setTeamPending(true)
    setTeamError(null)
    postJson<CreateTeamResponse>('/api/teams', {
      name: teamName,
      division: teamDivision || null,
      epcHex: teamEpcHex,
    })
      .then((created) => {
        setTeam(created)
        setRunnerLeg('1')
      })
      .catch((err: Error) => setTeamError(err.message))
      .finally(() => setTeamPending(false))
  }

  function handleAddRunner(e: FormEvent) {
    e.preventDefault()
    if (runnerPending || team === null) return

    if (!runnerName.trim() || !runnerLeg.trim() || !runnerEpcHex.trim()) {
      setRunnerError('Please fill out all required fields.')
      return
    }

    setRunnerPending(true)
    setRunnerError(null)
    postJson<CreateRunnerResponse>(`/api/teams/${team.id}/runners`, {
      name: runnerName,
      leg: Number(runnerLeg),
      bib: runnerBib || null,
      sex: runnerSex || null,
      epcHex: runnerEpcHex,
    })
      .then((created) => {
        setRunners((prev) => [...prev, created])
        setRunnerName('')
        setRunnerBib('')
        setRunnerSex('')
        setRunnerEpcHex('')
        setRunnerLeg(String(created.leg + 1))
      })
      .catch((err: Error) => setRunnerError(err.message))
      .finally(() => setRunnerPending(false))
  }

  function handleStartOver() {
    setTeam(null)
    setTeamName('')
    setTeamDivision('')
    setTeamEpcHex('')
    setTeamError(null)
    setRunners([])
    setRunnerError(null)
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
          <h1>Add Team &amp; Runners</h1>
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
          <h2>1. Team Details</h2>
          {team ? (
            <div className="form-summary">
              <p>
                <strong>{team.name}</strong> {team.division && <span>({team.division})</span>} — tag{' '}
                <code>{team.epcHex}</code>
              </p>
              <button type="button" className="clear-button" onClick={handleStartOver}>
                Start Over / Add Another Team
              </button>
            </div>
          ) : (
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
                <span>Division (optional)</span>
                <input
                  type="text"
                  value={teamDivision}
                  onChange={(e) => setTeamDivision(e.target.value)}
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
          )}
        </section>

        <section className="form-card" aria-disabled={team === null}>
          <h2>2. Add Runners</h2>
          {team === null ? (
            <p className="board-status">Create the team first.</p>
          ) : (
            <>
              <form onSubmit={handleAddRunner} className="admin-form admin-form-row" noValidate>
                <label className="form-field">
                  <span>Name</span>
                  <input
                    type="text"
                    value={runnerName}
                    onChange={(e) => setRunnerName(e.target.value)}
                    required
                    placeholder="Jordan Lee"
                  />
                </label>
                <label className="form-field form-field-narrow">
                  <span>Leg</span>
                  <input
                    type="number"
                    min={1}
                    value={runnerLeg}
                    onChange={(e) => setRunnerLeg(e.target.value)}
                    required
                  />
                </label>
                <label className="form-field form-field-narrow">
                  <span>Bib (optional)</span>
                  <input type="text" value={runnerBib} onChange={(e) => setRunnerBib(e.target.value)} placeholder="A01" />
                </label>
                <label className="form-field form-field-narrow">
                  <span>Sex (optional)</span>
                  <select value={runnerSex} onChange={(e) => setRunnerSex(e.target.value)}>
                    <option value="">—</option>
                    <option value="M">M</option>
                    <option value="F">F</option>
                  </select>
                </label>
                <label className="form-field">
                  <span>Runner Tag EPC Hex</span>
                  <input
                    type="text"
                    value={runnerEpcHex}
                    onChange={(e) => setRunnerEpcHex(e.target.value)}
                    required
                    placeholder="000000000000000000006401"
                  />
                </label>
                {runnerError && <p className="form-error">{runnerError}</p>}
                <button type="submit" className="start-button" disabled={runnerPending}>
                  {runnerPending ? 'Adding…' : 'Add Runner'}
                </button>
              </form>

              {runners.length > 0 && (
                <table className="runner-table admin-added-table">
                  <thead>
                    <tr>
                      <th>Leg</th>
                      <th>Runner</th>
                      <th>Bib</th>
                      <th>Sex</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {runners.map((runner) => (
                      <tr key={runner.id}>
                        <td>R{runner.leg}</td>
                        <td>{runner.name}</td>
                        <td>{runner.bib ?? '—'}</td>
                        <td>{runner.sex ?? '—'}</td>
                        <td>{runner.status}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </>
          )}
        </section>
      </main>
    </div>
  )
}

export default AddTeamPage

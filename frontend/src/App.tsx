import { useCallback, useEffect, useState } from 'react'
import type { TeamSummary } from './types/TeamSummary'
import type { RaceStatus } from './types/RaceStatus'
import './App.css'

const TEAM_COLORS = ['#aa3bff', '#3ba7ff', '#ff8a3b', '#3bffa0', '#ff3b6b']
const REFRESH_INTERVAL_MS = 15_000

function formatLapTime(millis: number | null): string {
  if (millis === null) return '—'
  const totalSeconds = Math.round(millis / 1000)
  const min = Math.floor(totalSeconds / 60)
  const sec = totalSeconds % 60
  return `${min}:${sec.toString().padStart(2, '0')}`
}

function App() {
  const [teams, setTeams] = useState<TeamSummary[] | null>(null)
  const [raceActive, setRaceActive] = useState<boolean | null>(null)
  const [raceActionPending, setRaceActionPending] = useState(false)
  const [clearPending, setClearPending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadData = useCallback(() => {
    return Promise.all([
      fetch('/api/teams').then((res) => {
        if (!res.ok) throw new Error(`Request failed: ${res.status}`)
        return res.json() as Promise<TeamSummary[]>
      }),
      fetch('/api/race/status').then((res) => {
        if (!res.ok) throw new Error(`Request failed: ${res.status}`)
        return res.json() as Promise<RaceStatus>
      }),
    ])
      .then(([teamsData, raceStatus]) => {
        setTeams(teamsData)
        setRaceActive(raceStatus.active)
        setError(null)
      })
      .catch((err: Error) => setError(err.message))
  }, [])

  useEffect(() => {
    loadData()
    const intervalId = setInterval(loadData, REFRESH_INTERVAL_MS)
    return () => clearInterval(intervalId)
  }, [loadData])

  const handleRaceToggle = useCallback(() => {
    if (raceActive === null || raceActionPending) return

    const endpoint = raceActive ? '/api/race/stop' : '/api/race/start'
    setRaceActionPending(true)
    fetch(endpoint, { method: 'POST' })
      .then((res) => {
        if (!res.ok) throw new Error(`Request failed: ${res.status}`)
        return res.json() as Promise<RaceStatus>
      })
      .then((status) => {
        setRaceActive(status.active)
        setError(null)
      })
      .catch((err: Error) => setError(err.message))
      .finally(() => setRaceActionPending(false))
  }, [raceActive, raceActionPending])

  const handleClearLapRecords = useCallback(() => {
    if (raceActive !== false || clearPending) return
    if (!window.confirm('This will permanently delete all lap records. Continue?')) return

    setClearPending(true)
    fetch('/api/lap-records', { method: 'DELETE' })
      .then((res) => {
        if (!res.ok) throw new Error(`Request failed: ${res.status}`)
        setError(null)
        return loadData()
      })
      .catch((err: Error) => setError(err.message))
      .finally(() => setClearPending(false))
  }, [raceActive, clearPending, loadData])

  return (
    <>
      <header id="page-header">
        <h1>8 Hour Relay</h1>
        <p>Live standings{teams ? ` — ${teams.length} teams` : ''}</p>
        <div className="race-controls">
          <span className={`race-status ${raceActive ? 'active' : 'inactive'}`}>
            {raceActive === null ? 'Loading…' : raceActive ? 'Race Running' : 'Race Stopped'}
          </span>
          <button
            type="button"
            className={raceActive ? 'stop-button' : 'start-button'}
            onClick={handleRaceToggle}
            disabled={raceActive === null || raceActionPending}
          >
            {raceActive ? 'Stop Race' : 'Start Race'}
          </button>
          <button
            type="button"
            className="clear-button"
            onClick={handleClearLapRecords}
            disabled={raceActive !== false || clearPending}
            title={raceActive !== false ? 'Stop the race before clearing lap records' : undefined}
          >
            Clear Lap Records
          </button>
        </div>
      </header>

      <section id="teams">
        {error && <p className="error">Failed to load teams: {error}</p>}
        {!error && teams === null && <p>Loading teams...</p>}
        {!error &&
          teams?.map((team, i) => (
            <div
              className="team-card"
              key={team.id}
              style={{ '--team-color': TEAM_COLORS[i % TEAM_COLORS.length] } as React.CSSProperties}
            >
              <div className="team-card-header">
                <h2>{team.name}</h2>
                <div className="team-stats">
                  <div className="stat">
                    <span className="stat-value">{team.totalLaps}</span>
                    <span className="stat-label">Total Laps</span>
                  </div>
                  <div className="stat">
                    <span className="stat-value">{formatLapTime(team.avgLapTimeMillis)}</span>
                    <span className="stat-label">Avg Lap Time</span>
                  </div>
                </div>
              </div>

              <table className="runner-table">
                <thead>
                  <tr>
                    <th>Runner</th>
                    <th>Laps</th>
                    <th>Avg Lap Time</th>
                  </tr>
                </thead>
                <tbody>
                  {team.runners.map((runner) => (
                    <tr key={runner.id}>
                      <td>{runner.name}</td>
                      <td>{runner.lapsCompleted}</td>
                      <td>{formatLapTime(runner.avgLapTimeMillis)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ))}
      </section>
    </>
  )
}

export default App

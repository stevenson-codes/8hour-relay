import { teams, totalLaps, teamAvgLapTime } from './data/mockData'
import './App.css'

function App() {
  return (
    <>
      <header id="page-header">
        <h1>8 Hour Relay</h1>
        <p>Live standings — 3 teams, 8 runners each</p>
      </header>

      <section id="teams">
        {teams.map((team) => (
          <div className="team-card" key={team.id} style={{ '--team-color': team.color } as React.CSSProperties}>
            <div className="team-card-header">
              <h2>{team.name}</h2>
              <div className="team-stats">
                <div className="stat">
                  <span className="stat-value">{totalLaps(team)}</span>
                  <span className="stat-label">Total Laps</span>
                </div>
                <div className="stat">
                  <span className="stat-value">{teamAvgLapTime(team)}</span>
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
                    <td>{runner.laps}</td>
                    <td>{runner.avgLapTime}</td>
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

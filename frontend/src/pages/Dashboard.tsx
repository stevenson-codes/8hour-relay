import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import BoardHeader from "../components/BoardHeader";
import type {
  RunnerRef,
  RunnerBoardStatus,
  TeamBoard,
} from "../types/RaceBoard";
import { READ_ONLY } from "../config";
import "../App.css";

const TEAM_COLORS = [
  "#f0c419",
  "#e83f9c",
  "#2f8fe0",
  "#3ecf6e",
  "#aa3bff",
  "#ff8a3b",
];
const REFRESH_INTERVAL_MS = 1_000;

function RunnerIcon({ className }: { className?: string }) {
  return (
    <svg
      className={className}
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="currentColor"
      aria-hidden="true"
    >
      <circle cx="15.2" cy="4.4" r="2.3" />
      <path d="M17.4 10.2a1.6 1.6 0 0 0-1.4-.9l-4-.3-2.9 2.6a1 1 0 0 0 1.3 1.5l2.3-2.1 1.2 1.7-3.6 3.4-.6 5.6a1 1 0 0 0 2 .2l.7-4.9 2.3-2.1 1.1 3.7a1 1 0 0 0 1.9-.6zM8.9 13.9l-2.8 2.6a1 1 0 1 0 1.4 1.5l3.1-2.9z" />
    </svg>
  );
}

function statusClass(status: RunnerBoardStatus): string {
  return `status-${status.toLowerCase()}`;
}

function formatDistance(km: number): string {
  return `${km.toFixed(1)} km`;
}

function formatGap(km: number | null): string {
  if (km === null) return "—";
  return `${km.toFixed(1)} km`;
}

function formatLapMillis(millis: number | null): string {
  if (millis === null) return "—";
  const totalSeconds = Math.round(millis / 1000);
  const min = Math.floor(totalSeconds / 60);
  const sec = totalSeconds % 60;
  return `${min}:${sec.toString().padStart(2, "0")}`;
}

function formatPace(secPerKm: number | null): string {
  if (secPerKm === null) return "—";
  const min = Math.floor(secPerKm / 60);
  const sec = Math.round(secPerKm % 60);
  return `${min}:${sec.toString().padStart(2, "0")} /km`;
}

function formatTimeOfDay(iso: string | null): string {
  if (iso === null) return "—";
  return new Date(iso).toLocaleTimeString([], {
    hour: "numeric",
    minute: "2-digit",
  });
}

function RunnerRefBadge({ runner }: { runner: RunnerRef }) {
  return (
    <>
      <RunnerIcon className="runner-ref-icon" />
      <span className="runner-ref-leg">{runner.leg}</span>
      <span className="runner-ref-name">{runner.name}</span>
      {runner.bib && <span className="runner-ref-bib">{runner.bib}</span>}
      {runner.sex && <span className="runner-ref-sex">{runner.sex}</span>}
    </>
  );
}

function Dashboard() {
  const [now, setNow] = useState(() => new Date());
  const [teams, setTeams] = useState<TeamBoard[] | null>(null);
  const [boardError, setBoardError] = useState<string | null>(null);

  useEffect(() => {
    const tick = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(tick);
  }, []);

  const loadBoard = useCallback(() => {
    return fetch("/api/summary")
      .then((res) => {
        if (!res.ok) throw new Error(`Request failed: ${res.status}`);
        return res.json() as Promise<TeamBoard[]>;
      })
      .then((data) => {
        setTeams(data);
        setBoardError(null);
      })
      .catch((err: Error) => setBoardError(err.message));
  }, []);

  useEffect(() => {
    loadBoard();
    const intervalId = setInterval(loadBoard, REFRESH_INTERVAL_MS);
    return () => clearInterval(intervalId);
  }, [loadBoard]);

  return (
    <div className="board">
      <BoardHeader
        title="BMAI Vancouver Runaholics 8-Hour Relay 2026"
        now={now}
        currentPage="dashboard"
        boardError={boardError}
        onBoardRefresh={loadBoard}
      />

      <main className="teams">
        {teams === null && !boardError && (
          <p className="board-status">Loading teams…</p>
        )}
        {teams !== null && teams.length === 0 && (
          <p className="board-status">No teams found.</p>
        )}
        {teams?.map((team, i) => (
          <section
            className="team-card"
            key={team.id}
            style={
              {
                "--team-color": TEAM_COLORS[i % TEAM_COLORS.length],
              } as React.CSSProperties
            }
          >
            <div className="team-card-top">
              <span className="team-rank">{team.overallRank}</span>
              <div className="team-heading">
                <h2>{team.name}</h2>
                <span className="team-epc">{team.epcHex}</span>
                <span className="team-division">{team.division ?? "—"}</span>
              </div>

              <div className="team-stat">
                <span className="team-stat-label">Overall Rank</span>
                <span className="team-stat-value">{team.overallRank}</span>
              </div>
              <div className="team-stat">
                <span className="team-stat-label">Division Rank</span>
                <span className="team-stat-value">{team.divisionRank}</span>
              </div>
              <div className="team-stat">
                <span className="team-stat-label">Total Laps</span>
                <span className="team-stat-value">{team.totalLaps}</span>
              </div>
              <div className="team-stat">
                <span className="team-stat-label">Total Distance</span>
                <span className="team-stat-value accent">
                  {formatDistance(team.totalDistanceKm)}
                </span>
              </div>
              <div className="team-stat">
                <span className="team-stat-label">Gap To Leader</span>
                <span className="team-stat-value">
                  {formatGap(team.gapToLeaderKm)}
                </span>
              </div>

              {!READ_ONLY && (
                <Link
                  to={`/teams/${team.id}`}
                  className="nav-link-button team-edit-link"
                >
                  Edit
                </Link>
              )}
            </div>

            <div className="team-card-current">
              <div className="current-field">
                <span className="current-label">Current Runner</span>
                <span className="current-value runner-ref">
                  {team.currentRunner ? (
                    <RunnerRefBadge runner={team.currentRunner} />
                  ) : (
                    "—"
                  )}
                </span>
              </div>
              <div className="current-field">
                <span className="current-label">Start Time (This Leg)</span>
                <span className="current-value">
                  {formatTimeOfDay(team.startTimeThisLeg)}
                </span>
              </div>
              <div className="current-field">
                <span className="current-label">Next Runner</span>
                <span className="current-value runner-ref">
                  {team.nextRunner ? (
                    <RunnerRefBadge runner={team.nextRunner} />
                  ) : (
                    "—"
                  )}
                </span>
              </div>
              <div className="current-field">
                <span className="current-label">Team Last Lap</span>
                <span className="current-value">
                  {formatLapMillis(team.teamLastLapMillis)}
                </span>
              </div>
              <div className="current-field">
                <span className="current-label">Avg Pace</span>
                <span className="current-value">
                  {formatPace(team.avgPaceSecPerKm)}
                </span>
              </div>
            </div>

            <div className="runner-table-wrap">
              <table className="runner-table">
                <thead>
                  <tr>
                    <th>Leg</th>
                    <th>Runner</th>
                    <th>Bib</th>
                    <th>Sex</th>
                    <th>Status</th>
                    <th>Laps</th>
                    <th>Distance</th>
                    <th>Last Lap</th>
                    <th>Best Lap</th>
                    <th>Avg Pace</th>
                    <th>Leg Start</th>
                    <th>Leg End</th>
                  </tr>
                </thead>
                <tbody>
                  {team.runners.map((runner) => (
                    <tr key={runner.leg} className={statusClass(runner.status)}>
                      <td>{runner.leg}</td>
                      <td>{runner.name}</td>
                      <td>{runner.bib ?? "—"}</td>
                      <td>{runner.sex ?? "—"}</td>
                      <td className="status-cell">{runner.statusLabel}</td>
                      <td>{runner.laps}</td>
                      <td>{formatDistance(runner.distanceKm)}</td>
                      <td>{formatLapMillis(runner.lastLapMillis)}</td>
                      <td>{formatLapMillis(runner.bestLapMillis)}</td>
                      <td>{formatPace(runner.avgPaceSecPerKm)}</td>
                      <td>{formatTimeOfDay(runner.legStart)}</td>
                      <td>{formatTimeOfDay(runner.legEnd)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        ))}
      </main>
    </div>
  );
}

export default Dashboard;

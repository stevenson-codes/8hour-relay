import { useCallback, useEffect, useMemo, useState } from "react";
import BoardHeader from "../components/BoardHeader";
import RaceStatsBar from "../components/RaceStatsBar";
import type { Division, RunnerRef, TeamBoard } from "../types/RaceBoard";
import type { RaceStatus } from "../types/RaceStatus";
import "../App.css";
import "./LeaderboardPage.css";

const SUMMARY_REFRESH_MS = 2_000;
const MAX_ROWS = 20;

const MODES = ["OVERALL", "OPEN", "MIXED", "MASTERS"] as const;
type Mode = (typeof MODES)[number];

const DIVISION_CLASS: Record<Division, string> = {
  OPEN: "division-open",
  MIXED: "division-mixed",
  MASTERS: "division-masters",
};

function RefreshIcon() {
  return (
    <svg
      width="14"
      height="14"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M20 11a8 8 0 1 0-2.3 5.7M20 5v6h-6"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function RunnerIcon({ className }: { className?: string }) {
  return (
    <svg
      className={className}
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="currentColor"
      aria-hidden="true"
    >
      <circle cx="15.2" cy="4.4" r="2.3" />
      <path d="M17.4 10.2a1.6 1.6 0 0 0-1.4-.9l-4-.3-2.9 2.6a1 1 0 0 0 1.3 1.5l2.3-2.1 1.2 1.7-3.6 3.4-.6 5.6a1 1 0 0 0 2 .2l.7-4.9 2.3-2.1 1.1 3.7a1 1 0 0 0 1.9-.6zM8.9 13.9l-2.8 2.6a1 1 0 1 0 1.4 1.5l3.1-2.9z" />
    </svg>
  );
}

function formatLapMillis(millis: number | null): string {
  if (millis === null) return "—";
  const totalSeconds = Math.round(millis / 1000);
  const min = Math.floor(totalSeconds / 60);
  const sec = totalSeconds % 60;
  return `${min}:${sec.toString().padStart(2, "0")}`;
}

function formatGap(km: number | null): string {
  if (km === null) return "—";
  return `${km.toFixed(1)} km`;
}

function formatTimeOfDay(iso: string | null): string {
  if (iso === null) return "—";
  return new Date(iso).toLocaleTimeString([], {
    hour: "numeric",
    minute: "2-digit",
  });
}

function CurrentRunnerCell({ runner }: { runner: RunnerRef | null }) {
  if (!runner) return <span className="runner-cell-empty">—</span>;
  return (
    <span className="runner-cell">
      <RunnerIcon className="runner-cell-icon" />
      <span className="runner-cell-leg">{runner.leg}</span>
      <span className="runner-cell-name">{runner.name}</span>
    </span>
  );
}

function LeaderboardPage() {
  const [now, setNow] = useState(() => new Date());
  const [teams, setTeams] = useState<TeamBoard[] | null>(null);
  const [boardError, setBoardError] = useState<string | null>(null);
  const [raceStatus, setRaceStatus] = useState<RaceStatus | null>(null);

  const [mode, setMode] = useState<Mode>("OVERALL");

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
    const intervalId = setInterval(loadBoard, SUMMARY_REFRESH_MS);
    return () => clearInterval(intervalId);
  }, [loadBoard]);

  const visibleTeams = useMemo(() => {
    if (!teams) return [];
    const filtered =
      mode === "OVERALL" ? teams : teams.filter((t) => t.division === mode);
    const sorted = [...filtered].sort((a, b) =>
      mode === "OVERALL"
        ? a.overallRank - b.overallRank
        : a.divisionRank - b.divisionRank,
    );
    return sorted;
  }, [teams, mode]);

  const shownTeams = visibleTeams.slice(0, MAX_ROWS);

  return (
    <div className="board leaderboard">
      <BoardHeader
        title="BMAI Vancouver Runaholics 8-Hour Relay 2026"
        now={now}
        currentPage="leaderboard"
        onBoardRefresh={loadBoard}
        onRaceStatusChange={setRaceStatus}
      />

      <RaceStatsBar now={now} raceStatus={raceStatus} />

      <div className="leaderboard-table-header">
        <h2>
          {mode === "OVERALL"
            ? "Overall"
            : mode.charAt(0) + mode.slice(1).toLowerCase()}{" "}
          Standings
        </h2>
        {boardError ? (
          <span className="controls-error">{boardError}</span>
        ) : (
          <span className="leaderboard-showing">
            Showing 1 – {shownTeams.length} of {visibleTeams.length} Teams
          </span>
        )}
        <span className="leaderboard-refresh">
          <RefreshIcon />
          Auto refresh: {SUMMARY_REFRESH_MS / 1000} sec
        </span>
      </div>

      <div className="leaderboard-table-wrap">
        <table className="leaderboard-table">
          <thead>
            <tr>
              <th>Rank</th>
              <th>Team</th>
              <th>Division</th>
              <th>Current Runner</th>
              <th>Bib</th>
              <th>Sex</th>
              <th>Start Time (This Leg)</th>
              <th>Laps (400m)</th>
              <th>Distance (km)</th>
              <th>Last Lap</th>
              <th>Gap To Leader</th>
            </tr>
          </thead>
          <tbody>
            {teams === null && !boardError && (
              <tr>
                <td colSpan={11} className="leaderboard-status-row">
                  Loading teams…
                </td>
              </tr>
            )}
            {teams !== null && shownTeams.length === 0 && (
              <tr>
                <td colSpan={11} className="leaderboard-status-row">
                  No teams found.
                </td>
              </tr>
            )}
            {shownTeams.map((team) => (
              <tr key={team.id}>
                <td
                  className={
                    team.overallRank === 1
                      ? "leaderboard-rank rank-leader"
                      : "leaderboard-rank"
                  }
                >
                  {mode === "OVERALL" ? team.overallRank : team.divisionRank}
                </td>
                <td className="leaderboard-team-name">{team.name}</td>
                <td
                  className={team.division ? DIVISION_CLASS[team.division] : ""}
                >
                  {team.division ?? "—"}
                </td>
                <td>
                  <CurrentRunnerCell runner={team.currentRunner} />
                </td>
                <td>{team.currentRunner?.bib ?? "—"}</td>
                <td>{team.currentRunner?.sex ?? "—"}</td>
                <td>{formatTimeOfDay(team.startTimeThisLeg)}</td>
                <td>{team.totalLaps}</td>
                <td className="leaderboard-distance">
                  {team.totalDistanceKm.toFixed(1)} km
                </td>
                <td>{formatLapMillis(team.teamLastLapMillis)}</td>
                <td>{formatGap(team.gapToLeaderKm)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <footer className="leaderboard-footer">
        <div className="leaderboard-mode">
          <span className="leaderboard-mode-label">Display Mode</span>
          {MODES.map((m) => (
            <button
              key={m}
              type="button"
              className={
                m === mode
                  ? "leaderboard-mode-tab active"
                  : "leaderboard-mode-tab"
              }
              onClick={() => setMode(m)}
            >
              {m}
            </button>
          ))}
        </div>
      </footer>
    </div>
  );
}

export default LeaderboardPage;

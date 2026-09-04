import { useCallback, useEffect, useMemo, useState } from "react";
import BoardHeader from "../components/BoardHeader";
import type { Division, RunnerRef, TeamBoard } from "../types/RaceBoard";
import type { RaceStatus } from "../types/RaceStatus";
import "../App.css";
import "./LeaderboardPage.css";

const SUMMARY_REFRESH_MS = 2_000;
const RACE_DURATION_MS = 8 * 60 * 60 * 1000;
const MAX_ROWS = 20;

const MODES = ["OVERALL", "OPEN", "MIXED", "MASTERS"] as const;
type Mode = (typeof MODES)[number];

const DIVISION_CLASS: Record<Division, string> = {
  OPEN: "division-open",
  MIXED: "division-mixed",
  MASTERS: "division-masters",
};

function ClockIcon() {
  return (
    <svg
      width="22"
      height="22"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.6" />
      <path
        d="M12 7v5l3.5 2"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function HourglassIcon() {
  return (
    <svg
      width="22"
      height="22"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M6 3h12M6 21h12M7 3c0 5 5 6 5 9s-5 4-5 9M17 3c0 5-5 6-5 9s5 4 5 9"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  );
}

function FlagIcon() {
  return (
    <svg
      width="22"
      height="22"
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
    >
      <path
        d="M5 21V4"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
      />
      <path
        d="M5 4h13l-3 4 3 4H5"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinejoin="round"
        strokeLinecap="round"
      />
    </svg>
  );
}

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

function pad2(n: number): string {
  return n.toString().padStart(2, "0");
}

function formatDuration(ms: number | null): string {
  if (ms === null) return "—";
  const totalSeconds = Math.max(0, Math.floor(ms / 1000));
  const h = Math.floor(totalSeconds / 3600);
  const m = Math.floor((totalSeconds % 3600) / 60);
  const s = totalSeconds % 60;
  return `${pad2(h)}:${pad2(m)}:${pad2(s)}`;
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
      <span className="runner-cell-leg">R{runner.leg}</span>
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

  const raceActive = raceStatus?.active ?? false;
  const elapsedMs =
    raceActive && raceStatus?.startedAt
      ? now.getTime() - new Date(raceStatus.startedAt).getTime()
      : null;
  const remainingMs = elapsedMs === null ? null : RACE_DURATION_MS - elapsedMs;

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

      <div className="leaderboard-stats">
        <div className="leaderboard-stat">
          <span className="leaderboard-stat-icon cyan">
            <ClockIcon />
          </span>
          <span className="leaderboard-stat-body">
            <span className="leaderboard-stat-label">Race Time</span>
            <span className="leaderboard-stat-value">
              {formatDuration(elapsedMs)}
            </span>
          </span>
        </div>
        <div className="leaderboard-stat">
          <span className="leaderboard-stat-icon amber">
            <HourglassIcon />
          </span>
          <span className="leaderboard-stat-body">
            <span className="leaderboard-stat-label">Time Remaining</span>
            <span className="leaderboard-stat-value amber">
              {formatDuration(remainingMs)}
            </span>
          </span>
        </div>
        <div className="leaderboard-stat">
          <span className={`leaderboard-stat-icon ${raceActive ? "green" : "red"}`}>
            <FlagIcon />
          </span>
          <span className="leaderboard-stat-body">
            <span className="leaderboard-stat-label">Race Status</span>
            <span
              className={`leaderboard-stat-value ${raceActive ? "green" : "red"}`}
            >
              {raceActive ? "Racing" : "Stopped"}
            </span>
          </span>
        </div>
        <div className="leaderboard-stat">
          <span className="leaderboard-stat-icon cyan">
            <ClockIcon />
          </span>
          <span className="leaderboard-stat-body">
            <span className="leaderboard-stat-label">Local Time</span>
            <span className="leaderboard-stat-value">
              {now.toLocaleTimeString([], {
                hour: "numeric",
                minute: "2-digit",
              })}
            </span>
          </span>
        </div>
      </div>

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
                m === mode ? "leaderboard-mode-tab active" : "leaderboard-mode-tab"
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

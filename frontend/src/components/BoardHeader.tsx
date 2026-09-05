import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import type { RaceStatus } from "../types/RaceStatus";
import { READ_ONLY } from "../config";
import "../App.css";

const RACE_STATUS_REFRESH_MS = 5_000;

interface BoardHeaderProps {
  title: string;
  subtitle?: string;
  now: Date;
  currentPage: "dashboard" | "leaderboard";
  boardError?: string | null;
  onBoardRefresh: () => void;
  onRaceStatusChange?: (status: RaceStatus) => void;
}

function BoardHeader({
  title,
  subtitle,
  now,
  currentPage,
  boardError,
  onBoardRefresh,
  onRaceStatusChange,
}: BoardHeaderProps) {
  const [raceStatus, setRaceStatus] = useState<RaceStatus | null>(null);
  const [raceActionPending, setRaceActionPending] = useState(false);
  const [clearPending, setClearPending] = useState(false);
  const [wipePending, setWipePending] = useState(false);
  const [controlsError, setControlsError] = useState<string | null>(null);

  const raceActive = raceStatus?.active ?? null;

  const loadRaceStatus = useCallback(() => {
    return fetch("/api/race/status")
      .then((res) => {
        if (!res.ok) throw new Error(`Request failed: ${res.status}`);
        return res.json() as Promise<RaceStatus>;
      })
      .then((status) => {
        setRaceStatus(status);
        setControlsError(null);
        onRaceStatusChange?.(status);
      })
      .catch((err: Error) => setControlsError(err.message));
  }, [onRaceStatusChange]);

  useEffect(() => {
    loadRaceStatus();
    const intervalId = setInterval(loadRaceStatus, RACE_STATUS_REFRESH_MS);
    return () => clearInterval(intervalId);
  }, [loadRaceStatus]);

  const handleRaceToggle = useCallback(() => {
    if (raceActive === null || raceActionPending) return;

    const endpoint = raceActive ? "/api/race/stop" : "/api/race/start";
    setRaceActionPending(true);
    fetch(endpoint, { method: "POST" })
      .then((res) => {
        if (!res.ok) throw new Error(`Request failed: ${res.status}`);
        return res.json() as Promise<RaceStatus>;
      })
      .then((status) => {
        setRaceStatus(status);
        setControlsError(null);
        onRaceStatusChange?.(status);
      })
      .catch((err: Error) => setControlsError(err.message))
      .finally(() => setRaceActionPending(false));
  }, [raceActive, raceActionPending, onRaceStatusChange]);

  const handleClearLapRecords = useCallback(() => {
    if (raceActive !== false || clearPending) return;
    if (
      !window.confirm("This will permanently delete all lap records. Continue?")
    )
      return;

    setClearPending(true);
    fetch("/api/lap-records", { method: "DELETE" })
      .then((res) => {
        if (!res.ok) throw new Error(`Request failed: ${res.status}`);
        setControlsError(null);
        onBoardRefresh();
      })
      .catch((err: Error) => setControlsError(err.message))
      .finally(() => setClearPending(false));
  }, [raceActive, clearPending, onBoardRefresh]);

  const handleWipeDatabase = useCallback(() => {
    if (raceActive !== false || wipePending) return;
    if (
      !window.confirm(
        "This will permanently delete all teams, runners, tags, and lap records. Continue?",
      )
    )
      return;

    setWipePending(true);
    fetch("/api/teams", { method: "DELETE" })
      .then((res) => {
        if (!res.ok) throw new Error(`Request failed: ${res.status}`);
        setControlsError(null);
        onBoardRefresh();
      })
      .catch((err: Error) => setControlsError(err.message))
      .finally(() => setWipePending(false));
  }, [raceActive, wipePending, onBoardRefresh]);

  return (
    <header className="board-header">
      <div className="board-header-top">
        <div className="brand">
          <svg
            className="brand-mark"
            width="34"
            height="34"
            viewBox="0 0 24 24"
            fill="none"
            aria-hidden="true"
          >
            <path
              d="M2 18 8 8l3.2 5L14 9l8 9z"
              stroke="currentColor"
              strokeWidth="1.6"
              strokeLinejoin="round"
            />
          </svg>
          <span className="brand-name">
            VANCOUVER
            <br />
            RUNAHOLICS
          </span>
        </div>

        <div className="event-title">
          <h1>{title}</h1>
          {subtitle && <p>{subtitle}</p>}
        </div>

        <div className="header-right">
          <span className="clock">
            {now.toLocaleTimeString([], {
              hour: "numeric",
              minute: "2-digit",
              second: "2-digit",
            })}
          </span>
          <span className="live-indicator">
            <span className="live-dot" />
            Live
          </span>
        </div>
      </div>

      <div className="board-controls">
        {(boardError || controlsError) && (
          <span className="controls-error">
            {boardError ?? controlsError}
          </span>
        )}
        {!READ_ONLY && (
          <Link to="/add-team" className="nav-link-button">
            Add Team
          </Link>
        )}
        {currentPage !== "dashboard" && (
          <Link to="/" className="nav-link-button nav-toggle">
            Dashboard
          </Link>
        )}
        {currentPage !== "leaderboard" && (
          <Link to="/leaderboard" className="nav-link-button nav-toggle">
            Leaderboard
          </Link>
        )}
        {!READ_ONLY && (
          <>
            <button
              type="button"
              className={raceActive ? "stop-button" : "start-button"}
              onClick={handleRaceToggle}
              disabled={raceActive === null || raceActionPending}
            >
              {raceActive === null
                ? "Loading…"
                : raceActive
                  ? "Stop Race"
                  : "Start Race"}
            </button>
            <button
              type="button"
              className="clear-button"
              onClick={handleClearLapRecords}
              disabled={raceActive !== false || clearPending}
              title={
                raceActive !== false
                  ? "Stop the race before clearing lap records"
                  : undefined
              }
            >
              Clear Lap Records
            </button>
            <button
              type="button"
              className="clear-button"
              onClick={handleWipeDatabase}
              disabled={raceActive !== false || wipePending}
              title={
                raceActive !== false
                  ? "Stop the race before wiping the database"
                  : undefined
              }
            >
              Clear All
            </button>
          </>
        )}
      </div>
    </header>
  );
}

export default BoardHeader;

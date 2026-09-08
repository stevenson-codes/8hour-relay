import type { RaceStatus } from "../types/RaceStatus";
import "./RaceStatsBar.css";

const RACE_DURATION_MS = 8 * 60 * 60 * 1000;

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

interface RaceStatsBarProps {
  now: Date;
  raceStatus: RaceStatus | null;
}

function RaceStatsBar({ now, raceStatus }: RaceStatsBarProps) {
  const raceActive = raceStatus?.active ?? false;
  const elapsedMs =
    raceActive && raceStatus?.startedAt
      ? now.getTime() - new Date(raceStatus.startedAt).getTime()
      : null;
  const remainingMs = elapsedMs === null ? null : RACE_DURATION_MS - elapsedMs;

  return (
    <div className="race-stats">
      <div className="race-stat">
        <span className="race-stat-icon cyan">
          <ClockIcon />
        </span>
        <span className="race-stat-body">
          <span className="race-stat-label">Race Time</span>
          <span className="race-stat-value">{formatDuration(elapsedMs)}</span>
        </span>
      </div>
      <div className="race-stat">
        <span className="race-stat-icon amber">
          <HourglassIcon />
        </span>
        <span className="race-stat-body">
          <span className="race-stat-label">Time Remaining</span>
          <span className="race-stat-value amber">
            {formatDuration(remainingMs)}
          </span>
        </span>
      </div>
      <div className="race-stat">
        <span
          className={`race-stat-icon ${raceActive ? "green" : "red"}`}
        >
          <FlagIcon />
        </span>
        <span className="race-stat-body">
          <span className="race-stat-label">Race Status</span>
          <span
            className={`race-stat-value ${raceActive ? "green" : "red"}`}
          >
            {raceActive ? "Racing" : "Stopped"}
          </span>
        </span>
      </div>
      <div className="race-stat">
        <span className="race-stat-icon cyan">
          <ClockIcon />
        </span>
        <span className="race-stat-body">
          <span className="race-stat-label">Local Time</span>
          <span className="race-stat-value">
            {now.toLocaleTimeString([], {
              hour: "numeric",
              minute: "2-digit",
            })}
          </span>
        </span>
      </div>
    </div>
  );
}

export default RaceStatsBar;

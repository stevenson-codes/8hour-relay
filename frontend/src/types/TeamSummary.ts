import type { RunnerSummary } from './RunnerSummary'

export interface TeamSummary {
  id: number
  name: string
  totalLaps: number
  avgLapTimeMillis: number | null
  runners: RunnerSummary[]
}

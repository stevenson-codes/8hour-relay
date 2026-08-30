export interface RunnerSummary {
  id: number
  name: string
  leg: number
  status: string
  lapsCompleted: number
  avgLapTimeMillis: number | null
}

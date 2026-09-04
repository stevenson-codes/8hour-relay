export type Sex = 'M' | 'F'
export type Division = 'OPEN' | 'MASTERS' | 'MIXED'

export interface RunnerRef {
  leg: number
  name: string
  bib: string | null
  sex: Sex | null
}

export type RunnerBoardStatus = 'RUNNING' | 'NEXT' | 'COMPLETED' | 'WAITING'

export interface RunnerBoardRow {
  leg: number
  name: string
  bib: string | null
  sex: Sex | null
  status: RunnerBoardStatus
  statusLabel: string
  laps: number
  distanceKm: number
  lastLapMillis: number | null
  bestLapMillis: number | null
  avgPaceSecPerKm: number | null
  legStart: string | null
  legEnd: string | null
}

export interface TeamBoard {
  id: number
  name: string
  epcHex: string
  division: Division | null
  overallRank: number
  divisionRank: number
  totalLaps: number
  totalDistanceKm: number
  gapToLeaderKm: number | null
  currentRunner: RunnerRef | null
  startTimeThisLeg: string | null
  nextRunner: RunnerRef | null
  teamLastLapMillis: number | null
  avgPaceSecPerKm: number | null
  runners: RunnerBoardRow[]
}

export type Division = 'OPEN' | 'MASTERS' | 'MIXED'

export const DIVISION_OPTIONS: { value: Division; label: string }[] = [
  { value: 'OPEN', label: 'Open' },
  { value: 'MASTERS', label: 'Masters' },
  { value: 'MIXED', label: 'Mixed' },
]

export const SEX_OPTIONS: { value: 'M' | 'F'; label: string }[] = [
  { value: 'M', label: 'M' },
  { value: 'F', label: 'F' },
]

export interface CreateTeamRequest {
  name: string
  division: Division | null
  epcHex: string
}

export interface CreateTeamResponse {
  id: number
  name: string
  division: Division | null
  epcHex: string
}

export interface CreateRunnerRequest {
  name: string
  leg: number
  bib: string | null
  sex: 'M' | 'F' | null
  epcHex: string
}

export interface CreateRunnerResponse {
  id: number
  name: string
  leg: number
  bib: string | null
  sex: 'M' | 'F' | null
  status: 'ACTIVE' | 'INACTIVE'
  teamId: number
}

export interface RunnerResponse {
  id: number
  name: string
  leg: number
  bib: string | null
  sex: 'M' | 'F' | null
  epcHex: string
  status: 'ACTIVE' | 'INACTIVE'
  teamId: number
}

export interface TeamDetailResponse {
  id: number
  name: string
  division: Division | null
  epcHex: string
  runners: RunnerResponse[]
}

export interface ApiError {
  error: string
}

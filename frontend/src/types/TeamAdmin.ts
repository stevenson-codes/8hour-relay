export interface CreateTeamRequest {
  name: string
  division: string | null
  epcHex: string
}

export interface CreateTeamResponse {
  id: number
  name: string
  division: string | null
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
  division: string | null
  epcHex: string
  runners: RunnerResponse[]
}

export interface ApiError {
  error: string
}

export interface Runner {
  id: string
  name: string
  laps: number
  avgLapTime: string // mm:ss
}

export interface Team {
  id: string
  name: string
  color: string
  runners: Runner[]
}

const runnerNames = [
  'Alex Rivera',
  'Jordan Lee',
  'Sam Patel',
  'Casey Kim',
  'Morgan Diaz',
  'Taylor Brooks',
  'Jamie Chen',
  'Riley Nguyen',
]

function makeRunners(prefix: string): Runner[] {
  return runnerNames.map((name, i) => ({
    id: `${prefix}-${i + 1}`,
    name,
    laps: [4, 3, 5, 2, 4, 3, 6, 4][i],
    avgLapTime: ['9:42', '10:15', '8:57', '11:03', '9:28', '10:41', '8:12', '9:55'][i],
  }))
}

export const teams: Team[] = [
  {
    id: 'team-a',
    name: 'Trailblazers',
    color: '#aa3bff',
    runners: makeRunners('a'),
  },
  {
    id: 'team-b',
    name: 'Night Owls',
    color: '#3ba7ff',
    runners: makeRunners('b'),
  },
  {
    id: 'team-c',
    name: 'Endurance Elite',
    color: '#ff8a3b',
    runners: makeRunners('c'),
  },
]

function lapTimeToSeconds(lapTime: string): number {
  const [min, sec] = lapTime.split(':').map(Number)
  return min * 60 + sec
}

function secondsToLapTime(totalSeconds: number): string {
  const min = Math.floor(totalSeconds / 60)
  const sec = Math.round(totalSeconds % 60)
  return `${min}:${sec.toString().padStart(2, '0')}`
}

export function totalLaps(team: Team): number {
  return team.runners.reduce((sum, r) => sum + r.laps, 0)
}

export function teamAvgLapTime(team: Team): string {
  const totalSeconds = team.runners.reduce(
    (sum, r) => sum + lapTimeToSeconds(r.avgLapTime) * r.laps,
    0,
  )
  const laps = totalLaps(team)
  return laps === 0 ? '0:00' : secondsToLapTime(totalSeconds / laps)
}

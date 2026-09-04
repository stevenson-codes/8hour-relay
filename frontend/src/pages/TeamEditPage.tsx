import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  DIVISION_OPTIONS,
  SEX_OPTIONS,
  type ApiError,
  type CreateRunnerResponse,
  type Division,
  type RunnerResponse,
  type TeamDetailResponse,
} from '../types/TeamAdmin'
import Dropdown from '../components/Dropdown'
import '../App.css'
import './AddTeamPage.css'
import './TeamEditPage.css'

async function apiRequest<T>(url: string, method: string, body?: unknown): Promise<T> {
  const res = await fetch(url, {
    method,
    headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  if (res.status === 204) {
    return undefined as T
  }
  const data = await res.json()
  if (!res.ok) {
    throw new Error((data as ApiError).error ?? `Request failed: ${res.status}`)
  }
  return data as T
}

interface RunnerFormState {
  name: string
  leg: string
  bib: string
  sex: 'M' | 'F' | ''
  epcHex: string
}

const emptyRunnerForm: RunnerFormState = { name: '', leg: '1', bib: '', sex: '', epcHex: '' }

function runnerFormValid(form: RunnerFormState): boolean {
  return Boolean(form.name.trim() && form.leg.trim() && form.bib.trim() && form.sex.trim() && form.epcHex.trim())
}

function TeamEditPage() {
  const { teamId } = useParams<{ teamId: string }>()
  const navigate = useNavigate()

  const [team, setTeam] = useState<TeamDetailResponse | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)

  const [teamName, setTeamName] = useState('')
  const [teamDivision, setTeamDivision] = useState<Division | ''>('')
  const [teamEpcHex, setTeamEpcHex] = useState('')
  const [teamSaving, setTeamSaving] = useState(false)
  const [teamDeleting, setTeamDeleting] = useState(false)
  const [teamError, setTeamError] = useState<string | null>(null)

  const [editingRunnerId, setEditingRunnerId] = useState<number | null>(null)
  const [editRunnerForm, setEditRunnerForm] = useState<RunnerFormState>(emptyRunnerForm)
  const [runnerSaving, setRunnerSaving] = useState(false)
  const [runnerDeletingId, setRunnerDeletingId] = useState<number | null>(null)
  const [runnerStatusUpdatingId, setRunnerStatusUpdatingId] = useState<number | null>(null)
  const [runnerRowError, setRunnerRowError] = useState<string | null>(null)

  const [addRunnerForm, setAddRunnerForm] = useState<RunnerFormState>(emptyRunnerForm)
  const [addRunnerPending, setAddRunnerPending] = useState(false)
  const [addRunnerError, setAddRunnerError] = useState<string | null>(null)

  const loadTeam = useCallback(() => {
    if (!teamId) return Promise.resolve()
    return apiRequest<TeamDetailResponse>(`/api/teams/${teamId}`, 'GET')
      .then((data) => {
        setTeam(data)
        setTeamName(data.name)
        setTeamDivision(data.division ?? '')
        setTeamEpcHex(data.epcHex)
        setLoadError(null)
      })
      .catch((err: Error) => setLoadError(err.message))
  }, [teamId])

  useEffect(() => {
    loadTeam()
  }, [loadTeam])

  function handleSaveTeam(e: FormEvent) {
    e.preventDefault()
    if (!teamId || teamSaving) return
    if (!teamName.trim() || !teamDivision.trim() || !teamEpcHex.trim()) {
      setTeamError('Please fill out all required fields.')
      return
    }

    setTeamSaving(true)
    setTeamError(null)
    apiRequest(`/api/teams/${teamId}`, 'PUT', { name: teamName, division: teamDivision, epcHex: teamEpcHex })
      .then(() => loadTeam())
      .catch((err: Error) => setTeamError(err.message))
      .finally(() => setTeamSaving(false))
  }

  function handleDeleteTeam() {
    if (!teamId || teamDeleting) return
    if (!window.confirm('This will permanently delete this team and all of its runners. Continue?')) return

    setTeamDeleting(true)
    setTeamError(null)
    apiRequest(`/api/teams/${teamId}`, 'DELETE')
      .then(() => navigate('/'))
      .catch((err: Error) => {
        setTeamError(err.message)
        setTeamDeleting(false)
      })
  }

  function startEditRunner(runner: RunnerResponse) {
    setEditingRunnerId(runner.id)
    setRunnerRowError(null)
    setEditRunnerForm({
      name: runner.name,
      leg: String(runner.leg),
      bib: runner.bib ?? '',
      sex: runner.sex ?? '',
      epcHex: runner.epcHex,
    })
  }

  function cancelEditRunner() {
    setEditingRunnerId(null)
    setRunnerRowError(null)
  }

  function handleSaveRunner(runnerId: number) {
    if (!teamId || runnerSaving) return
    if (!runnerFormValid(editRunnerForm)) {
      setRunnerRowError('Please fill out all required fields.')
      return
    }

    setRunnerSaving(true)
    setRunnerRowError(null)
    apiRequest(`/api/teams/${teamId}/runners/${runnerId}`, 'PUT', {
      name: editRunnerForm.name,
      leg: Number(editRunnerForm.leg),
      bib: editRunnerForm.bib,
      sex: editRunnerForm.sex,
      epcHex: editRunnerForm.epcHex,
    })
      .then(() => {
        setEditingRunnerId(null)
        return loadTeam()
      })
      .catch((err: Error) => setRunnerRowError(err.message))
      .finally(() => setRunnerSaving(false))
  }

  function handleSetRunnerStatus(runnerId: number, status: 'ACTIVE' | 'INACTIVE') {
    if (!teamId || runnerStatusUpdatingId !== null) return

    setRunnerStatusUpdatingId(runnerId)
    setRunnerRowError(null)
    apiRequest(`/api/teams/${teamId}/runners/${runnerId}/status`, 'PATCH', { status })
      .then(() => loadTeam())
      .catch((err: Error) => setRunnerRowError(err.message))
      .finally(() => setRunnerStatusUpdatingId(null))
  }

  function handleDeleteRunner(runnerId: number) {
    if (!teamId || runnerDeletingId !== null) return
    if (!window.confirm('This will permanently delete this runner. Continue?')) return

    setRunnerDeletingId(runnerId)
    setRunnerRowError(null)
    apiRequest(`/api/teams/${teamId}/runners/${runnerId}`, 'DELETE')
      .then(() => loadTeam())
      .catch((err: Error) => setRunnerRowError(err.message))
      .finally(() => setRunnerDeletingId(null))
  }

  function handleAddRunner(e: FormEvent) {
    e.preventDefault()
    if (!teamId || addRunnerPending) return
    if (!runnerFormValid(addRunnerForm)) {
      setAddRunnerError('Please fill out all required fields.')
      return
    }

    setAddRunnerPending(true)
    setAddRunnerError(null)
    apiRequest<CreateRunnerResponse>(`/api/teams/${teamId}/runners`, 'POST', {
      name: addRunnerForm.name,
      leg: Number(addRunnerForm.leg),
      bib: addRunnerForm.bib,
      sex: addRunnerForm.sex,
      epcHex: addRunnerForm.epcHex,
    })
      .then((created) => {
        setAddRunnerForm({ ...emptyRunnerForm, leg: String(created.leg + 1) })
        return loadTeam()
      })
      .catch((err: Error) => setAddRunnerError(err.message))
      .finally(() => setAddRunnerPending(false))
  }

  return (
    <div className="board">
      <header className="board-header">
        <div className="brand">
          <svg className="brand-mark" width="34" height="34" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path d="M2 18 8 8l3.2 5L14 9l8 9z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
          </svg>
          <span className="brand-name">
            VANCOUVER
            <br />
            RUNAHOLICS
          </span>
        </div>

        <div className="event-title">
          <h1>Edit Team</h1>
          <p>Race Setup</p>
        </div>

        <div className="header-right">
          <div className="board-controls">
            <Link to="/add-team" className="nav-link-button">
              Add Team
            </Link>
            <Link to="/" className="nav-link-button">
              ← Back to Standings
            </Link>
          </div>
        </div>
      </header>

      <main className="add-team-page">
        {loadError && (
          <section className="form-card">
            <p className="form-error">{loadError}</p>
          </section>
        )}

        {!loadError && team === null && (
          <section className="form-card">
            <p className="board-status">Loading team…</p>
          </section>
        )}

        {team && (
          <>
            <section className="form-card">
              <h2>Team Details</h2>
              <form onSubmit={handleSaveTeam} className="admin-form" noValidate>
                <label className="form-field">
                  <span>Team Name</span>
                  <input type="text" value={teamName} onChange={(e) => setTeamName(e.target.value)} required />
                </label>
                <label className="form-field">
                  <span>Division</span>
                  <Dropdown value={teamDivision} onChange={setTeamDivision} options={DIVISION_OPTIONS} />
                </label>
                <label className="form-field">
                  <span>Team Tag EPC Hex</span>
                  <input type="text" value={teamEpcHex} onChange={(e) => setTeamEpcHex(e.target.value)} required />
                </label>
                {teamError && <p className="form-error">{teamError}</p>}
                <div className="edit-actions">
                  <button type="submit" className="start-button" disabled={teamSaving}>
                    {teamSaving ? 'Saving…' : 'Save Changes'}
                  </button>
                  <button
                    type="button"
                    className="stop-button"
                    onClick={handleDeleteTeam}
                    disabled={teamDeleting}
                  >
                    {teamDeleting ? 'Deleting…' : 'Delete Team'}
                  </button>
                </div>
              </form>
            </section>

            <section className="form-card">
              <h2>Runners</h2>
              {team.runners.length === 0 ? (
                <p className="board-status">No runners yet.</p>
              ) : (
                <div className="runner-table-wrap">
                  <table className="runner-table admin-added-table">
                    <thead>
                      <tr>
                        <th>Leg</th>
                        <th>Name</th>
                        <th>Bib</th>
                        <th>Sex</th>
                        <th>Tag EPC Hex</th>
                        <th>Status</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {team.runners.map((runner) =>
                        editingRunnerId === runner.id ? (
                          <tr key={runner.id}>
                            <td>
                              <input
                                type="number"
                                min={1}
                                className="table-input table-input-narrow"
                                value={editRunnerForm.leg}
                                onChange={(e) => setEditRunnerForm((f) => ({ ...f, leg: e.target.value }))}
                              />
                            </td>
                            <td>
                              <input
                                type="text"
                                className="table-input"
                                value={editRunnerForm.name}
                                onChange={(e) => setEditRunnerForm((f) => ({ ...f, name: e.target.value }))}
                              />
                            </td>
                            <td>
                              <input
                                type="text"
                                className="table-input table-input-narrow"
                                value={editRunnerForm.bib}
                                onChange={(e) => setEditRunnerForm((f) => ({ ...f, bib: e.target.value }))}
                              />
                            </td>
                            <td>
                              <Dropdown
                                className="table-input-narrow"
                                value={editRunnerForm.sex}
                                onChange={(sex) => setEditRunnerForm((f) => ({ ...f, sex }))}
                                options={SEX_OPTIONS}
                              />
                            </td>
                            <td>
                              <input
                                type="text"
                                className="table-input"
                                value={editRunnerForm.epcHex}
                                onChange={(e) => setEditRunnerForm((f) => ({ ...f, epcHex: e.target.value }))}
                              />
                            </td>
                            <td>
                              <button
                                type="button"
                                className={runner.status === 'ACTIVE' ? 'status-toggle status-toggle-active' : 'status-toggle status-toggle-inactive'}
                                onClick={() =>
                                  handleSetRunnerStatus(runner.id, runner.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE')
                                }
                                disabled={runnerStatusUpdatingId !== null}
                              >
                                {runnerStatusUpdatingId === runner.id ? 'Updating…' : runner.status}
                              </button>
                            </td>
                            <td className="row-actions">
                              <button
                                type="button"
                                className="start-button"
                                onClick={() => handleSaveRunner(runner.id)}
                                disabled={runnerSaving}
                              >
                                {runnerSaving ? 'Saving…' : 'Save'}
                              </button>
                              <button type="button" className="clear-button" onClick={cancelEditRunner}>
                                Cancel
                              </button>
                            </td>
                          </tr>
                        ) : (
                          <tr key={runner.id}>
                            <td>R{runner.leg}</td>
                            <td>{runner.name}</td>
                            <td>{runner.bib ?? '—'}</td>
                            <td>{runner.sex ?? '—'}</td>
                            <td className="mono-cell">{runner.epcHex}</td>
                            <td>
                              <button
                                type="button"
                                className={runner.status === 'ACTIVE' ? 'status-toggle status-toggle-active' : 'status-toggle status-toggle-inactive'}
                                onClick={() =>
                                  handleSetRunnerStatus(runner.id, runner.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE')
                                }
                                disabled={
                                  editingRunnerId !== null || runnerDeletingId !== null || runnerStatusUpdatingId !== null
                                }
                              >
                                {runnerStatusUpdatingId === runner.id ? 'Updating…' : runner.status}
                              </button>
                            </td>
                            <td className="row-actions">
                              <button
                                type="button"
                                className="clear-button"
                                onClick={() => startEditRunner(runner)}
                                disabled={editingRunnerId !== null || runnerDeletingId !== null}
                              >
                                Edit
                              </button>
                              <button
                                type="button"
                                className="stop-button"
                                onClick={() => handleDeleteRunner(runner.id)}
                                disabled={editingRunnerId !== null || runnerDeletingId !== null}
                              >
                                {runnerDeletingId === runner.id ? 'Deleting…' : 'Delete'}
                              </button>
                            </td>
                          </tr>
                        ),
                      )}
                    </tbody>
                  </table>
                </div>
              )}
              {runnerRowError && <p className="form-error">{runnerRowError}</p>}

              <h2 className="add-runner-heading">Add Runner</h2>
              <form onSubmit={handleAddRunner} className="admin-form admin-form-row" noValidate>
                <label className="form-field">
                  <span>Name</span>
                  <input
                    type="text"
                    value={addRunnerForm.name}
                    onChange={(e) => setAddRunnerForm((f) => ({ ...f, name: e.target.value }))}
                    required
                    placeholder="Jordan Lee"
                  />
                </label>
                <label className="form-field form-field-narrow">
                  <span>Leg</span>
                  <input
                    type="number"
                    min={1}
                    value={addRunnerForm.leg}
                    onChange={(e) => setAddRunnerForm((f) => ({ ...f, leg: e.target.value }))}
                    required
                  />
                </label>
                <label className="form-field form-field-narrow">
                  <span>Bib</span>
                  <input
                    type="text"
                    value={addRunnerForm.bib}
                    onChange={(e) => setAddRunnerForm((f) => ({ ...f, bib: e.target.value }))}
                    required
                    placeholder="A01"
                  />
                </label>
                <label className="form-field form-field-narrow">
                  <span>Sex</span>
                  <Dropdown
                    value={addRunnerForm.sex}
                    onChange={(sex) => setAddRunnerForm((f) => ({ ...f, sex }))}
                    options={SEX_OPTIONS}
                  />
                </label>
                <label className="form-field">
                  <span>Runner Tag EPC Hex</span>
                  <input
                    type="text"
                    value={addRunnerForm.epcHex}
                    onChange={(e) => setAddRunnerForm((f) => ({ ...f, epcHex: e.target.value }))}
                    required
                    placeholder="000000000000000000006401"
                  />
                </label>
                {addRunnerError && <p className="form-error">{addRunnerError}</p>}
                <button type="submit" className="start-button" disabled={addRunnerPending}>
                  {addRunnerPending ? 'Adding…' : 'Add Runner'}
                </button>
              </form>
            </section>
          </>
        )}
      </main>
    </div>
  )
}

export default TeamEditPage

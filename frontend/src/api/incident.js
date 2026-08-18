import request from './request'

export function listIncidents() {
  return request.get('/incidents')
}

export function listActiveIncidents() {
  return request.get('/incidents/active')
}

export function analyzeIncident(id) {
  return request.post(`/incidents/${id}/ai-analysis`)
}

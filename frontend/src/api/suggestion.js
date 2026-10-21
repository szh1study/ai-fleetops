import request from './request'

export function listSuggestions(incidentId) {
  return request.get(`/incidents/${incidentId}/ai-suggestions`)
}

export function confirmSuggestion(id, confirmedBy, description) {
  return request.post(`/ai-suggestions/${id}/confirm`, { confirmedBy, description })
}

export function rejectSuggestion(id) {
  return request.post(`/ai-suggestions/${id}/reject`)
}

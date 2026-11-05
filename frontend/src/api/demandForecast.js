import request from './request'

export function generateForecast(payload) {
  return request.post('/demand-forecasts/generate', payload)
}

export function listForecasts(date) {
  return request.get('/demand-forecasts', { params: date ? { date } : {} })
}

export function confirmDispatchSuggestion(id) {
  return request.post(`/dispatch-suggestions/${id}/confirm`, {})
}

export function rejectDispatchSuggestion(id) {
  return request.post(`/dispatch-suggestions/${id}/reject`)
}

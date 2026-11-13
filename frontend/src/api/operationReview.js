import request from './request'

export function listForecastReview(date) {
  return request.get('/operation-reviews/forecast', { params: { date } })
}

export function listDispatchReview(date) {
  return request.get('/operation-reviews/dispatch', { params: { date } })
}

export function getIncidentEfficiency(date) {
  return request.get('/operation-metrics/incident-efficiency', { params: { date } })
}

export function getAiAdoption(date) {
  return request.get('/operation-metrics/ai-adoption', { params: date ? { date } : {} })
}

export function generateDailyReport(date) {
  return request.get('/operation-reports/daily', { params: { date } })
}

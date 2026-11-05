import request from './request'

export function generateForecast(payload) {
  return request.post('/demand-forecasts/generate', payload)
}

export function listForecasts(date) {
  return request.get('/demand-forecasts', { params: date ? { date } : {} })
}

import request from './request'

export function listVehicles() {
  return request.get('/vehicles')
}

export function getVehicleById(id) {
  return request.get(`/vehicles/${id}`)
}

export function listReadyVehicles() {
  return request.get('/vehicles/ready')
}

export function listAbnormalVehicles() {
  return request.get('/vehicles/abnormal')
}

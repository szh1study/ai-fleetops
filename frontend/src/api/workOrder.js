import request from './request'

export function listWorkOrders() {
  return request.get('/work-orders')
}

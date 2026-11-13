import request from './request'

export function createSession() {
  return request.post('/chat/sessions')
}

export function listSessions() {
  return request.get('/chat/sessions')
}

export function getMessages(id) {
  return request.get(`/chat/sessions/${id}/messages`)
}

export function sendMessage(id, content) {
  return request.post(`/chat/sessions/${id}/messages`, { content })
}

export function deleteSession(id) {
  return request.delete(`/chat/sessions/${id}`)
}

// frontend: src/services/notesService.js
import api from "@/services/api";

// returns Spring Page object
export function getMyNotes(page = 0, size = 10) {
  return api.get(`/users/me/notes?page=${page}&size=${size}`);
}

// Admin-only endpoints (if admin UI is added later)
export function getNotesForUser(userId, page = 0, size = 10) {
  return api.get(`/admin/users/${userId}/notes?page=${page}&size=${size}`);
}

export function createNoteForUser(userId, note) {
  return api.post(`/admin/users/${userId}/notes`, { note });
}

export function updateNote(noteId, note) {
  return api.put(`/admin/notes/${noteId}`, { note });
}

export function deleteNote(noteId) {
  return api.delete(`/admin/notes/${noteId}`);
}
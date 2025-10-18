import api from "@/services/api";

export function listRecurringTemplates(ownerId) {
  return api.get("/recurrings", { params: { ownerId } });
}
export function createRecurringTemplate(payload) {
  return api.post("/recurrings", payload);
}
export function updateRecurringTemplate(id, payload) {
  return api.put(`/recurrings/${id}`, payload);
}
export function toggleRecurringTemplate(id, active) {
  return api.patch(`/recurrings/${id}/toggle`, null, { params: { active } });
}
export function previewRecurringTemplate(id, from, to) {
  return api.get(`/recurrings/${id}/preview`, { params: { from, to } });
}
import api from "@/services/api";

// READ
export function listCategories(params = {}) {
  // podržano: { search, page, size, type } (ako backend ignoriše – ok)
  return api.get("/categories", { params });
}

// CREATE
export function createCategory(payload) {
  // očekujemo bar: { name } ; (ako backend traži i type/icon/color – dodaj)
  return api.post("/categories", payload);
}

// UPDATE (PUT)
export function updateCategory(id, payload) {
  return api.put(`/categories/${id}`, payload);
}

// DELETE
export function deleteCategory(id) {
  return api.delete(`/categories/${id}`);
}

export function listCategoriesByOwner(ownerId, params = {}) {
  return api.get(`/categories/owner/${ownerId}`, { params });
}
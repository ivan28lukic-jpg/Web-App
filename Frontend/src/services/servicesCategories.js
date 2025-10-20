import api from "@/services/api";

// ADMIN endpointi
export function adminListCategories(params = {}) {
  return api.get("/admin/categories", { params });
}

export function adminCreateCategory(payload) {
  return api.post("/admin/categories", payload);
}

export function adminUpdateCategory(id, payload) {
  return api.put(`/admin/categories/${id}`, payload);
}

export function adminDeleteCategory(id) {
  return api.delete(`/admin/categories/${id}`);
}

// USER endpointi

// SVI (user + global)
export function listCategories(params = {}) {
  // podržano: { search, page, size, type }
  return api.get("/categories", { params });
}

// Samo user kategorije (po ownerId)
export function listCategoriesByOwner(ownerId, params = {}) {
  return api.get(`/categories/owner/${ownerId}`, { params });
}

// CREATE (user endpoint)
export function createCategory(payload) {
  return api.post("/categories", payload);
}

// UPDATE (user endpoint)
export function updateCategory(id, payload) {
  return api.put(`/categories/${id}`, payload);
}

// DELETE (user endpoint)
export function deleteCategory(id) {
  return api.delete(`/categories/${id}`);
}
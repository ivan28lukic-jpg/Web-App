import api from "@/services/api";

// Pretraga korisnika po username (za autocomplete)
export function searchAdminUsers(q) {
  return api.get("/admin/users/search", { params: { q } });
}

// Pretraga kategorija po imenu (za autocomplete)
export function searchAdminCategories(q) {
  return api.get("/admin/categories/search", { params: { q } });
}
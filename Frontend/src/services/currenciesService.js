import api from "@/services/api";

// --- USER FUNKCIJE (ostaju tvoje) ---
export function listCurrencies(params = {}) {
  return api.get("/currencies", { params });
}

export function getCurrency(code) {
  return api.get(`/currencies/${encodeURIComponent(String(code))}`);
}

// --- ADMIN FUNKCIJE ---

/**
 * List all currencies (admin, GET /api/admin/currencies)
 */
export function adminListCurrencies() {
  return api.get("/admin/currencies");
}

/**
 * Create new currency (admin, POST /api/admin/currencies)
 * payload: { code, name, valueVsEur }
 */
export function adminCreateCurrency(payload) {
  return api.post("/admin/currencies", payload);
}

/**
 * Update currency (admin, PUT /api/admin/currencies/{id})
 * payload: { name, valueVsEur }
 */
export function adminUpdateCurrency(id, payload) {
  return api.put(`/admin/currencies/${id}`, payload);
}

/**
 * Delete currency (admin, DELETE /api/admin/currencies/{id})
 */
export function adminDeleteCurrency(id) {
  return api.delete(`/admin/currencies/${id}`);
}

/**
 * Fetch suggested rate from API (admin, GET /api/admin/currencies/fetch?code=RSD)
 */
export function adminFetchCurrency(code) {
  return api.get("/admin/currencies/fetch", { params: { code } });
}

/**
 * Refresh rate for existing currency (admin, PATCH /api/admin/currencies/{id}/refresh)
 */
export function adminRefreshCurrency(id) {
  return api.patch(`/admin/currencies/${id}/refresh`);
}
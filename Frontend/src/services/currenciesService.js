import api from "@/services/api";

/**
 * List all currencies (GET /api/currencies)
 * returns axios promise
 */
export function listCurrencies(params = {}) {
  return api.get("/currencies", { params });
}

/**
 * Get single currency by code (GET /api/currencies/{code})
 */
export function getCurrency(code) {
  return api.get(`/currencies/${encodeURIComponent(String(code))}`);
}
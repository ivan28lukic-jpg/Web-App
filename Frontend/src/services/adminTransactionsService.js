import api from "@/services/api";

export function adminListTransactions(params = {}) {
  return api.get("/admin/transactions", { params });
}

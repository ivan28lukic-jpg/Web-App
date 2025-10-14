import api from "@/services/api";

// LIST
export function listTransactions(params = {}) {
  // primer podržanih filtera: { search, page, size, walletId, type, dateFrom, dateTo, categoryId }
  return api.get("/transactions", { params });
}

// CREATE
export function createTransaction(payload) {
  // očekujemo: { walletId, type: 'INCOME'|'EXPENSE', amount, categoryId, description, date }
  return api.post("/transactions", payload);
}

// UPDATE (PUT, bez PATCH)
export function updateTransaction(id, payload) {
  // ista polja kao za create; backend će ignorisati koja ne menja
  return api.put(`/transactions/${id}`, payload);
}

// DELETE
export function deleteTransaction(id) {
  return api.delete(`/transactions/${id}`);
}
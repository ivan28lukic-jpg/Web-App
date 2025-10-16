// src/services/savingGoals.js
import api from "@/services/api";

export function createSavingGoal(payload) {
  return api.post(`/saving-goals`, payload);
}

export function updateSavingGoal(id, payload) {
  return api.put(`/saving-goals/${id}`, payload);
}

export function deleteSavingGoal(id) {
  return api.delete(`/saving-goals/${id}`);
}

// === LISTANJE CILJEVA ===

// ADMIN: vrati sve ciljeve
export function listSavingGoalsAll(includeArchived = false) {
  return api.get('/saving-goals', { params: { includeArchived } });
}

//USER
export function listSavingGoalsByOwner(ownerId, includeArchived = false) {
  return api.get(`/saving-goals/owner/${ownerId}`, { params: { includeArchived } });
}

// -- helpers --
const toNumber2dp = (x) => {
  const n = Number(String(x).replace(",", "."));
  if (!Number.isFinite(n)) return null;
  return Number(n.toFixed(2)); // BigDecimal-friendly numeric
};

// remove empty strings / null / undefined to avoid Jackson 400 on Instant = ""
const clean = (obj) => {
  const out = {};
  Object.entries(obj).forEach(([k, v]) => {
    if (v === "" || v === undefined || v === null) return; // ne šalji prazna polja
    out[k] = v;
  });
  return out;
};

export function contributeToGoal(id, payload) {
  const body = clean({
    amount: toNumber2dp(payload.amount),        // BigDecimal-friendly number
    fromWalletId: Number(payload.fromWalletId), // required
    // description: payload.description,
    // occurredAt: payload.occurredAt,          // MUST be ISO-8601 if sent, otherwise omit
    // outCategoryId: payload.outCategoryId,
    // inCategoryId: payload.inCategoryId,
  });
  return api.post(`/saving-goals/${id}/contribute`, body, {
    headers: { "Content-Type": "application/json" },
  });
}

export function withdrawFromGoal(id, payload) {
  const body = clean({
    amount: toNumber2dp(payload.amount),
    toWalletId: Number(payload.toWalletId),
    // description: payload.description,
    // occurredAt: payload.occurredAt,
    // outCategoryId: payload.outCategoryId,
    // inCategoryId: payload.inCategoryId,
  });
  return api.post(`/saving-goals/${id}/withdraw`, body, {
    headers: { "Content-Type": "application/json" },
  });
}

export function getGoalProgress(id, params = {}) {
  return api.get(`/saving-goals/${id}/progress`, { params });
}
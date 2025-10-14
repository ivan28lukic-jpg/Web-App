import api from "@/services/api";

// READ
export function listWallets(params = {}) {
  return api.get("/wallets", { params });
}
export function getWallet(id) {
  return api.get(`/wallets/${id}`);
}

// CREATE
export function createWallet(payload) {
  // backend koristi: name, currencyCode, balance?, archived?
  return api.post("/wallets", {
    name: payload.name,
    currencyCode: payload.currencyCode,
    balance: payload.balance,
    archived: payload.archived,
  });
}

// UPDATE (PUT – bez PATCH-a!)
export function updateWallet(id, payload) {
  // šaljemo komplet polja koja backend očekuje
  return api.put(`/wallets/${id}`, {
    name: payload.name,
    currencyCode: payload.currencyCode,
    balance: payload.balance,
    archived: payload.archived,
  });
}

// ARCHIVE / RESTORE = običan PUT sa promenjenim "archived"
export async function archiveWalletFull(id, fullWallet, archived) {
  // očekuješ da ovde proslediš *ceo* wallet (ili bar 4 obavezna polja)
  return updateWallet(id, {
    name: fullWallet.name,
    currencyCode: fullWallet.currencyCode,
    balance: fullWallet.balance,
    archived,
  });
}

// DELETE
export function deleteWallet(id) {
  return api.delete(`/wallets/${id}`);
}
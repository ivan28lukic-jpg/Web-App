import api from "@/services/api";

// READ
export function listWallets(params = {}) {
  return api.get("/wallets", { params });
}
export function getWallet(id) {
  return api.get(`/wallets/${id}`);
}
export function listWalletsByOwner(ownerId) {
  return api.get(`/wallets?ownerId=${ownerId}`);
}

// Helper za štedne novčanike — sada filtrira po savings polju
export async function getSavingsWallets(ownerId) {
  const { data } = await listWalletsByOwner(ownerId);
  const all = Array.isArray(data) ? data : (data?.content ?? []);
  return all.filter(w => w.savings === true); // filtrira po savings polju
}

// CREATE
export function createWallet(payload) {
  // backend koristi: name, currencyCode, balance, archived, savings
  return api.post("/wallets", {
    name: payload.name,
    currencyCode: payload.currencyCode,
    balance: payload.balance,       // koristi balance!
    archived: payload.archived,
    savings: payload.savings,       // DODATO: šalje savings polje!
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
    savings: payload.savings, // DODATO: šalje savings polje (ako backend dozvoljava)
  });
}

// ARCHIVE / RESTORE = običan PUT sa promenjenim "archived"
export async function archiveWalletFull(id, fullWallet, archived) {
  return updateWallet(id, {
    name: fullWallet.name,
    currencyCode: fullWallet.currencyCode,
    balance: fullWallet.balance,
    archived,
    savings: fullWallet.savings, // DODATO: šalje savings polje
  });
}

// DELETE
export function deleteWallet(id) {
  return api.delete(`/wallets/${id}`);
}
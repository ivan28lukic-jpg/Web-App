import api from "@/services/api";

/**
 * POST /api/transactions/transfer
 * payload must match TransferCreateRequest:
 * {
 *   fromWalletId: number,
 *   toWalletId: number,
 *   outCategoryId: number,
 *   inCategoryId: number,
 *   amount: number,
 *   description?: string,
 *   occurredAt?: string, // optional ISO
 *   transferId?: string  // optional
 * }
 */
export function transferFunds(payload) {
  return api.post("/transactions/transfer", payload);
}
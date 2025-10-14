<template>
  <div class="container page">
    <div class="card" style="overflow:auto;">
      <header style="display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:12px;">
        <div>
          <h2 style="margin:0 0 4px 0;">Transactions</h2>
          <p class="muted" style="margin:0;">Browse and manage your transactions.</p>
        </div>
        <div style="display:flex; gap:8px; align-items:center;">
          <select v-model="filters.walletId" class="form__control" style="width:160px;">
            <option value="">All wallets</option>
            <option v-for="w in wallets.items" :key="w.id" :value="String(w.id)">{{ w.name }}</option>
          </select>
          <select v-model="filters.type" class="form__control" style="width:140px;">
            <option value="">All types</option>
            <option value="INCOME">Income</option>
            <option value="EXPENSE">Expense</option>
          </select>
          <input v-model="filters.search" class="form__control" placeholder="Search…" style="width:200px;" @keydown.enter="reload()" />
          <button class="btn btn--outline" @click="reload()">Search</button>
          <button class="btn btn--primary" @click="openCreate()">+ New</button>
        </div>
      </header>

      <table class="wf__table">
        <thead>
          <tr>
            <th style="text-align:left;">Date</th>
            <th style="text-align:left;">Wallet</th>
            <th style="text-align:left;">Category</th>
            <th style="text-align:left;">Description</th>
            <th style="text-align:right;">Amount</th>
            <th style="width:1%;">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in txs.items" :key="t.id">
            <td style="text-align:left;">{{ dateOf(t) }}</td>
            <td style="text-align:left;">{{ walletName(t.walletId) }}</td>
            <td style="text-align:left;">{{ categoryName(t.categoryId) }}</td>
            <td style="text-align:left; opacity: .85;">{{ t.description || "—" }}</td>
            <td style="text-align:right; font-variant-numeric: tabular-nums;">
              <span :style="amountStyle(t)">
                {{ fmt(t.amount) }}
              </span>
            </td>
            <td>
              <div class="wf__actions">
                <button class="btn btn--secondary btn--sm" @click="openEdit(t)">Edit</button>
                <button class="btn btn--danger btn--sm" @click="askDelete(t)">Delete</button>
              </div>
            </td>
          </tr>

          <tr v-if="!txs.loading && txs.items.length === 0">
            <td colspan="6" style="text-align:center; padding:18px;">
              <span class="muted">No transactions.</span>
            </td>
          </tr>

          <tr v-if="txs.loading">
            <td colspan="6" style="text-align:center; padding:18px;">
              Loading…
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <TransactionModal
    v-model="modalOpen"
    :tx="current"
    @save="onSave"
    @closed="current=null"
  />

  <ConfirmDialog
    v-model="confirmOpen"
    title="Delete transaction?"
    :message="confirmMessage"
    confirm-text="Delete"
    cancel-text="Cancel"
    @confirm="doDelete"
  />
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useWalletsStore } from "@/stores/wallets";
import { useTransactionsStore } from "@/stores/transactions";
import { useCategoriesStore } from "@/stores/categories";
import { useToast } from "@/composables/useToast";
import TransactionModal from "@/components/TransactionModal.vue";
import ConfirmDialog from "@/components/ConfirmDialog.vue";

const toast = useToast();
const wallets = useWalletsStore();
const txs = useTransactionsStore();
const categories = useCategoriesStore?.();

const modalOpen = ref(false);
const current = ref(null);
const confirmOpen = ref(false);
const confirmMessage = ref("");
let toDeleteId = null;

const filters = txs.filters;

function fmt(n) {
  const v = Number(n || 0);
  return v.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
function dateOf(t) {
  // 1) pokušaj najčešćih ključeva
  const candidates = [
    "date", "executedAt", "occurredAt", "transactionDate",
    "createdAt", "updatedAt", "time", "timestamp", "happenedAt", "valueDate"
  ];

  let raw = null;
  for (const k of candidates) {
    if (t && t[k] != null && String(t[k]).length > 0) {
      raw = t[k];
      break;
    }
  }

  if (!raw) return "—";

  // 2) ako je broj -> epoch (sekunde ili milisekunde)
  if (typeof raw === "number") {
    const ms = raw > 1e12 ? raw : raw * 1000; // heuristika: 13 cifara = ms, 10 = sec
    return new Date(ms).toLocaleString();
  }

  // 3) ako je string ISO/SQL
  // pokušaj “YYYY-MM-DD” ili ISO sa vremenom
  const s = String(raw);

  // samo datum
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
    return new Date(s + "T00:00:00").toLocaleDateString();
  }

  // ISO sa vremenom (npr. 2025-10-13T18:42:11Z)
  const dt = new Date(s);
  if (!isNaN(dt.getTime())) {
    // prikaži i vreme da bude korisnije
    return dt.toLocaleString();     //ako hocemo samo datum -> toLocaleDateString()
  }

  // fallback (nepoznat format)
  return s;
}

function walletName(id) {
  const w = wallets.items.find(x => x.id === id);
  return w ? w.name : `#${id}`;
}
function categoryName(id) {
  if (!id || !categories) return "—";
  const c = categories.items.find(x => x.id === id);
  return c ? c.name : `#${id}`;
}
function amountStyle(t) {
  const isIncome = String(t.type).toUpperCase() === "INCOME";
  return { color: isIncome ? "var(--green-300, #22c55e)" : "var(--red-300, #ef4444)" };
}

async function reload() {
  await txs.fetch({ page: 0, size: 50 });
}

function openCreate() {
  current.value = null;
  modalOpen.value = true;
}
function openEdit(t) {
  current.value = { ...t };
  modalOpen.value = true;
}

async function onSave(payload) {
  try {
    if (current.value && current.value.id) {
      await txs.updateOne(current.value.id, payload);
    } else {
      await txs.createOne(payload);
    }
    await reload();
    modalOpen.value = false;
    current.value = null;
  } catch (e) {
    toast.error(e?.response?.data?.message || "Operation failed");
  }
}

function askDelete(t) {
  toDeleteId = t.id;
  confirmMessage.value = `This action is permanent. Delete transaction #${t.id}?`;
  confirmOpen.value = true;
}

async function doDelete() {
  if (!toDeleteId) return;
  try {
    await txs.removeOne(toDeleteId);
    await reload();
  } catch (e) {
    toast.error(e?.response?.data?.message || "Delete failed");
  } finally {
    toDeleteId = null;
  }
}

onMounted(async () => {
  if (!wallets.items.length) await wallets.fetch({ page: 0, size: 100 });
  if (categories && !categories.items?.length) await categories.fetch?.({ page: 0, size: 200 });
  await reload();
});
</script>

<style scoped>
.wf__table {
    width: 100%;
    border-collapse: collapse;
    border-spacing: 0;
}

.wf__table thead th {
    font-weight: 600;
    color: var(--muted);
    padding: 10px 12px;
    border-bottom: 1px solid rgba(255,255,255,0.08);
}

.wf__table tbody td {
    padding: 10px 12px;
    border-bottom: 1px solid rgba(255,255,255,0.06);
}

.wf__actions {
    display: flex;
    gap: 6px;
    justify-content: flex-end;
}

.muted {
    color: var(--muted);
}
</style>

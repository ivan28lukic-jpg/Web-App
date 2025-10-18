<template>
  <div class="container page">
    <div class="card" style="overflow:auto;">
      <header style="display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:12px;">
        <div>
          <h2 style="margin:0 0 4px 0;">Transactions</h2>
          <p class="muted" style="margin:0;">Browse and manage your transactions.</p>
        </div>

        <!-- controls: wallet filter, type filter, period selector + date range + display currency -->
        <div style="display:flex; gap:8px; align-items:center;">

          <select v-model="filters.walletId" class="form__control" style="width:160px;">
              <option value="">All wallets</option>
              <option v-for="w in walletOpts" :key="w.value" :value="w.value">{{ w.label }}</option>
            </select>

            <select v-model="filters.type" class="form__control" style="width:140px;">
              <option value="">All types</option>
              <option value="INCOME">Income</option>
              <option value="EXPENSE">Expense</option>
            </select>
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

    <!-- AGGREGATED STATS (computed on frontend with FX conversion) -->
    <div v-if="statsLoading" style="margin-top:16px;">Loading stats…</div>

    <div v-else style="margin-top:16px;" class="card" v-if="stats.length">
      <header style="padding:12px 16px; border-bottom:1px solid rgba(255,255,255,0.04);">
        <strong>Aggregated ({{ viewPeriodLabel }}) — displayed in {{ displayCurrencyCode }}</strong>
        <span style="margin-left:12px; color:var(--muted);">Period: {{ statsFrom || '—' }} → {{ statsTo || '—' }}</span>
        <div class="aggregated-filter-wrapper">

            <!-- aggregation period -->
            <select v-model="viewPeriod" class="form__control" style="width:160px;">
              <option value="daily">Daily</option>
              <option value="weekly">Weekly</option>
              <option value="monthly">Monthly</option>
              <option value="quarterly">Quarterly</option>
              <option value="yearly">Yearly</option>
            </select>

            <!-- date range -->
            <input type="date" v-model="statsFrom" class="form__control" style="width:140px;" />
            <input type="date" v-model="statsTo" class="form__control" style="width:140px;" />

            <!-- display currency -->
            <select v-model="displayCurrencyCode" class="form__control" style="width:120px;">
              <option v-for="c in currencies" :key="c.code" :value="c.code">{{ c.code }}</option>
              <option v-if="!currencies.length" value="EUR">EUR</option>
            </select>

            <button class="btn btn--outline" @click="applyStats()">Apply</button>
        </div>
      </header>
      <table class="wf__table" style="margin-top:8px;">
        <thead>
          <tr>
            <th style="text-align:left;">Period</th>
            <th style="text-align:right;">Income ({{ displayCurrencyCode }})</th>
            <th style="text-align:right;">Expense ({{ displayCurrencyCode }})</th>
            <th style="text-align:right;">Net ({{ displayCurrencyCode }})</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in stats" :key="row.period">
            <td style="text-align:left;">{{ row.period }}</td>
            <td style="text-align:right;">{{ fmt(row.income) }}</td>
            <td style="text-align:right;">{{ fmt(row.expense) }}</td>
            <td style="text-align:right;">{{ fmt(row.net) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Transfer form (inline, ispod everything) -->
    <WalletTransferForm
      v-if="!loadingTransfer"
      :wallets="wallets.items"
      :currencies="currencies"
      @submit="handleTransfer"
      @cancel="resetTransferForm"
    />

    <div v-if="loadingTransfer" style="margin-top:1rem;">Loading transfer resources…</div>
    <div v-if="errorTransfer" style="color:red; margin-top:6px;">{{ errorTransfer }}</div>

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
import api from "@/services/api";
import { useWalletsStore } from "@/stores/storeWallets";
import { useTransactionsStore } from "@/stores/transactions";
import { useCategoriesStore } from "@/stores/storeCategories";
import { useAuthStore } from "@/stores/auth";
import { useToast } from "@/composables/useToast";
import TransactionModal from "@/components/TransactionModal.vue";
import ConfirmDialog from "@/components/ConfirmDialog.vue";

import WalletTransferForm from "@/components/WalletTransferForm.vue";
import { listCurrencies } from "@/services/currenciesService";
import { transferFunds } from "@/services/transferService";

const toast = useToast();
const wallets = useWalletsStore();
const txs = useTransactionsStore();
const categories = useCategoriesStore?.();
const auth = useAuthStore();

const modalOpen = ref(false);
const current = ref(null);
const confirmOpen = ref(false);
const confirmMessage = ref("");
let toDeleteId = null;

const filters = txs.filters;

// Filtriraj wallet-e: admin vidi sve, user samo svoje
const isAdmin = computed(() => auth.user?.role === "ADMIN");
const walletOpts = computed(() =>
  isAdmin.value
    ? wallets.items.map(w => ({ value: String(w.id), label: w.name }))
    : auth.user
      ? wallets.items.filter(w => w.ownerId == auth.user.id)
                    .map(w => ({ value: String(w.id), label: w.name }))
      : []
);

// TRANSFER resources
const currencies = ref([]);
const loadingTransfer = ref(false);
const errorTransfer = ref("");

// STATS state
const viewPeriod = ref("daily"); // daily|weekly|monthly|quarterly|yearly
const statsFrom = ref(""); // yyyy-mm-dd
const statsTo = ref("");
const stats = ref([]);
const statsLoading = ref(false);

// display currency (default EUR)
const displayCurrencyCode = ref("EUR");

// utility format
function fmt(n) {
  const v = Number(n || 0);
  return v.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
function dateOf(t) {
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
  if (typeof raw === "number") {
    const ms = raw > 1e12 ? raw : raw * 1000;
    return new Date(ms).toLocaleString();
  }
  const s = String(raw);
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
    return new Date(s + "T00:00:00").toLocaleDateString();
  }
  const dt = new Date(s);
  if (!isNaN(dt.getTime())) {
    return dt.toLocaleString();
  }
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
  const type = (t?.type ?? t?.categoryType ?? t?.category?.type ?? "").toString().toUpperCase();
  return { color: type === "INCOME" ? "var(--green-300, #22c55e)" : "var(--red-300, #ef4444)" };
}

async function reload() {
  await txs.fetch({ page: 0, size: 50 });
}

// CRUD handlers (unchanged)
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

/** Resolve categories for transfer (auto) */
function resolveTransferCategories() {
  const cats = categories?.items || [];
  const findByNameAndType = (name, type) =>
    cats.find(c => c.name && c.name.toLowerCase() === name.toLowerCase() && String(c.type).toUpperCase() === String(type).toUpperCase());
  const findFirstType = (type) => cats.find(c => String(c.type).toUpperCase() === String(type).toUpperCase());

  const out = findByNameAndType("Transfer Out", "EXPENSE") || findFirstType("EXPENSE");
  const infl = findByNameAndType("Transfer In", "INCOME") || findFirstType("INCOME");

  return { outCategoryId: out ? Number(out.id) : null, inCategoryId: infl ? Number(infl.id) : null };
}

// TRANSFER handler
async function handleTransfer(payload) {
  loadingTransfer.value = true;
  errorTransfer.value = "";
  try {
    const { outCategoryId, inCategoryId } = resolveTransferCategories();
    if (!outCategoryId || !inCategoryId) {
      throw new Error("Transfer categories not configured on server. Please create Transfer Out (EXPENSE) and Transfer In (INCOME) categories.");
    }

    const body = {
      fromWalletId: Number(payload.fromWalletId),
      toWalletId: Number(payload.toWalletId),
      outCategoryId,
      inCategoryId,
      amount: Number(String(payload.amount).replace(",", ".")),
      description: payload.description || undefined
    };

    await transferFunds(body);
    toast.success("Transfer successful");
    // refresh wallets and transactions
    await wallets.fetch({ page: 0, size: 100 });
    await reload();
    // re-apply stats so aggregates reflect change
    await fetchAndAggregateTransactions();
  } catch (e) {
    const msg = e?.response?.data?.message || e.message || "Transfer failed";
    errorTransfer.value = msg;
    toast.error(msg);
  } finally {
    loadingTransfer.value = false;
  }
}

function resetTransferForm() {
  // form resets itself (child emits cancel)
}

/** utilities for aggregation/conversion */

// find currency object by code
function findCurrencyByCode(code) {
  if (!code) return null;
  return currencies.value.find(c => String(c.code).toUpperCase() === String(code).toUpperCase()) || null;
}

// convert amount from 'fromCode' to 'toCode' using valueVsEur.
// valueVsEur = units per EUR (e.g. RSD ~117)
// amount_to = amount_from / from.valueVsEur * to.valueVsEur
function convertAmount(amount, fromCode, toCode) {
  if (amount == null) return 0;
  if (!fromCode || !toCode) return Number(amount) || 0;
  const fromCur = findCurrencyByCode(fromCode);
  const toCur = findCurrencyByCode(toCode);
  if (!fromCur || !toCur || !fromCur.valueVsEur || !toCur.valueVsEur) {
    // if any rate missing, return amount as-is (warn)
    return Number(amount) || 0;
  }
  const rateFrom = Number(fromCur.valueVsEur);
  const rateTo = Number(toCur.valueVsEur);
  if (!rateFrom || !rateTo) return Number(amount) || 0;
  const eur = Number(amount) / rateFrom;
  const tgt = eur * rateTo;
  return Number(tgt);
}

// build period key from occurredAt based on viewPeriod
function makePeriodKey(isoString, period) {
  const d = new Date(isoString);
  if (isNaN(d.getTime())) return String(isoString);
  const y = d.getUTCFullYear();
  const m = d.getUTCMonth() + 1;
  const day = d.getUTCDate();
  if (period === "daily") {
    return `${y}-${String(m).padStart(2,"0")}-${String(day).padStart(2,"0")}`;
  }
  if (period === "weekly") {
    // ISO week number
    const tmp = new Date(Date.UTC(d.getUTCFullYear(), d.getUTCMonth(), d.getUTCDate()));
    // Thursday in current week decides the year.
    tmp.setUTCDate(tmp.getUTCDate() + 4 - (tmp.getUTCDay()||7));
    const yearStart = new Date(Date.UTC(tmp.getUTCFullYear(),0,1));
    const weekNo = Math.ceil( ( ( (tmp - yearStart) / 86400000) + 1)/7 );
    return `${tmp.getUTCFullYear()}-W${String(weekNo).padStart(2,"0")}`;
  }
  if (period === "monthly") {
    return `${y}-${String(m).padStart(2,"0")}`;
  }
  if (period === "quarterly") {
    const q = Math.floor((m-1)/3)+1;
    return `${y}-Q${q}`;
  }
  if (period === "yearly") {
    return `${y}`;
  }
  return `${y}-${String(m).padStart(2,"0")}-${String(day).padStart(2,"0")}`;
}

/** Fetch transactions (detailed) for given from/to and aggregate in frontend */
async function fetchAndAggregateTransactions() {
  statsLoading.value = true;
  stats.value = [];
  try {
    // build params for /api/transactions/search
    const params = {
      page: 0,
      size: 10000, // hopefully backend allows this; adjust if needed
    };
    if (filters.walletId) params.walletId = Number(filters.walletId);
    if (!isAdmin.value && auth.user?.id) params.ownerId = Number(auth.user.id);
    if (filters.type) params.categoryType = filters.type;
    if (statsFrom.value) params.from = statsFrom.value;
    if (statsTo.value) params.to = statsTo.value;

    const resp = await api.get("/transactions/search", { params });
    const data = resp?.data;
    // data can be a Page object or array
    let txItems = [];
    if (Array.isArray(data)) {
      txItems = data;
    } else if (data && Array.isArray(data.content)) {
      txItems = data.content;
    } else if (data && Array.isArray(data.items)) {
      txItems = data.items;
    }

    // aggregation map: key -> { income, expense }
    const map = new Map();
    const targetCode = displayCurrencyCode.value || "EUR";

    for (const t of txItems) {
      // t has: amount (BigDecimal), categoryType (INCOME|EXPENSE), occurredAt, walletId
      const occurred = t.occurredAt || t.createdAt || t.time || t.timestamp;
      const key = makePeriodKey(occurred, viewPeriod.value);

      // find wallet to get currency code
      const w = wallets.items.find(x => x.id === t.walletId);
      const fromCode = w ? (w.currencyCode || "EUR") : "EUR";

      // convert to target currency
      const amt = Number(t.amount || 0);
      const conv = convertAmount(amt, fromCode, targetCode);

      const row = map.get(key) || { income: 0, expense: 0, net: 0 };
      if (String(t.categoryType).toUpperCase() === "INCOME") {
        row.income += conv;
        row.net += conv;
      } else {
        row.expense += conv;
        row.net -= conv;
      }
      map.set(key, row);
    }

    // transform to array sorted by period (descending latest first)
    const arr = Array.from(map.entries()).map(([period, vals]) => ({
      period,
      income: Number(vals.income || 0),
      expense: Number(vals.expense || 0),
      net: Number(vals.net || 0)
    }));

    // sort by period — try to parse keys to Date for sensible order, fallback to string
    arr.sort((a,b) => {
      // attempt to convert period key to comparable date
      const pa = periodKeyToSortDate(a.period, viewPeriod.value);
      const pb = periodKeyToSortDate(b.period, viewPeriod.value);
      return pb - pa; // descending
    });

    stats.value = arr;
  } catch (e) {
    toast.error(e?.response?.data?.message || e.message || "Failed to load transactions for stats");
    stats.value = [];
  } finally {
    statsLoading.value = false;
  }
}

// helper to produce Date for sorting from period key
function periodKeyToSortDate(key, period) {
  try {
    if (period === "daily") return new Date(key + "T00:00:00Z").getTime();
    if (period === "monthly") {
      const [y,m] = key.split("-");
      return new Date(`${y}-${m}-01T00:00:00Z`).getTime();
    }
    if (period === "yearly") {
      return new Date(`${key}-01-01T00:00:00Z`).getTime();
    }
    if (period === "quarterly") {
      const [y, qPart] = key.split("-Q");
      const q = Number(qPart);
      const month = (q-1)*3 + 1;
      return new Date(`${y}-${String(month).padStart(2,"0")}-01T00:00:00Z`).getTime();
    }
    if (period === "weekly") {
      // key format YYYY-Www
      const [y, wStr] = key.split("-W");
      const w = Number(wStr);
      // approximate: first day of week w
      const jan4 = new Date(Date.UTC(y,0,4));
      const monday = new Date(jan4);
      const day = jan4.getUTCDay() || 7;
      monday.setUTCDate(jan4.getUTCDate() - day + 1 + (w-1)*7);
      return monday.getTime();
    }
    return Date.now();
  } catch (ex) {
    return Date.now();
  }
}

function applyStats() {
  fetchAndAggregateTransactions();
}

const viewPeriodLabel = computed(() => {
  switch (viewPeriod.value) {
    case "daily": return "Daily";
    case "weekly": return "Weekly";
    case "monthly": return "Monthly";
    case "quarterly": return "Quarterly";
    case "yearly": return "Yearly";
    default: return viewPeriod.value;
  }
});

/** INITIAL LOAD */
onMounted(async () => {
  // wallets fetch for wallet selects (if empty)
  if (!wallets.items.length) await wallets.fetch({ page: 0, size: 100 });
  if (categories && !categories.items?.length) await categories.fetch?.({ page: 0, size: 200 });
  await reload();

  // load currencies for conversion display (transfer + stats)
  loadingTransfer.value = true;
  try {
    const { data } = await listCurrencies();
    currencies.value = Array.isArray(data) ? data : (data?.content ?? []);
    // ensure default display currency exists
    if (!currencies.value.find(c => c.code === displayCurrencyCode.value)) {
      displayCurrencyCode.value = currencies.value.length ? currencies.value[0].code : "EUR";
    }
  } catch (e) {
    errorTransfer.value = e?.message || "Failed to load currencies";
  } finally {
    loadingTransfer.value = false;
  }

  // initial stats load
  await fetchAndAggregateTransactions();
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
.card { margin-top: 1rem; padding: 12px; border-radius: 8px; background: rgba(255,255,255,0.02); }
.form__control { padding:8px; border-radius:6px; border:1px solid rgba(255,255,255,0.04); background: transparent; color: inherit; }
.btn { padding:.5rem .8rem; border-radius:.5rem; cursor:pointer; }
.btn--primary { background:#16a34a; color:white; border:1px solid #16a34a; }
.btn--outline { background:transparent; border:1px solid rgba(255,255,255,0.06); color:inherit; }

.card header .aggregated-filter-wrapper {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-top: 8px;
  justify-content: end;
}
</style>
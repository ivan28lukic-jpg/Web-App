<template>
  <div class="transfer-card">
    <h3 style="margin:0 0 8px 0;">Transfer između novčanika</h3>

    <div class="transfer-grid">
      <div class="col">
        <label class="label">From</label>
        <select v-model="state.fromWalletId" class="form__control">
          <option value="">Choose wallet</option>
          <option v-for="w in wallets" :key="w.id" :value="String(w.id)">
            {{ w.name }} ({{ w.currencyCode }}) — {{ fmt(w.balance) }}
          </option>
        </select>

        <div v-if="fromWallet" class="box">
          <div>Balance: <b>{{ fmt(fromWallet.balance) }} {{ fromWallet.currencyCode }}</b></div>
          <div style="margin-top:8px;">
            Amount:
            <input v-model="state.amount" type="number" step="0.01" min="0" class="form__control" placeholder="0.00" />
          </div>
          <div style="margin-top:6px;">
            Description:
            <input v-model="state.description" type="text" class="form__control" placeholder="Optional note" />
          </div>
        </div>
      </div>

      <div class="arrow">➔</div>

      <div class="col">
        <label class="label">To</label>
        <select v-model="state.toWalletId" class="form__control">
          <option value="">Choose wallet</option>
          <option v-for="w in wallets" :key="w.id" :value="String(w.id)" :disabled="state.fromWalletId && String(w.id) === state.fromWalletId">
            {{ w.name }} ({{ w.currencyCode }}) — {{ fmt(w.balance) }}
          </option>
        </select>

        <div v-if="toWallet" class="box">
          <div>Currency: <b>{{ toWallet.currencyCode }}</b></div>
          <div style="margin-top:12px;">
            Converted amount:
            <span v-if="convertedAmount">{{ convertedAmount }} {{ toWallet.currencyCode }}</span>
            <span v-else>—</span>
          </div>
        </div>
      </div>
    </div>

    <div class="actions">
      <button class="btn btn--outline" type="button" @click="onCancel">Cancel</button>
      <button class="btn btn--primary" type="button" :disabled="!valid || submitting" @click="onSubmit">
        {{ submitting ? 'Transferring...' : 'Transfer' }}
      </button>
    </div>

    <div v-if="error" class="error">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from "vue";

const props = defineProps({
  wallets: { type: Array, default: () => [] },       // [{id, name, currencyCode, balance, ...}]
  currencies: { type: Array, default: () => [] }     // [{code, valueVsEur, name}]
});
const emit = defineEmits(["submit", "cancel"]);

const state = reactive({
  fromWalletId: "",
  toWalletId: "",
  amount: "",
  description: ""
});

// FIX: use ref for boolean/string state so template sees primitive value (auto-unwrapped)
const submitting = ref(false);
const error = ref("");

function fmt(n) {
  const v = Number(n || 0);
  return v.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

const fromWallet = computed(() => {
  return props.wallets.find(w => String(w.id) === String(state.fromWalletId)) || null;
});
const toWallet = computed(() => {
  return props.wallets.find(w => String(w.id) === String(state.toWalletId)) || null;
});

function getCurrency(code) {
  if (!code) return null;
  return props.currencies.find(c => String(c.code).toUpperCase() === String(code).toUpperCase()) || null;
}
const fromCurrency = computed(() => getCurrency(fromWallet.value?.currencyCode));
const toCurrency = computed(() => getCurrency(toWallet.value?.currencyCode));

// converted amount: convert amount (in from currency) -> to currency using valueVsEur
const convertedAmount = computed(() => {
  const raw = Number(state.amount || 0);
  if (!raw || !fromCurrency.value || !toCurrency.value) return "";
  const rateFrom = Number(fromCurrency.value.valueVsEur);
  const rateTo = Number(toCurrency.value.valueVsEur);
  if (!rateFrom || !rateTo) return "";
  // amountFrom in EUR = amount / rateFrom ; then * rateTo
  const inEur = raw / rateFrom;
  const amountTo = inEur * rateTo;
  return Number(amountTo).toFixed(2);
});

const valid = computed(() => {
  const amountN = Number(state.amount || 0);
  if (!fromWallet.value || !toWallet.value) return false;
  if (String(fromWallet.value.id) === String(toWallet.value.id)) return false;
  if (!(amountN > 0)) return false;
  if (fromWallet.value.balance != null) {
    try {
      const bal = Number(fromWallet.value.balance);
      if (!isNaN(bal) && amountN > bal) return false;
    } catch (e) {}
  }
  return true;
});

function onCancel() {
  state.fromWalletId = "";
  state.toWalletId = "";
  state.amount = "";
  state.description = "";
  error.value = "";
  emit("cancel");
}

async function onSubmit() {
  error.value = "";
  if (!valid.value) {
    error.value = "Please select wallets and enter a valid amount within the source wallet balance.";
    return;
  }
  submitting.value = true;
  try {
    // emit payload; parent handles the async transfer
    emit("submit", {
      fromWalletId: Number(state.fromWalletId),
      toWalletId: Number(state.toWalletId),
      amount: Number(String(state.amount).replace(",", ".")),
      description: state.description || undefined
    });
    // Optionally you can reset form here or wait for parent to refresh data
  } finally {
    // reset local loading flag so button becomes clickable again
    submitting.value = false;
  }
}
</script>

<style scoped>
.transfer-card.card {
  margin-top: 1.5rem;
  padding: 18px;
  border-radius: 10px;
  background: rgba(255,255,255,0.02); /* match table card */
  border: 1px solid rgba(255,255,255,0.04);
  box-shadow: 0 2px 10px rgba(0,0,0,0.25);
}

/* title consistent with other cards */
.tc__title {
  margin: 0 0 10px 0;
  color: var(--muted);
  font-weight: 700;
}

/* layout */
.transfer-grid { display:flex; gap:18px; align-items:flex-start; }
.col { flex:1; min-width: 220px; }
.arrow { align-self:center; font-size:28px; color:var(--green-300, #22c55e); padding:0 6px; }

/* boxes inside columns */
.box {
  margin-top:10px;
  padding:10px;
  border-radius:8px;
  background: rgba(0,0,0,0.12);
  border: 1px solid rgba(255,255,255,0.03);
}
.box-row { margin-bottom:8px; color: var(--muted); }

/* labels and small labels */
.label { display:block; font-weight:600; margin-bottom:6px; color:var(--muted); }
.mini { display:block; font-size:0.85rem; color:var(--muted); margin-bottom:6px; }

/* form controls styled like table inputs */
.form__control {
  width:100%;
  padding:10px 12px;
  border-radius:8px;
  border:1px solid rgba(255,255,255,0.04);
  background: transparent;
  color: inherit;
  box-sizing: border-box;
  outline: none;
}
.form__control:focus {
  box-shadow: 0 0 0 3px rgba(34,197,94,0.06);
  border-color: rgba(34,197,94,0.18);
}

/* actions aligned to the right like other cards */
.actions { display:flex; justify-content:flex-end; gap:10px; margin-top:14px; }
.error { margin-top:10px; color:#f87171; font-weight:600; }

/* buttons consistent with site */
.btn { padding:8px 14px; border-radius:8px; cursor:pointer; }
.btn--primary { background:var(--green-300, #16a34a); color:white; border:1px solid var(--green-300, #16a34a); }
.btn--outline { background:transparent; border:1px solid rgba(255,255,255,0.06); color:inherit; }

/* small screens tweak */
@media (max-width: 880px) {
  .transfer-grid { flex-direction: column; gap:12px; }
  .arrow { display:none; }
}
</style>
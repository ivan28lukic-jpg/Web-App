<script setup>
import { reactive, ref, watch, computed } from "vue";
import { listWalletsByOwner } from "@/services/serviceWallets";

const props = defineProps({
  mode: { type: String, required: true },     // "contribute" | "withdraw"
  ownerId: { type: Number, required: true },  // ID korisnika koji inicira, ali NE koristi se za prikaz wallets
  goal: { type: Object, required: true },     // goal.ownerId je pravi vlasnik cilja
});
const emit = defineEmits(["submit", "cancel"]);

const state = reactive({ amount: "", selectedWalletId: "" });
const wallets = ref([]);
const loading = ref(false);
const error = ref(null);

// Učitaj novčanike vlasnika cilja (goal.ownerId), ne trenutnog usera
async function loadWalletsForGoalOwner() {
  if (!props.goal?.ownerId) {
    wallets.value = [];
    return;
  }
  try {
    loading.value = true;
    const { data } = await listWalletsByOwner(props.goal.ownerId);
    wallets.value = Array.isArray(data) ? data : (data?.content ?? []);
  } catch (e) {
    error.value = e?.response?.data?.message || e.message;
  } finally {
    loading.value = false;
  }
}

watch(
  () => props.goal?.ownerId,
  () => { loadWalletsForGoalOwner(); },
  { immediate: true }
);

const goalWalletId  = computed(() => props.goal?.walletId ?? props.goal?.wallet?.id ?? null);
const goalCurrency  = computed(() => props.goal?.wallet?.currencyCode || props.goal?.walletCurrencyCode || "");
const goalAvailable = computed(() => Number(props.goal?.currentAmount ?? 0));

const filteredWallets = computed(() => {
  if (!goalWalletId.value) return wallets.value;
  return wallets.value.filter(w => w.id !== goalWalletId.value);
});
const selectedWallet = computed(() =>
  filteredWallets.value.find(w => String(w.id) === String(state.selectedWalletId))
);
function wCur(w) { return w?.currencyCode || w?.currency || ""; }
function wBal(w) {
  const c = [w?.balance, w?.availableBalance, w?.amount, w?.currentAmount];
  const v = c.find(x => x != null);
  return Number(v ?? NaN);
}
const selCur = computed(() => wCur(selectedWallet.value));
const selBal = computed(() => wBal(selectedWallet.value));

function toNumber2dp(x) {
  const n = Number(String(x).replace(",", "."));
  if (!Number.isFinite(n)) return null;
  return Number(n.toFixed(2));
}

function valid() {
  const a = toNumber2dp(state.amount);
  if (!a || !state.selectedWalletId) return false;
  if (props.mode === "contribute") {
    const b = selBal.value;
    if (Number.isFinite(b) && Number(a) > b) return false;
  } else {
    if (Number(a) > goalAvailable.value) return false;
  }
  return true;
}

function submit() {
  const amt = toNumber2dp(state.amount);
  if (amt === null || !state.selectedWalletId) return;

  const payload = { amount: amt };
  if (props.mode === "contribute") payload.fromWalletId = Number(state.selectedWalletId);
  else payload.toWalletId = Number(state.selectedWalletId);

  emit("submit", payload);
}
</script>

<template>
  <form class="tf" @submit.prevent="submit" @keydown.enter.prevent>
    <h3 class="title">{{ mode==='contribute' ? 'Contribute to goal' : 'Withdraw from goal' }}</h3>

    <div v-if="error" class="error">⚠ {{ error }}</div>
    <div v-else-if="loading">Loading wallets…</div>

    <div v-else class="grid">
      <div class="col">
        <label class="lab">Amount</label>
        <input v-model="state.amount" type="text" inputmode="decimal" class="inp"
               :placeholder="mode==='contribute' ? ('Currency: ' + (selCur || '—')) : ('Goal currency: ' + (goalCurrency || '—'))"/>
        <small class="hint" v-if="mode==='withdraw'">
          Available at goal: {{ goalAvailable.toFixed(2) }} {{ goalCurrency || '' }}
        </small>
      </div>

      <div class="col">
        <label class="lab">{{ mode==='contribute' ? 'From wallet' : 'To wallet' }}</label>
        <select v-model="state.selectedWalletId" class="inp">
          <option value="">— choose wallet —</option>
          <option v-for="w in filteredWallets" :key="w.id" :value="w.id">
            {{ w.name }} ({{ wCur(w) }})
          </option>
        </select>
        <small class="hint" v-if="mode==='contribute' && selectedWallet">
          Balans: {{ Number.isFinite(selBal) ? selBal.toFixed(2) : 'n/a' }} {{ selCur || '' }}
        </small>
      </div>
    </div>

    <div class="actions">
      <button type="button" class="btn" @click="$emit('cancel')">Cancel</button>
      <button type="submit" class="btn primary" :disabled="!valid()">Save</button>
    </div>
  </form>
</template>

<style scoped>
:root{--panel2:#121619;--line2:#27323a;--fg:#e9efec}
@media (prefers-color-scheme: light){
  :root{--panel2:#f7faf9;--line2:#e2e8f0;--fg:#0b1114}
}
.tf{display:grid;gap:1rem;color:var(--fg)}
.title{margin:0 0 .25rem 0;font-size:1.1rem;font-weight:800}
.grid{display:grid;grid-template-columns:1fr 1fr;gap:1rem}
.col{display:grid;gap:.45rem}
.lab{font-size:.9rem;font-weight:700;opacity:.9}
.inp{padding:.65rem .8rem;border:1px solid var(--line2);border-radius:.7rem;background:var(--panel2);color:var(--fg)}
.inp:focus{outline:none;border-color:#20c589;box-shadow:0 0 0 3px rgba(32,197,137,.18)}
.hint{color:#9eb5ad;font-size:.85rem}
.actions{display:flex;justify-content:flex-end;gap:.6rem}
.btn{padding:.6rem .9rem;border-radius:.8rem;border:1px solid #20c589;background:transparent;color:#20c589;font-weight:700}
.btn.primary{background:#20c589;color:#0b1114}
.error{color:#ff7d75}
</style>
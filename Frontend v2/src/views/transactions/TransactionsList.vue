<template>
<section class="card">
<header class="card__header">
<h2 style="margin:0">Transakcije</h2>
<div style="display:flex; gap:8px; align-items:center;">
<button class="btn" @click="openCreate">+ Nova transakcija</button>
<button class="btn btn--ghost" @click="openTransfer">Transfer</button>
</div>
</header>


<div class="card__body">
<div class="filters">
<SearchInput v-model="query" placeholder="Pretraži po nazivu..." />
<select v-model="walletId" class="select">
<option :value="null">Svi novčanici</option>
<option v-for="w in walletsOptions" :key="w.id" :value="w.id">{{ w.name }}</option>
</select>
<select v-model="type" class="select">
<option value="ALL">Svi tipovi</option>
<option value="INCOME">Prihod</option>
<option value="EXPENSE">Trošak</option>
</select>
<input v-model="dateFrom" class="input" type="date" />
<input v-model="dateTo" class="input" type="date" />
</div>


<div v-if="isLoading" class="skeleton">Učitavanje...</div>


<table v-else class="table">
<thead>
<tr>
<th>Naziv</th>
<th>Iznos</th>
<th>Tip</th>
<th>Kategorija</th>
<th>Novčanik</th>
<th>Datum</th>
<th style="width:120px;">Akcije</th>
</tr>
</thead>
<tbody>
<tr v-for="tx in items" :key="tx.id">
<td>{{ tx.name }}</td>
<td>{{ formatMoney(tx.amount, tx.currencyCode) }}</td>
<td>{{ tx.type }}</td>
<td>{{ tx.categoryName }}</td>
<td>{{ tx.walletName }}</td>
<td>{{ formatDate(tx.occurredAt) }}</td>
<td>
<button class="btn btn--ghost" @click="remove(tx)">Obriši</button>
</td>
</tr>
<tr v-if="items.length === 0">
<td colspan="7" style="text-align:center; color:#888;">Nema rezultata.</td>
</tr>
</tbody>
</table>


<div class="pager" v-if="total > size">
<button class="btn btn--ghost" :disabled="page===1" @click="prev">Prethodna</button>
<span>Strana {{ page }}</span>
<button class="btn btn--ghost" :disabled="page>=maxPage" @click="next">Sledeća</button>
</div>
</div>


<TransactionForm v-if="showForm" @close="closeCreate" @saved="refetch" />
<TransferForm v-if="showTransfer" @close="closeTransfer" @saved="refetch" />
</section>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useTransactionsStore } from '@/stores/transactions'
import { useWalletsStore } from '@/stores/wallets'
import { useLiveSearch } from '@/composables/useLiveSearch'
import SearchInput from '@/components/SearchInput.vue'
import TransactionForm from './TransactionForm.vue'
import TransferForm from './TransferForm.vue'


const txStore = useTransactionsStore()
const walletsStore = useWalletsStore()

// preuzmi opcije novčanika za filter i formu
if (!walletsStore.list?.length) {
walletsStore.fetchList('')
}
const walletsOptions = computed(() => walletsStore.list || [])


const walletId = ref(null)
const type = ref('ALL')
const dateFrom = ref('')
const dateTo = ref('')


const { query, results, isLoading } = useLiveSearch((q, signal) => {
return txStore.fetchList({
search: q,
walletId: walletId.value,
type: type.value,
dateFrom: dateFrom.value,
dateTo: dateTo.value,
}, signal)
}, { immediate: true })


watch([walletId, type, dateFrom, dateTo], () => {
txStore.fetchList({
search: query.value,
walletId: walletId.value,
type: type.value,
dateFrom: dateFrom.value,
dateTo: dateTo.value,
})
})

const page = computed(() => txStore.page)
const size = computed(() => txStore.size)
const total = computed(() => txStore.total)
const items = computed(() => {
const data = results.value || txStore.items
return Array.isArray(data) ? data : (data?.items || [])
})
const maxPage = computed(() => Math.max(1, Math.ceil(total.value / size.value)))


function prev() {
if (txStore.page > 1) { txStore.page--; refetch() }
}
function next() {
if (txStore.page < maxPage.value) { txStore.page++; refetch() }
}
function refetch() {
txStore.fetchList({
search: query.value,
walletId: walletId.value,
type: type.value,
dateFrom: dateFrom.value,
dateTo: dateTo.value,
})
}


function openCreate() { showForm.value = true }
function closeCreate() { showForm.value = false }
function openTransfer() { showTransfer.value = true }
function closeTransfer() { showTransfer.value = false }


async function remove(tx) {
if (confirm('Obriši transakciju?')) {
await txStore.remove(tx.id)
}
}

const showForm = ref(false)
const showTransfer = ref(false)


function formatMoney(amount, code) {
try { return new Intl.NumberFormat('sr-RS', { style: 'currency', currency: code || 'RSD' }).format(amount || 0) }
catch { return `${amount ?? 0} ${code || ''}` }
}
function formatDate(s) {
if (!s) return ''
const d = new Date(s)
return d.toLocaleDateString('sr-RS')
}
</script>

<style scoped>
.filters {
display: grid;
gap: 10px;
grid-template-columns: 1fr 220px 160px 160px 160px;
margin-bottom: 12px;
}
.pager {
display: flex;
align-items: center;
gap: 8px;
justify-content: flex-end;
padding-top: 12px;
}
.skeleton {
padding: 12px;
background: linear-gradient(90deg, #f2f2f2, #fafafa, #f2f2f2);
background-size: 200% 100%;
animation: pulse 1.2s infinite linear;
border-radius: 12px;
}
@keyframes pulse {
0% { background-position: 200% 0; }
100% { background-position: -200% 0; }
}
</style>
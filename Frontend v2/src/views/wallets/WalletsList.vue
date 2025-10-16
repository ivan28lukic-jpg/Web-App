<template>
<section class="card">
<header class="card__header">
<h2 style="margin:0">Novčanici</h2>
<div style="display:flex; gap:8px; align-items:center;">
<button class="btn" @click="openCreate">+ Novi novčanik</button>
</div>
</header>


<div class="card__body">
<div style="display:flex; gap:12px; align-items:center; margin-bottom:12px;">
<SearchInput v-model="query" placeholder="Pretraži po nazivu..." />
<label style="display:flex; gap:8px; align-items:center;">
<input type="checkbox" v-model="includeArchived" />
<span>Uključi arhivirane</span>
</label>
</div>


<div v-if="isLoading" class="skeleton">Učitavanje...</div>


<table v-else class="table">
<thead>
<tr>
<th>Naziv</th>
<th>Valuta</th>
<th>Štedni</th>
<th>Arhiviran</th>
<th>Stanje</th>
<th>Akcije</th>
</tr>
</thead>
<tbody>
<tr v-for="w in wallets" :key="w.id">
<td>
<RouterLink :to="`/wallets/${w.id}`">{{ w.name }}</RouterLink>
</td>
<td>{{ w.currencyCode }}</td>
<td>{{ w.savings ? 'Da' : 'Ne' }}</td>
<td>{{ w.archived ? 'Da' : 'Ne' }}</td>
<td>{{ formatMoney(w.balance, w.currencyCode) }}</td>
<td style="display:flex; gap:8px;">
<button class="btn btn--ghost" @click="edit(w)">Izmeni</button>
<button class="btn btn--ghost" @click="toggleArchive(w)">{{ w.archived ? 'De-arhiviraj' : 'Arhiviraj' }}</button>
<button class="btn btn--ghost" @click="remove(w)">Obriši</button>
</td>
</tr>
<tr v-if="wallets.length === 0">
<td colspan="6" style="text-align:center; color:#888;">Nema rezultata.</td>
</tr>
</tbody>
</table>
</div>


<WalletForm v-if="showForm" :initial="editing" @close="closeForm" @saved="onSaved" />
</section>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useWalletsStore } from '@/stores/wallets'
import { useAuthStore } from '@/stores/auth'
import { useLiveSearch } from '@/composables/useLiveSearch'
import SearchInput from '@/components/SearchInput.vue'
import WalletForm from './WalletForm.vue'


const walletsStore = useWalletsStore()
const auth = useAuthStore()


const includeArchived = ref(false)
const { query, results, isLoading } = useLiveSearch((q, signal) => {
return walletsStore.fetchList(q, includeArchived.value, signal)
}, { immediate: true })


watch(includeArchived, () => {
// ponovo učitaj kada se promeni čekboks
walletsStore.fetchList(query.value, includeArchived.value)
})


const data = computed(() => results.value || walletsStore.list)
const wallets = computed(() => {
const q = (query.value || '').toLowerCase()
// Backend lista nema search parametar; zato filtriramo na FE dok ne omogućimo BE
return (Array.isArray(data.value) ? data.value : []).filter(w => !q || (w.name || '').toLowerCase().includes(q))
})


const showForm = ref(false)
const editing = ref(null)


function openCreate() { editing.value = null; showForm.value = true }
function edit(w) { editing.value = w; showForm.value = true }
function closeForm() { showForm.value = false }
async function onSaved() {
showForm.value = false
await walletsStore.fetchList(query.value, includeArchived.value)
}


async function toggleArchive(w) {
await walletsStore.setArchived(w.id, !w.archived)
}
async function remove(w) {
if (confirm('Potvrdi brisanje novčanika')) {
await walletsStore.remove(w.id)
}
}


function formatMoney(amount, code) {
try {
return new Intl.NumberFormat('sr-RS', { style: 'currency', currency: code || 'RSD' }).format(amount || 0)
} catch {
return `${amount ?? 0} ${code || ''}`
}
}
</script>

<style scoped>
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
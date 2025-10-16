<template>
<section class="card">
<header class="card__header">
<h2 style="margin:0">Ponavljajuće transakcije</h2>
<div style="display:flex; gap:8px; align-items:center;">
<button class="btn" @click="openCreate">+ Nova ponavljajuća</button>
<button class="btn btn--ghost" @click="runDue">Run due</button>
</div>
</header>
<div class="card__body">
<div style="display:flex; gap:12px; align-items:center; margin-bottom:12px;">
<SearchInput v-model="query" placeholder="Pretraži po nazivu..." />
</div>


<div v-if="isLoading" class="skeleton">Učitavanje...</div>
<table v-else class="table">
<thead>
<tr>
<th>Naziv</th>
<th>Iznos</th>
<th>Tip</th>
<th>Učestalost</th>
<th>Novčanik</th>
<th>Aktivna</th>
<th style="width:160px;">Akcije</th>
</tr>
</thead>
<tbody>
<tr v-for="r in items" :key="r.id">
<td>{{ r.name }}</td>
<td>{{ formatMoney(r.amount, r.currencyCode) }}</td>
<td>{{ r.type }}</td>
<td>{{ r.frequency }}</td>
<td>{{ r.walletName }}</td>
<td>{{ r.active ? 'Da' : 'Ne' }}</td>
<td style="display:flex; gap:8px;">
<button class="btn btn--ghost" @click="toggle(r)">{{ r.active ? 'Isključi' : 'Uključi' }}</button>
<button class="btn btn--ghost" @click="remove(r)">Obriši</button>
</td>
</tr>
<tr v-if="items.length===0">
<td colspan="7" style="text-align:center; color:#888;">Nema rezultata.</td>
</tr>
</tbody>
</table>
</div>


<RecurringForm v-if="showForm" @close="closeCreate" @saved="refetch" />
</section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRecurringsStore } from '@/stores/recurrings'
import { useLiveSearch } from '@/composables/useLiveSearch'
import SearchInput from '@/components/SearchInput.vue'
import RecurringForm from './RecurringForm.vue'


const store = useRecurringsStore()
const { query, results, isLoading } = useLiveSearch((q, signal) => store.fetchList(q, signal), { immediate: true })
const items = computed(() => results.value || store.items)


const showForm = ref(false)
function openCreate(){ showForm.value = true }
function closeCreate(){ showForm.value = false }
function refetch(){ store.fetchList(query.value) }


async function runDue(){ await store.runDue(); await refetch() }
async function toggle(r){ await store.toggleActive(r.id, !r.active) }
async function remove(r){ if(confirm('Obriši?')) await store.remove(r.id) }


function formatMoney(amount, code){ try{ return new Intl.NumberFormat('sr-RS',{style:'currency',currency:code||'RSD'}).format(amount||0)}catch{ return `${amount??0} ${code||''}` } }
</script>

<style scoped>
.skeleton { padding:12px; background:linear-gradient(90deg,#f2f2f2,#fafafa,#f2f2f2); background-size:200% 100%; animation:pulse 1.2s infinite linear; border-radius:12px; }
@keyframes pulse { 0%{background-position:200% 0} 100%{background-position:-200% 0} }
</style>
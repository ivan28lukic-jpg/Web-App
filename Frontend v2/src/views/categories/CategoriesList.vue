<template>
<section class="card">
<header class="card__header">
<h2 style="margin:0">Kategorije</h2>
<button class="btn" @click="openCreate">+ Nova kategorija</button>
</header>
<div class="card__body">
<SearchInput v-model="query" placeholder="Pretraži kategorije..." />
<div v-if="isLoading" class="skeleton" style="margin-top:12px;">Učitavanje...</div>


<table v-else class="table" style="margin-top:12px;">
<thead>
<tr>
<th>Naziv</th>
<th>Tip</th>
<th>Opseg</th>
<th style="width:120px;">Akcije</th>
</tr>
</thead>
<tbody>
<tr v-for="c in filtered" :key="c.id">
<td>{{ c.name }}</td>
<td>{{ c.type }}</td>
<td>
<span :style="badgeStyle(c)">{{ c._isGlobal ? 'Globalna' : 'Moja' }}</span>
</td>
<td>
<button class="btn btn--ghost" :disabled="c._isGlobal" @click="remove(c)">Obriši</button>
</td>
</tr>
<tr v-if="filtered.length===0"><td colspan="4" style="text-align:center; color:#888;">Nema rezultata.</td></tr>
</tbody>
</table>
</div>


<div v-if="showForm" class="modal">
<div class="modal__backdrop" @click="closeCreate"></div>
<div class="modal__dialog card">
<div class="card__header">
<h3 style="margin:0">Nova kategorija</h3>
<button class="btn btn--ghost" @click="closeCreate">Zatvori</button>
</div>
<div class="card__body">
<form @submit.prevent="save" class="grid">
<label>Naziv</label>
<input v-model="form.name" class="input" required />
<label>Tip</label>
<select v-model="form.type" class="select" required>
<option value="INCOME">Prihod</option>
<option value="EXPENSE">Trošak</option>
</select>
<div style="display:flex; gap:8px; justify-content:flex-end; margin-top:8px;">
<button class="btn btn--ghost" type="button" @click="closeCreate">Otkaži</button>
<button class="btn">Sačuvaj</button>
</div>
</form>
</div>
</div>
</div>
</section>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useCategoriesStore } from '@/stores/categories'
import { useLiveSearch } from '@/composables/useLiveSearch'
import SearchInput from '@/components/SearchInput.vue'


const store = useCategoriesStore()
const { query, results, isLoading } = useLiveSearch((q, signal) => store.fetchList(q, signal), { immediate: true })
const items = computed(() => results.value || store.items)


const filtered = computed(() => {
const q = (query.value||'').toLowerCase()
return (items.value||[]).filter(c => !q || (c.name||'').toLowerCase().includes(q))
})


const showForm = ref(false)
const form = ref({ name: '', type: 'EXPENSE' })
function openCreate(){ showForm.value = true }
function closeCreate(){ showForm.value = false }
async function save(){ await store.create({ ...form.value }); showForm.value=false }
async function remove(c){ if(confirm('Obriši kategoriju?')) await store.remove(c.id) }


function badgeStyle(c){ return { display:'inline-block', padding:'4px 8px', borderRadius:'999px', background: c._isGlobal? '#e8fff4' : '#e8f0ff', color: c._isGlobal? '#057a55' : '#1e40af', border:'1px solid #e0e0e0' } }
</script>

<style scoped>
.modal { position:fixed; inset:0; display:grid; place-items:center; }
.modal__backdrop { position:absolute; inset:0; background:rgba(0,0,0,0.45); }
.modal__dialog { position:relative; width:100%; max-width:520px; }
.grid { display:grid; gap:10px; }
.skeleton { padding:12px; background:linear-gradient(90deg,#f2f2f2,#fafafa,#f2f2f2); background-size:200% 100%; animation:pulse 1.2s infinite linear; border-radius:12px; }
@keyframes pulse { 0%{background-position:200% 0} 100%{background-position:-200% 0} }
</style>
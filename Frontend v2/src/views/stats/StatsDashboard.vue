<template>
<section class="card">
<header class="card__header">
<h2 style="margin:0">Statistika</h2>
<div style="display:flex; gap:8px; align-items:center;">
<select v-model="period" class="select">
<option value="DAY">Dan</option>
<option value="WEEK">Nedelja</option>
<option value="MONTH">Mesec</option>
<option value="QUARTER">Kvartal</option>
<option value="YEAR">Godina</option>
</select>
<input v-model="dateFrom" type="date" class="input" />
<input v-model="dateTo" type="date" class="input" />
<button class="btn btn--ghost" @click="refetch">Primeni</button>
</div>
</header>


<div class="card__body">
<div v-if="store.loading" class="skeleton">Učitavanje...</div>
<div v-else>
<div class="stats-grid">
<div class="stat">
<div class="stat__label">Prihodi</div>
<div class="stat__value">{{ money(store.summary?.income) }}</div>
</div>
<div class="stat">
<div class="stat__label">Troškovi</div>
<div class="stat__value">{{ money(store.summary?.expense) }}</div>
</div>
<div class="stat">
<div class="stat__label">Bilans</div>
<div class="stat__value">{{ money(store.summary?.balance) }}</div>
</div>
</div>


<h3>Kategorije</h3>
<div class="bars">
<div v-for="c in store.byCategory" :key="c.name" class="bar">
<div class="bar__label">{{ c.name }}</div>
<div class="bar__track"><div class="bar__fill" :style="{ width: barPct(c.amount)+'%' }"></div></div>
<div class="bar__value">{{ money(c.amount) }}</div>
</div>
</div>


<h3 style="margin-top:16px;">Top 10 troškova</h3>
<table class="table">
<thead><tr><th>Naziv</th><th>Iznos</th><th>Datum</th></tr></thead>
<tbody>
<tr v-for="t in store.topExpenses" :key="t.id">
<td>{{ t.name }}</td>
<td>{{ money(t.amount) }}</td>
<td>{{ date(t.occurredAt) }}</td>
</tr>
<tr v-if="!store.topExpenses?.length"><td colspan="3" style="text-align:center;color:#888;">Nema podataka.</td></tr>
</tbody>
</table>
</div>
</div>
</section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useStatsStore } from '@/stores/stats'


const store = useStatsStore()
const period = ref('MONTH')
const dateFrom = ref('')
const dateTo = ref('')


onMounted(()=> refetch())


function refetch(){ store.fetch(period.value, { dateFrom: dateFrom.value||undefined, dateTo: dateTo.value||undefined }) }
function money(v){ try{ return new Intl.NumberFormat('sr-RS',{ style:'currency', currency:'RSD' }).format(v||0) }catch{ return v??0 } }
function date(s){ if(!s) return ''; const d=new Date(s); return d.toLocaleDateString('sr-RS') }
function barPct(v){ const max = Math.max(1, ...store.byCategory.map(x=>x.amount||0)); return Math.min(100, Math.round(((v||0)/max)*100)) }
</script>

<style scoped>
.stats-grid { display:grid; grid-template-columns: repeat(auto-fit,minmax(180px,1fr)); gap:12px; margin-bottom:16px; }
.stat { background:var(--c-white); border:1px solid #eee; border-radius:12px; padding:12px; }
.stat__label { color:#666; font-size:14px; }
.stat__value { font-size:22px; font-weight:700; }
.bars { display:grid; gap:10px; }
.bar { display:grid; grid-template-columns: 1fr 5fr 1fr; gap:10px; align-items:center; }
.bar__track { height:10px; background:#f0f2f5; border-radius:999px; overflow:hidden; }
.bar__fill { height:100%; background: var(--c-green); }
.skeleton { padding:12px; background:linear-gradient(90deg,#f2f2f2,#fafafa,#f2f2f2); background-size:200% 100%; animation:pulse 1.2s infinite linear; border-radius:12px; }
@keyframes pulse { 0%{background-position:200% 0} 100%{background-position:-200% 0} }
</style>
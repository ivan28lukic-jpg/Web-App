<template>
<section class="card">
<header class="card__header">
<h2 style="margin:0">Ciljevi štednje</h2>
<button class="btn" @click="openCreate">+ Novi cilj</button>
</header>
<div class="card__body">
<SearchInput v-model="query" placeholder="Pretraži ciljeve..." />
<div v-if="isLoading" class="skeleton" style="margin-top:12px;">Učitavanje...</div>


<div v-else class="goals">
<div v-for="g in filtered" :key="g.id" class="goal card">
<div class="card__body">
<div class="goal__row">
<strong>{{ g.name }}</strong>
<span>{{ progressText(g) }}</span>
</div>
<div class="bar">
<div class="bar__fill" :style="{ width: percent(g)+'%' }"></div>
</div>
<div class="goal__meta">
<span>Cilj: {{ money(g.targetAmount, g.currencyCode) }}</span>
<span>Rok: {{ date(g.deadline) }}</span>
<span>Novčanik: {{ g.walletName }}</span>
</div>
<div style="display:flex; gap:8px; justify-content:flex-end; margin-top:8px;">
<button class="btn btn--ghost" @click="edit(g)">Izmeni</button>
<button class="btn btn--ghost" @click="remove(g)">Obriši</button>
</div>
</div>
</div>
<div v-if="filtered.length===0" style="text-align:center; color:#888; width:100%;">Nema rezultata.</div>
</div>
</div>


<GoalForm v-if="showForm" :initial="editing" @close="closeCreate" @saved="refetch" />
</section>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useGoalsStore } from '@/stores/goals'
import { useLiveSearch } from '@/composables/useLiveSearch'
import SearchInput from '@/components/SearchInput.vue'
import GoalForm from './GoalForm.vue'


const store = useGoalsStore()
const { query, results, isLoading } = useLiveSearch((q, signal) => store.fetchList(q, signal), { immediate: true })
const items = computed(() => results.value || store.items)
const filtered = computed(() => { const q=(query.value||'').toLowerCase(); return (items.value||[]).filter(g => !q || (g.name||'').toLowerCase().includes(q)) })


const showForm = ref(false)
const editing = ref(null)
function openCreate(){ editing.value=null; showForm.value=true }
function closeCreate(){ showForm.value=false }
function refetch(){ store.fetchList(query.value) }
function edit(g){ editing.value=g; showForm.value=true }
async function remove(g){ if(confirm('Obriši cilj?')) await store.remove(g.id) }


function percent(g){ const p = Math.min(100, Math.round(((g.currentAmount||0)/(g.targetAmount||1))*100)); return isFinite(p)? p:0 }
function progressText(g){ return `${money(g.currentAmount,g.currencyCode)} / ${money(g.targetAmount,g.currencyCode)}` }
function money(v,code){ try{ return new Intl.NumberFormat('sr-RS',{style:'currency',currency:code||'RSD'}).format(v||0)}catch{ return `${v??0} ${code||''}` } }
function date(s){ if(!s) return '-'; const d=new Date(s); return d.toLocaleDateString('sr-RS') }
</script>

<style scoped>
.goals { display:grid; grid-template-columns: repeat(auto-fill, minmax(280px,1fr)); gap:12px; }
.goal__row { display:flex; align-items:center; justify-content:space-between; margin-bottom:8px; }
.bar { height:10px; background:#f0f2f5; border-radius:999px; overflow:hidden; }
.bar__fill { height:100%; background: var(--c-green); }
.goal__meta { display:flex; flex-wrap:wrap; gap:12px; margin-top:8px; color:#555; font-size:14px; }
.skeleton { padding:12px; background:linear-gradient(90deg,#f2f2f2,#fafafa,#f2f2f2); background-size:200% 100%; animation:pulse 1.2s infinite linear; border-radius:12px; }
@keyframes pulse { 0%{background-position:200% 0} 100%{background-position:-200% 0} }
</style>
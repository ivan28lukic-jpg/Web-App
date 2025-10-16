<template>
<div class="modal">
<div class="modal__backdrop" @click="$emit('close')"></div>
<div class="modal__dialog card">
<div class="card__header">
<h3 style="margin:0">{{ initial ? 'Izmeni cilj' : 'Novi cilj' }}</h3>
<button class="btn btn--ghost" @click="$emit('close')">Zatvori</button>
</div>
<div class="card__body">
<form @submit.prevent="onSubmit" class="grid">
<label>Naziv</label>
<input v-model="form.name" class="input" required />


<label>Željeni iznos</label>
<input v-model.number="form.targetAmount" type="number" step="0.01" class="input" required />


<label>Rok</label>
<input v-model="form.deadline" type="date" class="input" />


<label>Novčanik (štedni)</label>
<select v-model="form.walletId" class="select" required>
<option v-for="w in wallets" :key="w.id" :value="w.id">{{ w.name }} ({{ w.currencyCode }})</option>
</select>


<div style="display:flex; gap:8px; justify-content:flex-end; margin-top:8px;">
<button class="btn btn--ghost" type="button" @click="$emit('close')">Otkaži</button>
<button class="btn" :disabled="loading">Sačuvaj</button>
</div>
</form>
</div>
</div>
</div>
</template>

<script setup>
import { ref, onMounted, computed, watchEffect } from 'vue'
import { useGoalsStore } from '@/stores/goals'
import { useWalletsStore } from '@/stores/wallets'


const props = defineProps({ initial: { type:Object, default:null } })
const emit = defineEmits(['close','saved'])
const store = useGoalsStore()
const walletsStore = useWalletsStore()
const loading = ref(false)


const form = ref({ name:'', targetAmount:0, deadline:'', walletId:null })


onMounted(async ()=>{
if(!walletsStore.list?.length) await walletsStore.fetchList('')
if(walletsStore.list?.length) form.value.walletId = walletsStore.list[0].id
})


watchEffect(()=>{
if(props.initial){
form.value = { name: props.initial.name, targetAmount: props.initial.targetAmount, deadline: props.initial.deadline?.slice(0,10)||'', walletId: props.initial.walletId }
}
})


const wallets = computed(()=> (walletsStore.list||[]).filter(w=> w.savings))


async function onSubmit(){
loading.value = true
try{
if(props.initial){ await store.update(props.initial.id, { ...form.value }) }
else { await store.create({ ...form.value }) }
emit('saved'); emit('close')
}catch(e){ alert('Greška pri čuvanju cilja') }
finally{ loading.value = false }
}
</script>

<style scoped>
.modal { position:fixed; inset:0; display:grid; place-items:center; }
.modal__backdrop { position:absolute; inset:0; background:rgba(0,0,0,0.45); }
.modal__dialog { position:relative; width:100%; max-width:560px; }
.grid { display:grid; gap:10px; }
</style>
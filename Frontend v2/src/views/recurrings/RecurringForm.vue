<template>
<div class="modal">
<div class="modal__backdrop" @click="$emit('close')"></div>
<div class="modal__dialog card">
<div class="card__header">
<h3 style="margin:0">Nova ponavljajuća</h3>
<button class="btn btn--ghost" @click="$emit('close')">Zatvori</button>
</div>
<div class="card__body">
<form @submit.prevent="onSubmit" class="grid">
<label>Naziv</label>
<input v-model="form.name" class="input" required />


<label>Iznos</label>
<input v-model.number="form.amount" class="input" type="number" step="0.01" required />


<label>Tip</label>
<select v-model="form.type" class="select" required>
<option value="INCOME">Prihod</option>
<option value="EXPENSE">Trošak</option>
</select>


<label>Učestalost</label>
<select v-model="form.frequency" class="select" required>
<option value="DAILY">Dnevno</option>
<option value="WEEKLY">Nedeljno</option>
<option value="MONTHLY">Mesečno</option>
<option value="YEARLY">Godišnje</option>
</select>


<label>Novčanik</label>
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
import { ref, onMounted, computed } from 'vue'
import { useRecurringsStore } from '@/stores/recurrings'
import { useWalletsStore } from '@/stores/wallets'


const emit = defineEmits(['close','saved'])
const loading = ref(false)
const store = useRecurringsStore()
const walletsStore = useWalletsStore()


const form = ref({ name:'', amount:0, type:'EXPENSE', frequency:'MONTHLY', walletId:null })


onMounted(async ()=>{
if(!walletsStore.list?.length) await walletsStore.fetchList('')
if(walletsStore.list?.length) form.value.walletId = walletsStore.list[0].id
})


const wallets = computed(()=> walletsStore.list || [])


async function onSubmit(){
loading.value = true
try{
await store.create({ ...form.value })
emit('saved')
emit('close')
}catch(e){ alert('Greška pri čuvanju') }
finally{ loading.value = false }
}
</script>

<style scoped>
.modal { position:fixed; inset:0; display:grid; place-items:center; }
.modal__backdrop { position:absolute; inset:0; background:rgba(0,0,0,0.45); }
.modal__dialog { position:relative; width:100%; max-width:560px; }
.grid { display:grid; gap:10px; }
</style>
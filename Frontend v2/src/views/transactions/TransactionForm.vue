<template>
<div class="modal">
<div class="modal__backdrop" @click="$emit('close')"></div>
<div class="modal__dialog card">
<div class="card__header">
<h3 style="margin:0">Nova transakcija</h3>
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


<label>Kategorija</label>
<select v-model="form.categoryId" class="select" required>
<option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
</select>


<label>Novčanik</label>
<select v-model="form.walletId" class="select" required>
<option v-for="w in walletsOptions" :key="w.id" :value="w.id">{{ w.name }}</option>
</select>


<label>Datum</label>
<input v-model="form.occurredAt" class="input" type="date" required />


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
import { useTransactionsStore } from '@/stores/transactions'
import { useWalletsStore } from '@/stores/wallets'
import { http } from '@/api/http'


const emit = defineEmits(['close', 'saved'])
const loading = ref(false)
const txStore = useTransactionsStore()
const walletsStore = useWalletsStore()


const form = ref({ name: '', amount: 0, type: 'EXPENSE', categoryId: null, walletId: null, occurredAt: today() })
const categories = ref([])


onMounted(async () => {
if (!walletsStore.list?.length) await walletsStore.fetchList('')
await loadCategories()
if (walletsStore.list?.length) form.value.walletId = walletsStore.list[0].id
})


async function loadCategories() {
// korisničke + predefinisane
categories.value = await http.get('/categories', { params: { scope: 'mine+global' } }).then(r => r.data)
}


async function onSubmit() {
loading.value = true
try {
await txStore.create({
name: form.value.name,
amount: Number(form.value.amount),
type: form.value.type,
categoryId: form.value.categoryId,
walletId: form.value.walletId,
occurredAt: form.value.occurredAt,
})
emit('saved')
emit('close')
} catch (e) {
alert('Greška pri čuvanju transakcije')
} finally {
loading.value = false
}
}


function today() {
const d = new Date()
const m = String(d.getMonth()+1).padStart(2,'0')
const day = String(d.getDate()).padStart(2,'0')
return `${d.getFullYear()}-${m}-${day}`
}
</script>

<style scoped>
.modal { position: fixed; inset: 0; display: grid; place-items: center; }
.modal__backdrop { position: absolute; inset: 0; background: rgba(0,0,0,0.45); }
.modal__dialog { position: relative; width: 100%; max-width: 560px; }
.grid { display: grid; gap: 10px; }
</style>
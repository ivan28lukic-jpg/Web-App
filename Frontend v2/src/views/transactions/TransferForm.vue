<template>
<div class="modal">
<div class="modal__backdrop" @click="$emit('close')"></div>
<div class="modal__dialog card">
<div class="card__header">
<h3 style="margin:0">Transfer</h3>
<button class="btn btn--ghost" @click="$emit('close')">Zatvori</button>
</div>
<div class="card__body">
<form @submit.prevent="onSubmit" class="grid">
<label>Sa novčanika</label>
<select v-model="form.fromWalletId" class="select" required>
<option v-for="w in wallets" :key="w.id" :value="w.id">{{ w.name }} ({{ w.currencyCode }})</option>
</select>


<label>Na novčanik</label>
<select v-model="form.toWalletId" class="select" required>
<option v-for="w in wallets" :key="w.id" :value="w.id">{{ w.name }} ({{ w.currencyCode }})</option>
</select>


<label>Iznos</label>
<input v-model.number="form.amount" class="input" type="number" step="0.01" required />


<label>Napomena</label>
<input v-model="form.note" class="input" placeholder="(opciono)" />


<div style="display:flex; gap:8px; justify-content:flex-end; margin-top:8px;">
<button class="btn btn--ghost" type="button" @click="$emit('close')">Otkaži</button>
<button class="btn" :disabled="loading">Prebaci</button>
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


const emit = defineEmits(['close', 'saved'])
const loading = ref(false)
const txStore = useTransactionsStore()
const walletsStore = useWalletsStore()


const form = ref({ fromWalletId: null, toWalletId: null, amount: 0, note: '' })


onMounted(async () => {
if (!walletsStore.list?.length) await walletsStore.fetchList('')
const ws = walletsStore.list || []
if (ws.length >= 2) {
form.value.fromWalletId = ws[0].id
form.value.toWalletId = ws[1].id
}
})


const wallets = computed(() => walletsStore.list || [])


async function onSubmit() {
loading.value = true
try {
await txStore.transfer({
fromWalletId: form.value.fromWalletId,
toWalletId: form.value.toWalletId,
amount: Number(form.value.amount),
note: form.value.note || undefined,
})
emit('saved')
emit('close')
} catch (e) {
alert('Greška pri transferu')
} finally {
loading.value = false
}
}
</script>

<style scoped>
.modal { position: fixed; inset: 0; display: grid; place-items: center; }
.modal__backdrop { position: absolute; inset: 0; background: rgba(0,0,0,0.45); }
.modal__dialog { position: relative; width: 100%; max-width: 560px; }
.grid { display: grid; gap: 10px; }
</style>
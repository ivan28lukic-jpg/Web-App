<template>
<div class="modal">
<div class="modal__backdrop" @click="$emit('close')"></div>
<div class="modal__dialog card">
<div class="card__header">
<h3 style="margin:0">{{ initial ? 'Izmeni novčanik' : 'Novi novčanik' }}</h3>
<button class="btn btn--ghost" @click="$emit('close')">Zatvori</button>
</div>
<div class="card__body">
<form @submit.prevent="onSubmit" class="grid">
<label>Naziv</label>
<input class="input" v-model="form.name" required />


<label>Valuta</label>
<select class="select" v-model="form.currencyCode" required>
<option value="RSD">RSD</option>
<option value="EUR">EUR</option>
<option value="USD">USD</option>
</select>


<label>Početno stanje</label>
<input class="input" v-model.number="form.initialBalance" type="number" step="0.01" />


<label style="display:flex; align-items:center; gap:8px;">
<input type="checkbox" v-model="form.savings" />
<span>Štedni novčanik</span>
</label>


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
import { ref, watchEffect } from 'vue'
import { useWalletsStore } from '@/stores/wallets'
import { useAuthStore } from '@/stores/auth'


const props = defineProps({ initial: { type: Object, default: null } })
const emit = defineEmits(['close', 'saved'])


const wallets = useWalletsStore()
const auth = useAuthStore()


const loading = ref(false)
const form = ref({ name: '', currencyCode: 'RSD', initialBalance: 0, savings: false })


watchEffect(() => {
if (props.initial) {
form.value = {
name: props.initial.name,
currencyCode: props.initial.currencyCode,
initialBalance: props.initial.balance ?? 0,
savings: !!props.initial.savings,
}
} else {
form.value = { name: '', currencyCode: 'RSD', initialBalance: 0, savings: false }
}
})


async function onSubmit() {
loading.value = true
try {
if (props.initial) {
await wallets.update(props.initial.id, {
name: form.value.name,
archived: props.initial.archived,
})
} else {
await wallets.create({
ownerId: Number(auth.userId),
name: form.value.name,
currencyCode: form.value.currencyCode,
initialBalance: Number(form.value.initialBalance || 0),
savings: !!form.value.savings,
})
}
emit('saved')
} catch (e) {
alert('Greška pri čuvanju novčanika')
} finally {
loading.value = false
}
}
</script>

<style scoped>
.modal {
position: fixed;
inset: 0;
display: grid;
place-items: center;
}
.modal__backdrop {
position: absolute;
inset: 0;
background: rgba(0,0,0,0.45);
}
.modal__dialog {
position: relative;
width: 100%;
max-width: 520px;
}
.grid { display: grid; gap: 10px; }
</style>
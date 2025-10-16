<template>
<section class="card">
<header class="card__header">
<h2 style="margin:0">Detalj novčanika</h2>
<RouterLink class="btn btn--ghost" to="/wallets">← Nazad</RouterLink>
</header>
<div class="card__body" v-if="wallet">
<div style="display:grid; grid-template-columns: repeat(2,1fr); gap: 16px;">
<div class="card">
<div class="card__body">
<div><strong>Naziv:</strong> {{ wallet.name }}</div>
<div><strong>Valuta:</strong> {{ wallet.currencyCode }}</div>
<div><strong>Štedni:</strong> {{ wallet.savings ? 'Da' : 'Ne' }}</div>
<div><strong>Arhiviran:</strong> {{ wallet.archived ? 'Da' : 'Ne' }}</div>
<div><strong>Stanje:</strong> {{ wallet.balance }}</div>
<div><strong>Kreiran:</strong> {{ wallet.createdAt }}</div>
</div>
</div>
<div class="card">
<div class="card__body">
<em>Ovde ćemo dodati listu transakcija po novčaniku.</em>
</div>
</div>
</div>
</div>
<div class="card__body" v-else>
Učitavanje...
</div>
</section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { http } from '@/api/http'
import { useRoute } from 'vue-router'


const route = useRoute()
const wallet = ref(null)


onMounted(async () => {
try {
wallet.value = await http.get(`/wallets/${route.params.id}`).then(r => r.data)
} catch (e) {
wallet.value = null
alert('Novčanik nije pronađen')
}
})
</script>
<template>
<section class="card">
<header class="card__header"><h2 style="margin:0">Profil</h2></header>
<div class="card__body">
<form @submit.prevent="save" class="grid" style="max-width:560px;">
<label>Ime</label>
<input v-model="form.firstName" class="input" />
<label>Prezime</label>
<input v-model="form.lastName" class="input" />
<label>Email</label>
<input v-model="form.email" class="input" type="email" />
<div style="display:flex; gap:8px; justify-content:flex-end; margin-top:8px;">
<button class="btn">Sačuvaj</button>
</div>
</form>
</div>
</section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '@/api/http'


const form = ref({ firstName:'', lastName:'', email:'' })


onMounted(async ()=>{
try{ const data = await http.get('/me').then(r=>r.data); form.value = { ...form.value, ...data } }catch{}
})


async function save(){
try{ await http.put('/me', form.value); alert('Sačuvano') }catch{ alert('Greška pri čuvanju') }
}
</script>
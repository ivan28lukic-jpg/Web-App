<template>
<div class="card" style="max-width:420px;margin:60px auto;">
<div class="card__header"><h2 style="margin:0">Prijava</h2></div>
<div class="card__body">
<form @submit.prevent="onSubmit" class="form">
<div class="form__row">
<label>Korisničko ime</label>
<input v-model="username" class="input" autocomplete="username" />
</div>
<div class="form__row">
<label>Lozinka</label>
<input v-model="password" class="input" type="password" autocomplete="current-password" />
</div>
<div class="form__row" style="display:flex;gap:10px;justify-content:flex-end">
<button class="btn" :disabled="loading">Prijavi se</button>
</div>
</form>
</div>
</div>
</template>


<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'


const auth = useAuthStore()
const router = useRouter()


const username = ref('')
const password = ref('')
const loading = ref(false)


async function onSubmit() {
loading.value = true
try {
await auth.login(username.value, password.value)
router.push('/dashboard')
} catch (e) {
alert('Neuspešna prijava')
} finally {
loading.value = false
}
}
</script>


<style scoped>
.form { display: grid; gap: 12px; }
.form__row { display: grid; gap: 6px; }
label { font-size: 14px; color: #333; }
</style>
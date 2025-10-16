<template>
<div>
<nav class="navbar" v-if="auth.isLoggedIn">
<span class="navbar__brand">WebFinance</span>
<RouterLink to="/wallets">Novčanici</RouterLink>
<RouterLink to="/transactions">Transakcije</RouterLink>
<RouterLink to="/stats">Statistika</RouterLink>
<RouterLink to="/goals">Ciljevi</RouterLink>
<RouterLink to="/categories">Kategorije</RouterLink>
<RouterLink to="/profile">Profil</RouterLink>
<span style="flex:1"></span>
<button class="btn btn--ghost" @click="logout">Odjava</button>
</nav>


<div class="container">
<RouterView />
</div>
</div>
</template>


<script setup>
import { useAuthStore } from './stores/auth'
import { useRouter } from 'vue-router'


const auth = useAuthStore()
const router = useRouter()


function logout() {
auth.logout()
router.push('/login')
}
</script>
<template>
    <div class="container page">
      <div class="card" style="margin-bottom: 32px;">
        <h1>Dobrodošli u WebFinanceApp!</h1>
        <p>Opis aplikacije, slike, benefiti...</p>
  
        <div class="muted" style="font-size: 1.15rem; margin-top: 18px;">
          <b>Broj registrovanih korisnika:</b>
          <span class="brand" style="font-size: 1.25rem;">{{ userCount }}</span>
        </div>
      </div>
      <!-- Možeš dodati dugme za registraciju/prijavu -->
      <RouterLink to="/login" class="btn btn--primary" style="margin-top: 18px;">Prijavi se</RouterLink>
      <RouterLink to="/register" class="btn btn--secondary" style="margin-left: 8px;">Registruj se</RouterLink>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from "vue";
  import api from "@/services/api"; // koristiš svoj axios instance
  
  const userCount = ref(null);
  
  onMounted(async () => {
    try {
      const { data } = await api.get("/users/count");
      userCount.value = data;
    } catch (e) {
      userCount.value = "N/A";
    }
  });
  </script>
  
  <style scoped>
  .brand {
    color: var(--brand);
    font-weight: 700;
  }
  </style>
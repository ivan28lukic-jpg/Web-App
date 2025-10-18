<template>
    <div class="container page">
      <div class="card" style="margin-bottom: 32px;">
        <h1>Welcome to WebFinanceApp!</h1>
        <p>Description of the application, images, benefits...</p>

        <div class="muted" style="font-size: 1.15rem; margin-top: 18px;">
          <b>Number of registered users: </b>
          <span class="brand" style="font-size: 1.25rem;">{{ userCount }}</span>
        </div>
      </div>
      <!-- You can add a button for registration/login -->
      <RouterLink to="/login" class="btn btn--primary" style="margin-top: 18px;">Log in</RouterLink>
      <RouterLink to="/register" class="btn btn--secondary" style="margin-left: 8px;">Register</RouterLink>
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
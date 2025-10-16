<template>
    <section>
      <h1>Statistika prihoda i troškova</h1>
      <components-stats :owner-id="userId" />
    </section>
  </template>
  
  <script setup>
  // Importuj komponentu (prilagodi putanju gde ti je fajl!)
  import ComponentsStats from "@/components/componentsStats.vue";
  // Ako ti je u subfolderu: "@/components/stats/componentsStats.vue"
  
  import { useAuthStore } from "@/stores/auth";
    const authStore = useAuthStore();
    const ownerId = authStore.user?.id || localStorage.getItem("ownerId");
  </script>







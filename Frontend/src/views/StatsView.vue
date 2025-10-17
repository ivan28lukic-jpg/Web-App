<template>
    <section>
      <h1>Statistic</h1>
      <components-stats
            :owner-id="authStore.isAdmin ? undefined : ownerId"
             :is-admin="authStore.isAdmin"
/>
      />
    </section>
  </template>
  
  <script setup>
  import ComponentsStats from "@/components/componentsStats.vue";
  import { useAuthStore } from "@/stores/auth";
  const authStore = useAuthStore();
  const ownerId = authStore.user?.id || Number(localStorage.getItem("ownerId"));
    console.log("authStore.isAdmin:", authStore.isAdmin);
    console.log("authStore.user?.role:", authStore.user?.role);
    console.log("ownerId:", ownerId);
  </script>
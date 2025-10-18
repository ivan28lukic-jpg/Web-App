<template>
  <section>
    <h1>Statistika</h1>
    <components-stats
      :owner-id="authStore.isAdmin ? undefined : ownerId"
      :is-admin="authStore.isAdmin"
    />
    <!-- Prikaz Top 10 troškova samo za običnog korisnika -->
    <TopExpenses v-if="!authStore.isAdmin" />
  </section>
</template>

<script setup>
import ComponentsStats from "@/components/componentsStats.vue";
import TopExpenses from "@/components/TopExpenses.vue";
import { useAuthStore } from "@/stores/auth";
const authStore = useAuthStore();
const ownerId = authStore.user?.id || Number(localStorage.getItem("ownerId"));
</script>
<template>
  <AppNav />
  <router-view />
  <ToastHost />
</template>

<script setup>
import AppNav from "@/components/AppNav.vue";
import ToastHost from "@/components/ToastHost.vue";
</script>
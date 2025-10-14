<template>
  <div class="toast-host">
    <transition-group name="toast" tag="div">
      <div
        v-for="t in toast.items"
        :key="t.id"
        class="toast"
        :data-type="t.type"
      >
        <span class="dot"></span>
        <p class="msg">{{ t.message }}</p>
        <button class="btn btn--ghost btn--sm" @click="toast.remove(t.id)">×</button>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { useToast } from "@/composables/useToast";
const toast = useToast();
</script>

<style scoped>
.toast-host {
  position: fixed;
  right: 16px;
  bottom: 16px;
  display: grid;
  gap: 10px;
  z-index: 9999;
}

.toast {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 10px;
  background: rgba(14, 20, 17, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  padding: 10px 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,.3);
  min-width: 260px;
  max-width: 420px;
}

.toast .msg { margin: 0; }
.toast .dot {
  width: 8px; height: 8px; border-radius: 50%;
  box-shadow: 0 0 10px currentColor;
}
.toast[data-type="info"]    .dot { color: #93c5fd; }
.toast[data-type="success"] .dot { color: var(--brand); }
.toast[data-type="error"]   .dot { color: var(--danger); }
.toast[data-type="warning"] .dot { color: #fbbf24; }

.toast-enter-from,
.toast-leave-to { opacity: 0; transform: translateY(10px); }
.toast-enter-active,
.toast-leave-active { transition: all 140ms ease; }
</style>
<template>
  <div v-if="modelValue" class="cd__backdrop" @click.self="cancel">
    <div class="cd__card card">
      <h3 style="margin-top:0">{{ title }}</h3>
      <p class="muted" v-if="message">{{ message }}</p>
      <div style="display:flex; gap:12px; justify-content:flex-end; margin-top:14px;">
        <button class="btn btn--secondary" @click="cancel">{{ cancelText }}</button>
        <button class="btn btn--danger" @click="confirm">{{ confirmText }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: "Are you sure?" },
  message: { type: String, default: "" },
  confirmText: { type: String, default: "Confirm" },
  cancelText: { type: String, default: "Cancel" },
});

const emit = defineEmits(["update:modelValue", "confirm", "cancel"]);

function cancel() {
  emit("update:modelValue", false);
  emit("cancel");
}
function confirm() {
  emit("update:modelValue", false);
  emit("confirm");
}
</script>

<style scoped>
.cd__backdrop{
  position: fixed; inset: 0;
  display: grid; place-items: center;
  background: rgba(0,0,0,0.45);
  z-index: 9998;
  padding: 16px;
}
.cd__card { max-width: 460px; width: 100%; }
.muted { color: var(--muted); }
</style>

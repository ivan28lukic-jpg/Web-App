<template>
  <div v-if="modelValue" class="ac__backdrop" @click.self="close">
    <div class="ac__card card">
      <h3 style="margin:0 0 8px 0;">
        {{ archived ? 'Restore wallet' : 'Archive wallet' }}
      </h3>

      <p class="muted" style="margin:0;">
        You’re about to {{ archived ? 'restore' : 'archive' }} wallet
        <strong>“{{ wallet?.name }}”</strong>.
      </p>

      <ul class="muted" style="margin:10px 0 12px 18px;">
        <li>Wallet will stay in your account (not deleted).</li>
        <li>You can {{ archived ? 'archive it again' : 'restore it later' }} anytime.</li>
        <li>Transactions remain intact.</li>
      </ul>

      <label class="check" style="margin-bottom: 12px;">
        <input type="checkbox" v-model="ack" />
        <span>I understand the consequences of this action.</span>
      </label>

      <div class="ac__actions">
        <button class="btn btn--secondary" @click="close">Cancel</button>
        <button
          class="btn btn--primary"
          :disabled="!ack"
          @click="confirm"
        >
          {{ archived ? 'Restore' : 'Archive' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from "vue";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  wallet: { type: Object, default: null },
  archived: { type: Boolean, default: false },
});

const emit = defineEmits(["update:modelValue", "confirm", "cancel"]);

const ack = ref(false);

watch(
  () => props.modelValue,
  (open) => { if (open) ack.value = false; }
);

function close() {
  emit("update:modelValue", false);
  emit("cancel");
}

function confirm() {
  if (!ack.value) return;
  emit("update:modelValue", false);
  emit("confirm");
}
</script>

<style scoped>
.ac__backdrop {
  position: fixed; inset: 0; display: grid; place-items: center;
  background: rgba(0,0,0,0.45); z-index: 9998; padding: 16px;
}
.ac__card { width: 100%; max-width: 560px; }
.ac__actions { display: flex; gap: 12px; justify-content: flex-end; }
.muted { color: var(--muted); }
</style>
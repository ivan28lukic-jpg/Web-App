<template>
  <!-- Overlay; klik na tamni sloj zatvara -->
  <div
    class="modal"
    @click.self="handleClose"
    tabindex="0"
    @keydown.esc.prevent="handleClose"
  >
    <div class="card">
      <header class="card_head">
        <h3 class="card_title">New goal</h3>
        <button class="x" type="button" @click="handleClose">✕</button>
      </header>

      <div class="grid">
        <label class="full">
          <span>Name</span>
          <input v-model.trim="form.name" type="text" placeholder="Goal name" />
        </label>

        <label class="full">
          <span>Wallet</span>
          <select v-model="form.walletId">
            <option value="" disabled>Choose a savings wallet</option>
            <option
              v-for="w in wallets"
              :key="w.id"
              :value="w.id"
            >
              {{ walletLabel(w) }}
            </option>
          </select>
          <small>Choose the wallet used for this saving goal.</small>
        </label>

        <label>
          <span>Target amount</span>
          <input v-model.trim="form.targetAmount" type="number" step="0.01" placeholder="e.g. 60000" />
        </label>

        <label>
          <span>Due date</span>
          <input v-model="form.dueDate" type="date" />
        </label>

        <label class="full">
          <span>Description</span>
          <input v-model.trim="form.description" type="text" placeholder="Optional note" />
        </label>
      </div>

      <footer class="card_foot">
        <button class="btn" type="button" @click="handleClose">Cancel</button>
        <button class="btn btn-primary" type="button" :disabled="saving" @click="handleCreate">
          Create
        </button>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed } from "vue";
import api from "@/services/api";

// Props & emits
const props = defineProps({
  ownerId: { type: Number, required: true },
  wallets: { type: Array, default: () => [] }, // lista *savings* novčanika
});
const emit = defineEmits(["close", "created"]);

const saving = reactive({ v: false });

const form = reactive({
  name: "",
  walletId: "",
  targetAmount: "",
  dueDate: "",
  description: "",
});

function handleClose() {
  emit("close");
}

function walletLabel(w) {
  // pokušaj da složiš lep prikaz; prilagodi po svom modelu
  const cur = w.currencyCode ?? w.currency?.code ?? "";
  return `${w.name} (${cur})`;
}

async function handleCreate() {
  if (!form.name || !form.walletId || !form.targetAmount || !form.dueDate) {
    alert("Please fill in name, wallet, target and due date.");
    return;
  }
  saving.v = true;
  try {
    // backend ruta za kreiranje saving goala (prilagodi ako treba)
    await api.post("/saving-goals", {
      ownerId: props.ownerId,
      walletId: Number(form.walletId),
      name: form.name,
      targetAmount: Number(form.targetAmount),
      dueDate: form.dueDate, // ISO yyyy-mm-dd
      description: form.description || undefined,
    });
    emit("created");    // roditelj će refrešovati listu
    emit("close");      // i zatvoriti modal
  } catch (e) {
    console.error(e);
    alert("Create failed.");
  } finally {
    saving.v = false;
  }
}
</script>

<style scoped>
/* Overlay */
.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: grid;
  place-items: center;
  z-index: 1000;
}

/* Card */
.card {
  width: min(680px, 92vw);
  background: #0f1a14;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
}

/* Header */
.card_head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.card_title {
  font-weight: 600;
}

/* Grid form */
.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px 16px;
  padding: 18px;
}
.full {
  grid-column: 1 / -1;
}

label span {
  display: block;
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 6px;
}

input,
select {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
  color: #e9f7ef;
  outline: none;
}

small {
  display: block;
  font-size: 11px;
  opacity: 0.7;
  margin-top: 6px;
}

/* Footer */
.card_foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 18px 18px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.btn {
  height: 36px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.04);
  color: #e9f7ef;
}
.btn-primary {
  border-color: transparent;
  background: linear-gradient(135deg, #2bb56a, #1f8a4f);
}
.x {
  height: 32px;
  width: 32px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.04);
  color: #e9f7ef;
}
</style>

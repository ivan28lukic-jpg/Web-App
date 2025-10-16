<template>
  <div v-if="modelValue" class="tm__backdrop" @click.self="close">
    <div class="tm__card card">
      <h3 class="tm__title">{{ isEdit ? "Edit transaction" : "New transaction" }}</h3>
      <p class="tm__subtitle">{{ isEdit ? "Update transaction details" : "Add a new transaction" }}</p>

      <form class="form" @submit.prevent="submit">
        <div class="form__row form__row--2">
          <Select
            label="Wallet"
            v-model="form.walletId"
            :options="walletOpts"
            placeholder="Choose wallet"
            :error="errors.walletId"
          />
          <Select
            label="Type"
            v-model="form.type"
            :options="typeOpts"
            placeholder="Choose type"
            :error="errors.type"
          />
        </div>

        <div class="form__row form__row--2">
          <Select
            label="Category"
            v-model="form.categoryId"
            :options="categoryOpts"
            placeholder="Choose category"
          />
          <Input
            label="Amount"
            v-model="form.amount"
            type="number"
            step="0.01"
            placeholder="0.00"
            :error="errors.amount"
          />
        </div>

        <div class="form__row form__row--2">
          <Input
            label="Date"
            v-model="form.date"
            type="date"
          />
          <Input
            label="Description"
            v-model="form.description"
            placeholder="Optional note"
          />
        </div>

        <div class="tm__actions">
          <button type="button" class="btn btn--secondary" @click="close">Cancel</button>
          <button type="submit" class="btn btn--primary" :disabled="loading" :class="{ 'is-loading': loading }">
            {{ isEdit ? "Save changes" : "Create" }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from "vue";
import Input from "@/components/Input.vue";
import Select from "@/components/Select.vue";
import { useWalletsStore } from "@/stores/storeWallets";
import { useCategoriesStore } from "@/stores/storeCategories"; // ako nemaš još, privremeno napravi prost store ili zameni options ručno

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  tx: { type: Object, default: null }, // transaction za edit
});
const emit = defineEmits(["update:modelValue", "save", "closed"]);

const wallets = useWalletsStore();
const categories = useCategoriesStore?.() ?? { items: [] }; // fallback ako store ne postoji

const loading = ref(false);
const isEdit = computed(() => !!props.tx);

const form = reactive({
  walletId: "",
  type: "",
  categoryId: "",
  amount: "",
  date: "",
  description: "",
});

const errors = reactive({
  walletId: "",
  type: "",
  amount: "",
});

const typeOpts = [
  { value: "INCOME", label: "Income" },
  { value: "EXPENSE", label: "Expense" },
];

const walletOpts = computed(() =>
  wallets.items.map((w) => ({ value: String(w.id), label: w.name }))
);

const categoryOpts = computed(() =>
  (categories.items || []).map((c) => ({ value: String(c.id), label: c.name }))
);

watch(
  () => props.tx,
  (t) => {
    if (t) {
      form.walletId = String(t.walletId ?? "");
      form.type = t.type || "";
      form.categoryId = t.categoryId != null ? String(t.categoryId) : "";
      form.amount = t.amount != null ? String(t.amount) : "";
      form.date = (t.date || t.createdAt || "").slice(0, 10);
      form.description = t.description || "";
    } else {
      form.walletId = "";
      form.type = "";
      form.categoryId = "";
      form.amount = "";
      form.date = new Date().toISOString().slice(0, 10);
      form.description = "";
    }
    errors.walletId = errors.type = errors.amount = "";
  },
  { immediate: true }
);

function validate() {
  errors.walletId = form.walletId ? "" : "Wallet is required";
  errors.type = form.type ? "" : "Type is required";
  errors.amount = form.amount ? "" : "Amount is required";
  return !errors.walletId && !errors.type && !errors.amount;
}

async function submit() {
  if (!validate()) return;
  loading.value = true;
  try {
    const normalize = (v) => (typeof v === "string" ? v.replace(",", ".") : v);
    const payload = {
      walletId: Number(form.walletId),
      type: form.type,
      amount: Number(normalize(form.amount)),
      categoryId: form.categoryId ? Number(form.categoryId) : undefined,
      description: form.description || undefined,
      date: form.date || undefined, // backend može ignorisati ako generiše sam
    };
    emit("save", payload);
  } finally {
    loading.value = false;
  }
}

function close() {
  emit("update:modelValue", false);
  emit("closed");
}
</script>

<style scoped>
.tm__backdrop {
    position: fixed;
    inset: 0;
    display: grid;
    place-items: center;
    background: rgba(0, 0, 0, 0.45);
    z-index: 9997;
    padding: 16px;
}

.tm__card {
    width: 100%;
    max-width: 900px;
}

.tm__title {
    margin: 0 0 6px 0;
}

.tm__subtitle {
    margin: 0 0 12px 0;
    color: var(--muted);
}

.tm__actions {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
}
</style>
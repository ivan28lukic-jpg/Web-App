<template>
  <div v-if="modelValue" class="wm__backdrop" @click.self="onClose">
    <div class="wm__card card">
      <h3 class="wm__title">{{ isEdit ? "Edit wallet" : "New wallet" }}</h3>
      <p class="wm__subtitle">
        {{ isEdit ? "Update wallet details" : "Create a new wallet" }}
      </p>

      <form class="form" @submit.prevent="submit">
        <!-- NAME (uvek) -->
        <Input
          id="wm-name"
          label="Name"
          v-model="form.name"
          placeholder="My wallet"
          :error="errors.name"
        >
          <template #left>💼</template>
        </Input>

        <!-- CURRENCY & BALANCE (samo kod CREATE) -->
        <div v-if="!isEdit" class="form__row form__row--2">
          <Select
            id="wm-curr"
            label="Currency"
            v-model="form.currencyCode"
            :options="currencies"
            placeholder="Choose currency"
            :error="errors.currencyCode"
          >
            <template #left>💱</template>
          </Select>

          <Input
            id="wm-balance"
            label="Initial balance"
            v-model="form.balance"
            type="number"
            step="0.01"
            placeholder="0.00"
          >
            <template #left>💰</template>
          </Input>
        </div>

        <!-- ARCHIVED (uklonjeno iz edit moda) -->
        <div v-if="!isEdit">
          <Checkbox v-model="form.archived">Archived</Checkbox>
        </div>

        <div class="wm__actions">
          <button type="button" class="btn btn--secondary" @click="onClose">
            Cancel
          </button>
          <button
            type="submit"
            class="btn btn--primary"
            :class="{ 'is-loading': loading }"
            :disabled="loading"
          >
            {{ isEdit ? "Save changes" : "Create" }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch } from "vue";
import Input from "@/components/Input.vue";
import Select from "@/components/Select.vue";
import Checkbox from "@/components/Checkbox.vue";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  wallet: { type: Object, default: null },
  currencies: { type: Array, default: () => [] },
});
const emit = defineEmits(["update:modelValue", "save", "closed"]);

const isEdit = computed(() => !!props.wallet);
const loading = ref(false);

const form = reactive({
  name: "",
  currencyCode: "",
  balance: "",
  archived: false,
});

const errors = reactive({ name: "", currencyCode: "" });

watch(
  () => props.wallet,
  (w) => {
    if (w) {
      // EDIT mode: samo ime; ostala polja ćemo ignorisati
      form.name = w.name || "";
      form.currencyCode = w.currencyCode || "";
      form.balance = w.balance ?? "";
      form.archived = !!w.archived;
    } else {
      // CREATE mode: reset
      form.name = "";
      form.currencyCode = "";
      form.balance = "";
      form.archived = false;
    }
    errors.name = "";
    errors.currencyCode = "";
  },
  { immediate: true }
);

function validate() {
  errors.name = form.name ? "" : "Name is required";
  // valuta je obavezna samo kod CREATE
  errors.currencyCode = isEdit.value
    ? ""
    : form.currencyCode
    ? ""
    : "Currency is required";

  return !errors.name && !errors.currencyCode;
}

async function submit() {
  if (!validate()) return;
  loading.value = true;
  try {
    let payload;
    if (isEdit.value) {
      // U EDIT režimu šaljemo samo ono što sme da se menja
      payload = { name: form.name };
    } else {
      // U CREATE režimu šaljemo sve
      const normalize = (v) =>
        typeof v === "string" ? v.replace(",", ".") : v;
      payload = {
        name: form.name,
        currencyCode: form.currencyCode,
        balance:
          form.balance === "" ? undefined : Number(normalize(form.balance)),
        archived: form.archived,
      };
    }
    emit("save", payload);
  } finally {
    loading.value = false;
  }
}

function onClose() {
  emit("update:modelValue", false);
  emit("closed");
}
</script>

<style scoped>
.wm__backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: grid;
  place-items: center;
  padding: 16px;
  z-index: 9997;
}
.wm__card {
  width: 100%;
  max-width: 520px;
}
.wm__title {
  margin: 0 0 6px 0;
}
.wm__subtitle {
  margin: 0 0 12px 0;
  color: var(--muted);
}
.wm__actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>
<template>
  <div v-if="modelValue" class="cm__backdrop" @click.self="close">
    <div class="cm__card card">
      <h3 class="cm__title">{{ isEdit ? "Edit category" : "New category" }}</h3>
      <p class="cm__subtitle">{{ isEdit ? "Update category details" : "Create a new category" }}</p>

      <form class="form" @submit.prevent="submit">
        <div class="form__row form__row--2">
          <Input
            id="cat-name"
            label="Name"
            v-model="form.name"
            placeholder="e.g. Salary, Groceries…"
            :error="errors.name"
          />
          <Select
            id="cat-type"
            label="Type"
            v-model="form.type"
            :options="typeOpts"
            placeholder="Choose type"
            :error="errors.type"
          />
        </div>

        <div class="cm__actions">
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
import { reactive, ref, computed, watch } from "vue";
import Input from "@/components/Input.vue";
import Select from "@/components/Select.vue";

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  category: { type: Object, default: null }, // za edit
});
const emit = defineEmits(["update:modelValue", "save", "closed"]);

const loading = ref(false);
const isEdit = computed(() => !!props.category);

const form = reactive({
  name: "",
  type: "", // INCOME | EXPENSE
});
const errors = reactive({ name: "", type: "" });

const typeOpts = [
  { value: "INCOME", label: "Income" },
  { value: "EXPENSE", label: "Expense" },
];

watch(
  () => props.category,
  (c) => {
    if (c) {
      form.name = c.name || "";
      form.type = (c.type || "").toUpperCase();
    } else {
      form.name = "";
      form.type = "";
    }
    errors.name = errors.type = "";
  },
  { immediate: true }
);

function validate() {
  errors.name = form.name ? "" : "Name is required";
  errors.type = form.type ? "" : "Type is required";
  return !errors.name && !errors.type;
}

async function submit() {
  if (!validate()) return;
  loading.value = true;
  try {
    emit("save", { name: form.name, type: String(form.type).toUpperCase() });
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
.cm__backdrop { position: fixed; inset: 0; display: grid; place-items: center; background: rgba(0,0,0,0.45); z-index: 9997; padding: 16px; }
.cm__card { width: 100%; max-width: 720px; }
.cm__title { margin: 0 0 6px 0; }
.cm__subtitle { margin: 0 0 12px 0; color: var(--muted); }
.cm__actions { display: flex; gap: 12px; justify-content: flex-end; }
</style>
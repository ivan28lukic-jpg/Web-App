<template>
    <div class="container page">
      <h2>Admin: Currencies</h2>
      <div>
        <button class="btn btn--primary" @click="openCreate">+ Add Currency</button>
      </div>
  
      <table class="wf__table" v-if="currencies.items.length">
        <thead>
          <tr>
            <th>Code</th>
            <th>Name</th>
            <th>Value vs EUR</th>
            <th>Updated</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in currencies.items" :key="c.id">
            <td>{{ c.code }}</td>
            <td>{{ c.name }}</td>
            <td>{{ c.valueVsEur }}</td>
            <td>{{ c.updatedAt }}</td>
            <td>
              <button class="btn btn--secondary btn--sm" @click="openEdit(c)">Edit</button>
              <button class="btn btn--danger btn--sm" @click="deleteCurrency(c.id)">Delete</button>
              <button class="btn btn--outline btn--sm" @click="refreshCurrency(c.id)">Refresh</button>
            </td>
          </tr>
        </tbody>
      </table>
  
      <div v-if="currencies.loading">Loading…</div>
      <div v-if="currencies.error" class="muted">{{ currencies.error }}</div>
  
      <!-- Modal -->
      <div v-if="modalOpen" class="modal">
        <form @submit.prevent="onSave">
          <h3>{{ editing ? "Edit" : "Add" }} Currency</h3>
          <label>
            Code:
            <input v-model="form.code" maxlength="3" @blur="fetchSuggestion" :disabled="editing" />
          </label>
          <label>
            Name:
            <input v-model="form.name" maxlength="64" />
          </label>
          <label>
            Value vs EUR:
            <input v-model.number="form.valueVsEur" type="number" step="0.000001" />
            <span v-if="currencies.suggestValue && !form.valueVsEur" class="muted">
              Suggestion: {{ currencies.suggestValue }} (source: {{ currencies.suggestSource }})
              <button type="button" class="btn btn--sm" @click="useSuggestion">Use</button>
            </span>
          </label>
          <div style="margin-top:16px;">
            <button class="btn btn--primary" type="submit">{{ editing ? "Save" : "Add" }}</button>
            <button class="btn btn--outline" type="button" @click="closeModal">Cancel</button>
          </div>
        </form>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from "vue";
import { useCurrenciesStore } from "@/stores/storeCurrencies"; // Ensure the correct path and module export
const currencies = useCurrenciesStore();
  
  const modalOpen = ref(false);
  const editing = ref(false);
  const form = ref({ code: "", name: "", valueVsEur: null });
  let editId = null;
  
  function openCreate() {
    modalOpen.value = true;
    editing.value = false;
    form.value = { code: "", name: "", valueVsEur: null };
    currencies.suggestValue = null;
  }
  
  function openEdit(c) {
    modalOpen.value = true;
    editing.value = true;
    form.value = { code: c.code, name: c.name, valueVsEur: c.valueVsEur };
    editId = c.id;
    currencies.suggestValue = null;
  }
  
  function closeModal() {
    modalOpen.value = false;
    editing.value = false;
    form.value = { code: "", name: "", valueVsEur: null };
    editId = null;
    currencies.suggestValue = null;
  }
  
  async function onSave() {
    try {
      if (editing.value) {
        await currencies.updateOne(editId, { name: form.value.name, valueVsEur: form.value.valueVsEur });
      } else {
        await currencies.createOne(form.value);
      }
      closeModal();
    } catch (e) { /* error handled in store */ }
  }
  
  async function deleteCurrency(id) {
    if (!confirm("Delete currency?")) return;
    await currencies.removeOne(id);
  }
  
  async function refreshCurrency(id) {
    await currencies.refreshCurrency(id);
  }
  
  async function fetchSuggestion() {
    if (form.value.code && form.value.code.length === 3) {
      await currencies.fetchSuggestion(form.value.code);
    }
  }
  
  function useSuggestion() {
    if (currencies.suggestValue) {
      form.value.valueVsEur = currencies.suggestValue;
    }
  }
  
  onMounted(() => currencies.fetchAll());
  </script>
  
  <style scoped>
  .muted { color: #94a3b8; font-size: 13px; }
  .modal {
    position: fixed; left: 0; top: 0; width: 100vw; height: 100vh;
    background: rgba(0,0,0,0.18); display: flex; align-items: center; justify-content: center;
  }
  .modal form { background: #fff; padding: 24px; border-radius: 8px; min-width: 320px; }
  label { display: block; margin-bottom: 12px;}
  input[type="number"], input[type="text"] { width: 100px; }
  .wf__table { width:100%; border-collapse:collapse; }
  .wf__table th, .wf__table td { padding: 8px 12px; border-bottom: 1px solid #e5e7eb; }
  </style>
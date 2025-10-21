<template>
    <div class="container page currencies-page">
      <h2>Admin: Currencies</h2>
      <div class="currencies-header">
        <button class="btn btn--primary" @click="openCreate">+ Add Currency</button>
      </div>
      <div class="currencies-table-wrap">
        <table class="currencies-table" v-if="currencies.items.length">
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Value vs EUR</th>
              <th>Updated</th>
              <th class="actions-col">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in currencies.items" :key="c.id">
              <td>{{ c.code }}</td>
              <td>{{ c.name }}</td>
              <td>{{ c.valueVsEur }}</td>
              <td>{{ formatDate(c.updatedAt) }}</td>
              <td class="actions-col">
                <div class="action-buttons">
                  <button class="action-btn edit" @click="openEdit(c)">Edit</button>
                  <button class="action-btn delete" @click="deleteCurrency(c.id)">Delete</button>
                  <button class="action-btn refresh" @click="refreshCurrency(c.id)">Refresh</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="currencies.loading" class="muted mt-2">Loading…</div>
      <div v-if="currencies.error" class="muted mt-2">{{ currencies.error }}</div>
  
      <!-- Modal -->
      <div v-if="modalOpen" class="currency-modal-backdrop">
        <div class="currency-modal-card">
          <form @submit.prevent="onSave" autocomplete="off">
            <h3 class="currency-modal-title">
              {{ editing ? "Edit Currency" : "Add Currency" }}
            </h3>
            <div class="currency-modal-fields">
              <label>
                <span>Code</span>
                <input
                  v-model.trim="form.code"
                  maxlength="3"
                  :disabled="editing"
                  autocapitalize="characters"
                  autocomplete="off"
                  spellcheck="false"
                  required
                  @blur="fetchSuggestion"
                />
              </label>
              <label>
                <span>Name</span>
                <input
                  v-model.trim="form.name"
                  maxlength="64"
                  autocomplete="off"
                  required
                />
              </label>
              <label>
                <span>Value vs EUR</span>
                <input
                  v-model.number="form.valueVsEur"
                  type="number"
                  step="0.000001"
                  autocomplete="off"
                  required
                  min="0"
                />
                <span v-if="currencies.suggestValue && !form.valueVsEur" class="muted" style="font-size:0.95em;">
                  Suggestion: {{ currencies.suggestValue }} ({{ currencies.suggestSource }})
                  <button type="button" class="btn btn--sm" @click="useSuggestion">Use</button>
                </span>
              </label>
            </div>
            <div class="currency-modal-actions">
              <button class="btn btn--primary" type="submit" :disabled="loading">
                {{ editing ? "Save" : "Add" }}
              </button>
              <button class="btn btn--secondary" type="button" @click="closeModal" :disabled="loading">
                Cancel
              </button>
            </div>
          </form>
        </div>    
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from "vue";
  import { useCurrenciesStore } from "@/stores/storeCurrencies";
  const currencies = useCurrenciesStore();
  
  const modalOpen = ref(false);
  const editing = ref(false);
  const loading = ref(false);
  const form = ref({ code: "", name: "", valueVsEur: null });
  let editId = null;
  
  function openCreate() {
    modalOpen.value = true;
    editing.value = false;
    form.value = { code: "", name: "", valueVsEur: null };
    editId = null;
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
    loading.value = false;
    form.value = { code: "", name: "", valueVsEur: null };
    editId = null;
    currencies.suggestValue = null;
  }
  async function onSave() {
    if (!form.value.code || !form.value.name || !form.value.valueVsEur) return;
    loading.value = true;
    try {
      if (editing.value) {
        await currencies.updateOne(editId, {
          name: form.value.name,
          valueVsEur: form.value.valueVsEur,
        });
      } else {
        // Backend expects code, name, valueVsEur
        await currencies.createOne({ ...form.value });
      }
      closeModal();
    } catch (e) {
      // Error handled in store
    } finally {
      loading.value = false;
    }
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
  function formatDate(date) {
    if (!date) return "-";
    try {
      return new Date(date).toLocaleString();
    } catch {
      return date;
    }
  }
  
  onMounted(() => currencies.fetchAll());
  </script>
  
  <style scoped>
  .currencies-page {
    max-width: 860px;
    margin: 0 auto;
  }
  
  .currencies-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
  }
  
  .currencies-header .btn {
    font-size: 1.08em;
    padding: 12px 24px;
    border-radius: 14px;
    box-shadow: 0 4px 18px rgba(54,255,168,0.09);
  }
  
  .currencies-table-wrap {
    background: rgba(255,255,255,0.01);
    border-radius: 16px;
    box-shadow: 0 2px 16px rgba(16,32,24,0.07);
    padding: 24px 16px;
  }
  
  .currencies-table {
    width: 100%;
    border-collapse: separate;
    border-spacing: 0 12px;
    font-size: 1.13em;
  }
  
  .currencies-table th, .currencies-table td {
    padding: 14px 18px;
    background: rgba(255,255,255,0.07);
    border-radius: 8px;
    text-align: left;
  }
  
  .currencies-table th {
    background: none;
    color: #16ff97;
    font-size: 1.02em;
    font-weight: 800;
    letter-spacing: 0.03em;
    border-bottom: 2px solid #16ff9766;
  }
  
  .actions-col {
    width: 190px;
    text-align: center;
  }
  
  .action-buttons {
    display: flex;
    gap: 8px;
    justify-content: center;
  }
  
  .action-btn {
    padding: 6px 15px;
    font-size: 1em;
    border-radius: 7px;
    border: none;
    cursor: pointer;
    transition: background 0.15s, color 0.15s, box-shadow 0.15s;
    font-weight: 600;
    outline: none;
    background: #212d2a;
    color: #fff;
    box-shadow: 0 1.5px 8px rgba(54, 255, 168, 0.07);
  }
  .action-btn.edit {
    background: linear-gradient(90deg, #38ef7d 0%, #16ff97 100%);
    color: #133b25;
  }
  .action-btn.edit:hover {
    background: linear-gradient(90deg, #16ff97 0%, #38ef7d 100%);
  }
  .action-btn.delete {
    background: #2b1818;
    color: #ff4141;
  }
  .action-btn.delete:hover {
    background: #ff4141;
    color: #fff;
  }
  .action-btn.refresh {
    background: #202c36;
    color: #16ff97;
  }
  .action-btn.refresh:hover {
    background: #133b25;
    color: #fff;
  }
  
  .currency-modal-backdrop {
    position: fixed;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(18,30,24,0.35);
    z-index: 9999;
    transition: background 0.2s;
  }
  .currency-modal-card {
    min-width: 340px;
    max-width: 96vw;
    background: #f9fffd;
    border-radius: 26px;
    box-shadow: 0 6px 36px rgba(20, 24, 22, 0.26), 0 1.5px 12px rgba(54, 255, 168, 0.13);
    padding: 36px 34px 24px 34px;
    display: flex;
    flex-direction: column;
    align-items: stretch;
    animation: popin 0.19s cubic-bezier(.71,-0.08,.84,.36);
  }
  @keyframes popin {
    0% { transform: scale(0.89); opacity: 0; }
    100% { transform: scale(1); opacity: 1; }
  }
  .currency-modal-title {
    margin-bottom: 16px;
    color: #16ff97;
    font-size: 1.55em;
    font-weight: 800;
    letter-spacing: 0.02em;
    text-align: center;
  }
  .currency-modal-fields {
    margin-bottom: 18px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  .currency-modal-card label {
    display: flex;
    flex-direction: column;
    font-weight: 600;
    color: #133b25;
    font-size: 1.07em;
    margin-bottom: 2px;
  }
  .currency-modal-card input[type="text"],
  .currency-modal-card input[type="number"] {
    width: 100%;
    padding: 12px 15px;
    margin-top: 6px;
    border-radius: 10px;
    border: 1.2px solid #b9e9d6;
    background: #f7fdfa;
    font-size: 1.13em;
    transition: border 0.14s;
    margin-bottom: 0px;
  }
  .currency-modal-card input:focus {
    border-color: #16ff97;
    outline: none;
  }
  .currency-modal-actions {
    display: flex;
    gap: 22px;
    margin-top: 18px;
    justify-content: center;
  }
  .currency-modal-actions .btn {
    min-width: 100px;
    padding: 12px 18px;
    border-radius: 10px;
    font-weight: 700;
    font-size: 1.07em;
    transition: background 0.14s, box-shadow 0.17s;
    cursor: pointer;
  }
  .currency-modal-actions .btn--primary {
    background: linear-gradient(90deg, #16ff97 0%, #38ef7d 100%);
    color: #133b25;
    border: none;
    box-shadow: 0 2px 8px rgba(54, 255, 168, 0.14);
  }
  .currency-modal-actions .btn--primary:hover {
    background: linear-gradient(90deg, #38ef7d 0%, #16ff97 100%);
    box-shadow: 0 3px 18px rgba(54, 255, 168, 0.23);
  }
  .currency-modal-actions .btn--secondary {
    background: none;
    color: #789;
    border: none;
  }
  .currency-modal-actions .btn--secondary:hover {
    text-decoration: underline;
  }
  @media (max-width: 600px) {
    .currency-modal-card {
      min-width: 94vw;
      padding: 18px 6vw 14px 6vw;
    }
    .currency-modal-title {
      font-size: 1.17em;
    }
    .currency-modal-actions .btn {
      padding: 8px 7vw;
    }
  }
  .muted {
    color: #90a899;
  }
  .mt-2 {
    margin-top: 1.2em;
  }
  </style>
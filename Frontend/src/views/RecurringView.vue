<template>
    <div class="container page">
      <div class="card">
        <div class="table-header-wrapper">
          <h2>Ponavljajuće transakcije</h2>
          <div style="display: flex; gap: 8px; align-items: center; margin-bottom: 8px;">
            <button class="btn btn--primary" @click="fetchTemplates" :disabled="loading">Osveži</button>
            <button class="btn btn--primary" @click="openCreateForm">Dodaj šablon</button>
          </div>
        </div>
        <table class="wf__table" style="margin-top:12px;">
          <thead>
            <tr>
              <th>Naziv</th>
              <th>Novčanik</th>
              <th>Kategorija</th>
              <th>Iznos</th>
              <th>Valuta</th>
              <th>Frekvencija</th>
              <th>Interval</th>
              <th>Start</th>
              <th>Kraj</th>
              <th>Sledeći termin</th>
              <th>Status</th>
              <th>Akcije</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tpl in templates" :key="tpl.templateId">
              <td>{{ tpl.name }}</td>
              <td>{{ walletName(tpl.walletId) }}</td>
              <td>{{ categoryName(tpl.categoryId) }}</td>
              <td>{{ fmt(tpl.amount) }}</td>
              <td>{{ tpl.walletCurrencyCode }}</td>
              <td>{{ tpl.frequency }}</td>
              <td>{{ tpl.interval }}</td>
              <td>{{ tpl.startDate }}</td>
              <td>{{ tpl.endDate || "—" }}</td>
              <td>{{ tpl.nextRunDate || "—" }}</td>
              <td>
                <span :style="{ color: tpl.active ? 'green' : 'gray' }">
                  {{ tpl.active ? "Aktivno" : "Isključeno" }}
                </span>
              </td>
              <td class="recurring-btns-wrapper">
                <button @click="openEditForm(tpl)" class="btn btn--sm">Izmeni</button>
                <button @click="toggleTemplate(tpl)" class="btn btn--sm">
                  {{ tpl.active ? "Isključi" : "Aktiviraj" }}
                </button>
              </td>
            </tr>
            <tr v-if="!loading && !templates.length">
              <td colspan="12" style="text-align:center; color:var(--muted);">Nema podataka.</td>
            </tr>
            <tr v-if="loading">
              <td colspan="12" style="text-align:center;">Učitavanje…</td>
            </tr>
          </tbody>
        </table>
    
        <!-- Modal za kreiranje/izmenu šablona -->
        <RecurringTemplateForm
          :open="modalOpen"
          :template="selectedTemplate"
          @saved="fetchTemplates"
          @close="closeForm"
        />
      </div>
    </div>
  </template>
  
<script setup>
import { ref, onMounted } from "vue";
import { useAuthStore } from "@/stores/auth";
import { listRecurringTemplates, toggleRecurringTemplate } from "@/services/recurringService";
import { useWalletsStore } from "@/stores/storeWallets";
import { useCategoriesStore } from "@/stores/storeCategories";
import { useToast } from "@/composables/useToast";
import RecurringTemplateForm from "@/components/RecurringTemplateForm.vue";

const toast = useToast();
const wallets = useWalletsStore();
const categories = useCategoriesStore();
const auth = useAuthStore();

const templates = ref([]);
const loading = ref(false);
const modalOpen = ref(false);
const selectedTemplate = ref(null);

function openCreateForm() {
  selectedTemplate.value = null;
  modalOpen.value = true;
}
function openEditForm(tpl) {
  selectedTemplate.value = tpl;
  modalOpen.value = true;
}
function closeForm() {
  modalOpen.value = false;
  selectedTemplate.value = null;
}

function fmt(n) {
  const v = Number(n || 0);
  return v.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function walletName(id) {
  const w = wallets.items.find(x => x.id === id);
  return w ? w.name : "#" + id;
}
function categoryName(id) {
  const c = categories.items.find(x => x.id === id);
  return c ? c.name : "#" + id;
}

async function fetchTemplates() {
  if (!auth.user?.id) return;
  loading.value = true;
  try {
    const { data } = await listRecurringTemplates(auth.user.id);
    templates.value = data;
  } catch (e) {
    templates.value = [];
    toast.error("Greška pri učitavanju šablona!");
  } finally {
    loading.value = false;
  }
}

async function toggleTemplate(tpl) {
  loading.value = true;
  await toggleRecurringTemplate(tpl.templateId, !tpl.active);
  await fetchTemplates();
}

onMounted(fetchTemplates);
</script>
  
<style scoped>
.wf__table {
  width: 100%;
  border-collapse: collapse;
  border-spacing: 0;
}
.wf__table thead th {
  font-weight: 600;
  color: var(--muted);
  padding: 10px 12px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.wf__table tbody td {
  padding: 10px 12px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.btn { 
  padding:.4rem .7rem;
  border-radius:.4rem;
  cursor:pointer;
  transition: 0.3s;
}
.btn--primary { 
  background:#16a34a; 
  color:white; 
  border:1px solid #16a34a;
  transition: 0.3s;
}

.btn--sm { 
  font-size: 0.93em;
}

.recurring-btns-wrapper {
  display: flex;
  gap: 0.5rem;
  padding: 10px 6px;
  align-items: center;
  justify-content: center;
}

.table-header-wrapper{
  display: flex;
  justify-content: space-between;
}
</style>
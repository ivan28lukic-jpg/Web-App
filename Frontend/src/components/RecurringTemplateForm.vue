<template>
    <div class="modal-backdrop" v-if="open">
      <div class="modal">
        <h3>{{ isEdit ? "Izmeni šablon" : "Kreiraj novi šablon" }}</h3>
        <form @submit.prevent="handleSubmit">
          <!-- Prvi red -->
          <div class="form-row">
            <div class="form-group">
              <label>Naziv</label>
              <input v-model="form.name" required />
            </div>
            <div class="form-group">
              <label>Novčanik</label>
              <select v-model="form.walletId" required>
                <option v-for="w in wallets.items" :key="w.id" :value="w.id">{{ w.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>Kategorija</label>
              <select v-model="form.categoryId" required>
                <option v-for="c in categories.items" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
            </div>
          </div>
          <!-- Drugi red -->
          <div class="form-row">
            <div class="form-group">
              <label>Iznos</label>
              <input v-model.number="form.amount" type="number" min="0.1" step="1" required />
            </div>
            <div class="form-group">
              <label>Valuta</label>
              <span>{{ selectedWallet?.currencyCode || "?" }}</span>
            </div>
            <div class="form-group">
              <label>Frekvencija</label>
              <select v-model="form.frequency" required>
                <option value="DAILY">Dnevno</option>
                <option value="WEEKLY">Nedeljno</option>
                <option value="MONTHLY">Mesečno</option>
                <option value="YEARLY">Godišnje</option>
              </select>
            </div>
            <div class="form-group">
              <label>Interval</label>
              <input v-model.number="form.interval" type="number" min="1" required />
            </div>
          </div>
          <!-- Treći red -->
          <div class="form-row">
            <div class="form-group">
              <label>Datum početka</label>
              <input v-model="form.startDate" type="date" required />
            </div>
            <div class="form-group">
              <label>Datum kraja</label>
              <input v-model="form.endDate" type="date" />
            </div>
            <div class="form-group" style="align-items:center; flex-direction:row; gap:8px;">
              <label>Aktivno</label>
              <input type="checkbox" v-model="form.active" />
            </div>
          </div>
          <!-- Opis i dugmad -->
          <div class="form-group">
            <label>Opis</label>
            <input v-model="form.descriptionTemplate" />
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn--primary" :disabled="submitting">
              {{ isEdit ? "Sačuvaj izmene" : "Kreiraj" }}
            </button>
            <button type="button" class="btn" @click="$emit('close')">Odustani</button>
          </div>
          <div v-if="errorMessage" class="form-error">{{ errorMessage }}</div>
        </form>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, watch, onMounted } from "vue";
  import { createRecurringTemplate, updateRecurringTemplate } from "@/services/recurringService";
  import { useWalletsStore } from "@/stores/storeWallets";
  import { useCategoriesStore } from "@/stores/storeCategories";
  import { useAuthStore } from "@/stores/auth";
  import { useToast } from "@/composables/useToast";
  
  
  // Props
  const props = defineProps({
    open: Boolean,
    template: Object // ako je edit, šalješ šablon koji menjaš
  });
  const emit = defineEmits(["saved", "close"]);
  
  const wallets = useWalletsStore();
  const categories = useCategoriesStore();
  const auth = useAuthStore();
  const toast = useToast();
  
  const isEdit = computed(() => !!props.template && !!props.template.templateId);
  
  // Lokalni state forme
  const form = ref({
    name: "",
    walletId: "",
    categoryId: "",
    amount: 0.01,
    descriptionTemplate: "",
    frequency: "MONTHLY",
    interval: 1,
    startDate: "",
    endDate: "",
    active: true
  });
  const errorMessage = ref("");
  const submitting = ref(false);
  
  const selectedWallet = computed(() =>
    wallets.items.find(w => String(w.id) === String(form.value.walletId))
  );
  
  // Kada se promeni "template", popuni formu
  watch(
    () => props.template,
    (tpl) => {
      if (tpl) {
        form.value = {
          name: tpl.name || "",
          walletId: tpl.walletId || "",
          categoryId: tpl.categoryId || "",
          amount: tpl.amount || 0.01,
          descriptionTemplate: tpl.descriptionTemplate || "",
          frequency: tpl.frequency || "MONTHLY",
          interval: tpl.interval || 1,
          startDate: tpl.startDate || "",
          endDate: tpl.endDate || "",
          active: tpl.active !== undefined ? tpl.active : true
        };
      } else {
        form.value = {
          name: "",
          walletId: "",
          categoryId: "",
          amount: 0.01,
          descriptionTemplate: "",
          frequency: "MONTHLY",
          interval: 1,
          startDate: "",
          endDate: "",
          active: true
        };
      }
    },
    { immediate: true }
  );
  
  async function handleSubmit() {
    errorMessage.value = "";
    submitting.value = true;
    try {
      // Pripremi payload
      const payload = {
        ownerId: auth.user.id,
        ...form.value
      };
      if (isEdit.value) {
        await updateRecurringTemplate(props.template.templateId, payload);
        toast.success("Šablon je izmenjen!");
      } else {
        await createRecurringTemplate(payload);
        toast.success("Šablon je kreiran!");
      }
      emit("saved");
      emit("close");
    } catch (e) {
      errorMessage.value = e?.response?.data?.message || "Greška pri čuvanju šablona!";
      toast.error(errorMessage.value);
    } finally {
      submitting.value = false;
    }
  }
    onMounted(() => {
    if (wallets.items.length === 0 && typeof wallets.fetch === "function") {
        wallets.fetch();
    }
    if (categories.items.length === 0 && typeof categories.fetch === "function") {
        categories.fetch();
    }
    });
  </script>
  
  <style scoped>
  .modal-backdrop {
    position: fixed;
    z-index: 999;
    left: 0; top: 0; right: 0; bottom: 0;
    background: rgba(0,0,0,.55); /* tamnija pozadina */
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .modal {
    background: #181f1b; /* tamna pozadina */
    color: #cce8d4;      /* svetlo-zelena za tekst */
    padding: 2.5rem 2rem;
    border-radius: 16px;
    min-width: 350px;
    max-width: 420px;
    box-shadow: 0 8px 32px rgba(0,0,0,.35);
    border: 1px solid #22c55e22;
  }
  
  h3 {
    color: #22c55e;
    margin-bottom: 1.5rem;
    font-size: 1.35em;
    font-weight: bold;
    text-align: center;
  }
  
  .form-row {
    display: flex;
    gap: 1.5rem;
    margin-bottom: 1rem;
  }
  .form-row .form-group {
    flex: 1 1 0;
    min-width: 0;
  }
  @media (max-width: 600px) {
    .form-row {
      flex-direction: column;
      gap: 0.5rem;
    }
  }
  
  .form-group { 
    margin-bottom: 1.1rem; 
    display: flex; 
    flex-direction: column;
  }
  
  label {
    font-size: 1em;
    color: #22c55e;
    margin-bottom: 3px;
    font-weight: 500;
  }
  
  input, select {
    background: #232c25;
    color: #e7fbe5;
    border: 1px solid #22c55e44;
    padding: 0.55em 0.6em;
    border-radius: 7px;
    font-size: 1em;
    outline: none;
    transition: border 0.18s;
  }
  input:focus, select:focus {
    border: 1.5px solid #22c55e;
    background: #29322b;
  }
  
  input[type="checkbox"] {
    width: 20px;
    height: 20px;
    accent-color: #22c55e;
  }
  
  .form-actions { 
    display: flex; 
    gap: 1rem; 
    margin-top: 1.2rem; 
    justify-content: center;
  }
  .form-error { color: #ef4444; margin-top: .6rem; text-align:center; }
  
  .btn {
    padding: .6rem 1.2rem;
    border-radius: .5rem;
    cursor: pointer;
    border: none;
    background: #22c55e;
    color: white;
    font-weight: 500;
    box-shadow: 0 2px 8px #22c55e22;
    transition: background .15s;
  }
  .btn:hover {
    background: #16a34a;
  }
  
  .btn--primary {
    background: #22c55e;
  }
  </style>
<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useSavingGoalsStore } from "@/stores/savingGoals";
import { useWalletsStore } from "@/stores/wallets";

const auth = useAuthStore();
const goals = useSavingGoalsStore();
const wallets = useWalletsStore();

const userId = computed(() => auth.user?.id);
const userRole = computed(() => auth.user?.role || auth.role || "USER");

setTimeout(() => {
  console.log("userRole:", userRole.value);
  console.log("userId:", userId.value);
  console.log("savingWallets:", savingWallets.value);
}, 2000);

const showAdd = ref(false);
const showContribute = ref(false);
const showProgress = ref(false);
const selectedGoal = ref(null);

const form = ref({
  name: "",
  walletId: "",
  targetAmount: "",
  dueDate: ""
});

const contributeForm = ref({
  fromWalletId: "",
  amount: "",
  description: ""
});

// Štedni novčanici po pravima
const savingWallets = computed(() =>
  userRole.value === "ADMIN"
    ? wallets.items.filter(w => w.savings && !w.archived)
    : wallets.items.filter(w => w.savings && !w.archived && w.ownerId === userId.value)
);

function walletName(walletId) {
  const w = wallets.items.find(w => w.id === walletId);
  return w ? w.name : "—";
}

function fmt(n) {
  if (n === null || n === undefined || n === "" || isNaN(Number(n))) return "—";
  return Number(n).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
function parseNumber(n) {
  const parsed = Number(n);
  return isNaN(parsed) ? 0 : parsed;
}
function formatDate(d) {
  if (!d) return "—";
  return typeof d === "string" ? new Date(d).toLocaleDateString() : d.toLocaleDateString();
}
function progressPercent(current, target) {
  const curr = parseNumber(current);
  const targ = parseNumber(target);
  if (!curr || !targ) return "—";
  return Math.round((curr / targ) * 100) + "%";
}

function resetForm() {
  form.value = { name: "", walletId: "", targetAmount: "", dueDate: "" };
}
function resetContributeForm() {
  contributeForm.value = { fromWalletId: "", amount: "", description: "" };
}

// Dodavanje cilja
async function submitGoal() {
  await goals.createGoal({
    ownerId: userId.value,
    walletId: form.value.walletId,
    name: form.value.name,
    targetAmount: form.value.targetAmount,
    dueDate: form.value.dueDate || null
  });
  showAdd.value = false;
  resetForm();
}

function openContribute(goal) {
  selectedGoal.value = goal;
  showContribute.value = true;
}
async function submitContribute() {
  if (!selectedGoal.value) return;
  await goals.contributeToGoal(selectedGoal.value.id, {
    fromWalletId: contributeForm.value.fromWalletId,
    amount: contributeForm.value.amount,
    description: contributeForm.value.description,
    occurredAt: new Date().toISOString(),
    outCategoryId: null,
    inCategoryId: null
  });
  showContribute.value = false;
  resetContributeForm();
}
function openProgress(goal) {
  selectedGoal.value = goal;
  goals.fetchProgress(goal.id);
  showProgress.value = true;
}

// Brisanje cilja: admin može sve, user može svoj
async function handleDeleteGoal(goalId) {
  await goals.deleteGoal(goalId);
}

// Učitavanje podataka po pravima
onMounted(async () => {
  await auth.fetchMe();

  // Kada userId postane dostupan, uradi fetch!
  watch(
    () => userId.value,
    (newVal) => {
      if (!newVal) return;

      if (userRole.value === "ADMIN") {
        wallets.fetchAll();
        goals.fetchAll();
      } else {
        wallets.fetch({ ownerId: userId.value });
        goals.fetchAll(userId.value);
      }
    },
    { immediate: true }
  );
});
</script>

<template>
  <div class="container page">
    <div v-if="goals.error" class="error">{{ goals.error }}</div>
    <div class="card" v-if="!goals.loading">
      <h2>Ciljevi štednje</h2>
      <button class="btn btn--primary" @click="showAdd = true">+ Dodaj cilj</button>
      <table class="wf__table" v-if="goals.items.length">
        <thead>
          <tr>
            <th>Naziv</th>
            <th>Štedni novčanik</th>
            <th>Željeni iznos</th>
            <th>Stanje</th>
            <th>Rok</th>
            <th>Napredak</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="g in goals.items" :key="g.id">
            <td>{{ g.name }}</td>
            <td>{{ walletName(g.walletId) }}</td>
            <td>{{ fmt(g.targetAmount) }}</td>
            <td>{{ fmt(g.currentAmount) }}</td>
            <td>{{ formatDate(g.dueDate) }}</td>
            <td>
              <progress :value="parseNumber(g.currentAmount)" :max="parseNumber(g.targetAmount) || 1"></progress>
              {{ progressPercent(g.currentAmount, g.targetAmount) }}
            </td>
            <td>
              <button @click="openContribute(g)">Uplati</button>
              <button @click="openProgress(g)">Grafikon</button>
              <!-- Samo admin ili vlasnik može da briše -->
              <button
                v-if="userRole.value === 'ADMIN' || g.ownerId === userId.value"
                @click="handleDeleteGoal(g.id)"
                style="background: #d32f2f; color: #fff; margin-left: 6px;"
              >
                Obriši
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="no-data">Nema ciljeva za prikaz.</div>
    </div>
    <div v-else>Učitavanje...</div>

    <!-- Modal za unos cilja -->
    <div v-if="showAdd" class="modal">
      <div class="modal-content">
        <h3>Novi cilj štednje</h3>
        <form @submit.prevent="submitGoal">
          <input v-model="form.name" placeholder="Naziv cilja" required />
          <input v-model.number="form.targetAmount" type="number" min="1" placeholder="Željeni iznos" required />
          <select v-model="form.walletId" required>
            <option disabled value="">Odaberi štedni novčanik</option>
            <option v-for="w in savingWallets" :key="w.id" :value="w.id">
              {{ w.name }} ({{ w.currencyCode }})
            </option>
          </select>
          <input v-model="form.dueDate" type="date" placeholder="Rok (opciono)" />
          <button type="submit">Sačuvaj</button>
          <button type="button" @click="showAdd = false">Otkaži</button>
        </form>
      </div>
    </div>

   <!-- Modal za uplatu -->
<div v-if="showContribute" class="modal">
  <div class="modal-content">
    <h3>Uplata na cilj: {{ selectedGoal.name }}</h3>
    <form @submit.prevent="submitContribute">
      <select v-model="contributeForm.fromWalletId" required>
        <option disabled value="">Odaberi novčanik</option>
        <option
          v-for="w in wallets.items.filter(w => !w.savings && !w.archived)"
          :key="w.id"
          :value="w.id"
        >
          {{ w.name }} ({{ w.currencyCode }})
        </option>
      </select>
      <input v-model.number="contributeForm.amount" type="number" min="1" placeholder="Iznos" required />
      <input v-model="contributeForm.description" placeholder="Opis" />
      <button type="submit">Uplati</button>
      <button type="button" @click="showContribute = false">Otkaži</button>
    </form>
  </div>
</div>

    <!-- Modal za grafikon -->
    <div v-if="showProgress && goals.progress[selectedGoal.id]" class="modal">
      <div class="modal-content">
        <h3>Napredak cilja "{{ selectedGoal.name }}"</h3>
        <div>
          <progress :value="parseNumber(goals.progress[selectedGoal.id].currentAmount)" :max="parseNumber(goals.progress[selectedGoal.id].targetAmount) || 1"></progress>
          <div>Ukupno: {{ fmt(goals.progress[selectedGoal.id].currentAmount) }} / {{ fmt(goals.progress[selectedGoal.id].targetAmount) }}</div>
          <div>Procenat: {{ fmt(goals.progress[selectedGoal.id].percent) }}%</div>
        </div>
        <button @click="showProgress = false">Zatvori</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.card {
  background: #23272f;
  padding: 30px 24px;
  border-radius: 14px;
  box-shadow: 0 2px 16px #0002;
  margin-bottom: 32px;
}
.wf__table {
  width: 100%;
  margin-bottom: 24px;
  border-collapse: collapse;
  background: #252930;
  color: #e7ebf0;
}
.wf__table th, .wf__table td {
  padding: 8px 12px;
  border-bottom: 1px solid #333;
}
.btn.btn--primary {
  background: #208a4d;
  color: #fff;
  border: none;
  margin: 8px 0 16px 0;
  padding: 8px 22px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
}
.no-data {
  color: #a0a0a0;
  font-size: 1.13rem;
  margin-bottom: 10px;
  text-align: center;
}
.modal {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(255, 255, 255, 0.75);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}
.modal-content {
  background: white;
  border-radius: 16px;
  padding: 32px;
  width: 95%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  color: #222;
}
.modal-content select {
  background: #f2f7fa;
  color: #222;
  border: 1px solid #aaa;
}
.modal-content select:focus {
  background: #e0f8ec;
  border-color: #208a4d;
}
.modal-content option {
  background: #f2f7fa;
  color: #222;
}

.modal-content h3 { margin-bottom: 16px; font-weight: 700;}
.modal-content form { display: flex; flex-direction: column; gap: 14px;}
.modal-content input, .modal-content select { padding: 8px 10px; border-radius: 8px; border: 1px solid #ccc;}
.modal-content button { margin-top: 8px; }
.error { color: #ff4d4f; margin: 18px 0; font-weight: 600; }
</style>
<script setup>
import { ref, watch, onMounted } from "vue";
import { useSavingGoalsStore } from "@/stores/storesSavingGoals";
import BaseModal from "@/components/BaseModal.vue";
import SavingGoalForm from "@/components/SavingGoalForm.vue";
import SavingGoalTable from "@/components/SavingGoalTable.vue";
import GoalTransferForm from "@/components/GoalTransferForm.vue";
import SavingGoalProgressChart from "@/components/SavingGoalProgressChart.vue";
import "@/assets/categories-look.css"; // ⬅ isti izgled kao Categories
import { useWalletsStore } from "@/stores/storeWallets";

const store = useSavingGoalsStore();
const q = ref("");
watch(q, v => (store.search = v));

const showEdit = ref(false);
const showTransfer = ref(false);
const showProgress = ref(false);
const editGoal = ref(null);
const transferMode = ref("contribute");
const walletsStore = useWalletsStore();

function openCreate() {
  editGoal.value = null;
  showEdit.value = true;
}
function openEdit(g) {
  editGoal.value = { ...g };
  showEdit.value = true;
}
function closeEdit() {
  showEdit.value = false;
  editGoal.value = null;
}
async function submitEdit(payload) {
  if (editGoal.value?.id) await store.edit(editGoal.value.id, payload);
  else await store.add(payload);
  closeEdit();
}

async function openContrib(goal) {
  transferMode.value = "contribute";
  editGoal.value = { ...goal };
  await walletsStore.fetchForOwner(goal.ownerId); // ← OVO JE KLJUČNO
  showTransfer.value = true;
}
async function openWithdraw(goal) {
  transferMode.value = "withdraw";
  editGoal.value = { ...goal };
  await walletsStore.fetchForOwner(goal.ownerId); // ← OVO JE KLJUČNO
  showTransfer.value = true;
}
function closeTransfer() {
  showTransfer.value = false;
  editGoal.value = null;
}
async function submitTransfer(payload) {
  try {
    if (!editGoal.value?.id) return;
    if (transferMode.value === "contribute")
      await store.contribute(editGoal.value.id, payload);
    else await store.withdraw(editGoal.value.id, payload);
    closeTransfer();
  } catch (e) {
    alert(e.message);
  }
}

async function openProgress(g) {
  editGoal.value = { ...g };
  await store.loadProgress(g.id, {});
  showProgress.value = true;
}
function closeProgress() {
  showProgress.value = false;
  editGoal.value = null;
}
async function onDelete(g) {
  if (confirm(`Delete goal "${g.name}"?`)) await store.remove(g.id);
}

onMounted(() => store.load());
</script>

<template>
  <section class="data-page">
    <div class="data-card">
      <div class="data-card__head">
        <h1 class="data-card__title">Saving Goals</h1>
        <div class="data-toolbar">
          <input v-model="q" class="data-input" placeholder="Search..." />
          <label class="data-switch">
            <input type="checkbox" v-model="store.includeArchived" @change="store.load()" />
            Show archived
          </label>
          <button class="data-btn data-btn--primary" @click="openCreate">+ New</button>
        </div>
      </div>

      <SavingGoalTable
        :items="store.filtered"
        :loading="store.loading"
        @edit="openEdit"
        @delete="onDelete"
        @progress="openProgress"
        @contribute="openContrib"
        @withdraw="openWithdraw"
      />

      <p v-if="store.error" style="color:#ff7d75; padding: .9rem 1rem;">⚠ {{ store.error }}</p>
    </div>

    <!-- CREATE / EDIT -->
    <BaseModal v-model="showEdit" :title="editGoal ? 'Change goal' : 'New goal'">
      <SavingGoalForm :initial="editGoal" :owner-id="store.ownerId" @submit="submitEdit" @cancel="closeEdit" />
    </BaseModal>

    <!-- TRANSFERS -->
    <BaseModal v-model="showTransfer" :title="transferMode==='contribute' ? 'Deposit to goal' : 'Withdraw from goal'">
      <GoalTransferForm :mode="transferMode" :owner-id="store.ownerId" :goal="editGoal" @submit="submitTransfer" @cancel="closeTransfer" />
    </BaseModal>

    <!-- PROGRESS -->
    <BaseModal v-model="showProgress" :title="editGoal ? `Progress: ${editGoal.name}` : 'Progress'" width="760px">
      <div v-if="store.progressLoading" style="opacity:.8;padding:.6rem 0;">Loading…</div>
      <div v-else-if="store.progressError" style="color:#ff7d75;padding:.6rem 0;">⚠ {{ store.progressError }}</div>
      <SavingGoalProgressChart v-else-if="store.progress" :progress="store.progress" />
      <template #footer><button class="data-btn" @click="closeProgress">Close</button></template>
    </BaseModal>
  </section>
</template>

<style>
.progress-bar {
  height: 8px;
  width: 100%;
  background-color: #111; /* pozadina trake */
  border-radius: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #20c589; /* tvoja zelena */
  border-radius: 10px;
  width: 0;
  transition: width 1s ease-in-out;
}
</style>


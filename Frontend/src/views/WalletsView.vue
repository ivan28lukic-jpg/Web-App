<template>
  <div class="container page">
    <div class="card" style="overflow:auto;">
      <header style="display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:12px;">
        <div>
          <h2 style="margin:0 0 4px 0;">Wallets</h2>
          <p class="muted" style="margin:0;">Create and manage your wallets.</p>
        </div>
        <div style="display:flex; gap:8px; align-items:center;">
          <input
            v-model="search"
            class="form__control"
            placeholder="Search by name…"
            style="width:220px;"
            @keydown.enter="reload()"
          />
          <button class="btn btn--outline" @click="reload()">Search</button>
          <button class="btn btn--primary" @click="openCreate()">+ New wallet</button>
        </div>
      </header>

      <table class="wf__table">
        <thead>
          <tr>
            <th style="text-align:left;">Name</th>
            <th>Currency</th>
            <th style="text-align:right;">Balance</th>
            <th>Archived</th>
            <th style="width:1%;">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="w in wallets.items" :key="w.id">
            <td style="text-align:left;">{{ w.name }}</td>
            <td style="text-align:center;">{{ currencyOf(w) }}</td>
            <td style="text-align:right;">{{ fmt(w.balance) }}</td>
            <td style="text-align:center;">
              <span :style="{opacity: w.archived ? 1 : 0.35}">{{ w.archived ? "Yes" : "No" }}</span>
            </td>
            <td>
              <div class="wf__actions">
                <button class="btn btn--secondary btn--sm" @click="openEdit(w)">Edit</button>
                <button class="btn btn--outline btn--sm" @click="openArchive(w)">
                  {{ w.archived ? "Restore" : "Archive" }}
                </button>
                <button class="btn btn--danger btn--sm" @click="askDelete(w)">Delete</button>
              </div>
            </td>
          </tr>

          <tr v-if="!wallets.loading && wallets.items.length === 0">
            <td colspan="5" style="text-align:center; padding:18px;">
              <span class="muted">No wallets yet.</span>
            </td>
          </tr>

          <tr v-if="wallets.loading">
            <td colspan="5" style="text-align:center; padding:18px;">
              Loading…
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Modal & Confirm -->
  <WalletModal
    v-model="showModal"
    :wallet="current"
    :currencies="wallets.currencies"
    @save="onSave"
    @closed="current = null"
  />
  <ArchiveConfirm
    v-model="archiveOpen"
    :wallet="archiveTarget"
    :archived="!!archiveTarget?.archived"
    @confirm="doArchive"
  />
  <ConfirmDialog
    v-model="confirmOpen"
    title="Delete wallet?"
    :message="confirmMessage"
    confirm-text="Delete"
    cancel-text="Cancel"
    @confirm="doDelete"
  />
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useWalletsStore } from "@/stores/storeWallets";
import { useToast } from "@/composables/useToast";
import WalletModal from "@/components/WalletModal.vue";
import ConfirmDialog from "@/components/ConfirmDialog.vue";
import ArchiveConfirm from "@/components/ArchiveConfirm.vue";

const toast = useToast();
const wallets = useWalletsStore();

const search = ref("");
const showModal = ref(false);
const current = ref(null);

const confirmOpen = ref(false);
const confirmMessage = ref("");
let toDeleteId = null;

const archiveOpen = ref(false);
const archiveTarget = ref(null);

/** format number with 2 decimals */
function fmt(n) {
  const v = Number(n || 0);
  return v.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

/** normalize currency field from API */
function currencyOf(w) {
  return w.currencyCode || "-";
}

async function reload() {
  await wallets.fetch({ search: search.value, page: 0, size: 100 });
}

function openCreate() {
  current.value = null;
  showModal.value = true;
}

function openEdit(w) {
  current.value = { ...w };
  showModal.value = true;
}

function openArchive(w) {
  archiveTarget.value = { ...w };
  archiveOpen.value = true;
}

async function onSave(payload) {
  try {
    if (current.value && current.value.id) {
      await wallets.updateOne(current.value.id, payload);
    } else {
      await wallets.createOne(payload);
    }
    await reload();                 // ⬅ osveži tabelu
    showModal.value = false;
    current.value = null;
  } catch (e) {
    toast.error(e?.response?.data?.message || "Operation failed");
  }
}

async function doArchive() {
  if (!archiveTarget.value) return;
  try {
    await wallets.setArchived(archiveTarget.value.id, !archiveTarget.value.archived);
    await reload();
  } catch (e) {
    toast.error(e?.response?.data?.message || "Failed to change archive state");
  } finally {
    archiveTarget.value = null;
  }
}

function askDelete(w) {
  toDeleteId = w.id;
  confirmMessage.value = `This action is permanent. Delete wallet “${w.name}”?`;
  confirmOpen.value = true;
}

async function doDelete() {
  if (!toDeleteId) return;
  try {
    await wallets.removeOne(toDeleteId);
    await reload();                 // ⬅ osveži tabelu
  } catch (e) {
    toast.error(e?.response?.data?.message || "Failed to delete wallet");
  } finally {
    toDeleteId = null;
  }
}

onMounted(async () => {
  await wallets.ensureCurrencies();
  await reload();
});
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
.wf__actions {
  display: flex;
  gap: 6px;
  justify-content: flex-end;
}
.muted { color: var(--muted); }
</style>
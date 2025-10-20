<template>
  <div class="container page">
    <div class="card" style="overflow:auto;">
      <header style="display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:12px;">
        <div>
          <h2 style="margin:0 0 4px 0;">Categories</h2>
          <p class="muted" style="margin:0;">Create and manage your categories.</p>
        </div>
        <div style="display:flex; gap:8px; align-items:center;">
          <select v-model="filters.type" class="form__control" style="width:160px;">
            <option value="">All types</option>
            <option value="INCOME">Income</option>
            <option value="EXPENSE">Expense</option>
          </select>
          <input v-model="filters.search" class="form__control" placeholder="Search…" style="width:200px;" @input="() => {}" @keydown.enter="reload()" />
          <button class="btn btn--outline" @click="reload()">Search</button>
          <button class="btn btn--primary" @click="openCreate()">+ New</button>
        </div>
      </header>

      <table class="wf__table">
        <thead>
          <tr>
            <th style="text-align:left;">Name</th>
            <th style="text-align:left;">Type</th>
            <th style="width:1%;">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in rows" :key="c.id">
            <td style="text-align:left;">
                {{ c.name }}
                <span v-if="isGlobal(c)" class="badge badge--global">Predefined</span>
                <span v-else class="badge badge--me">My</span>
            </td>
            <td style="text-align:left;">
                <span :style="typeStyle(c.type)">{{ c.type }}</span>
            </td>
            <td>
              <div class="wf__actions">
                <!-- Ako je admin: uvek može Edit/Delete -->
                <template v-if="isAdmin">
                  <button class="btn btn--secondary btn--sm" @click="openEdit(c)">Edit</button>
                  <button class="btn btn--danger btn--sm" @click="askDelete(c)">Delete</button>
                </template>
                <!-- Ako NIJE admin -->
                <template v-else>
                  <!-- Ako je moja: mogu Edit/Delete -->
                  <template v-if="!isGlobal(c)">
                    <button class="btn btn--secondary btn--sm" @click="openEdit(c)">Edit</button>
                    <button class="btn btn--danger btn--sm" @click="askDelete(c)">Delete</button>
                  </template>
                  <!-- Ako je globalna: mogu samo Use -->
                  <template v-else>
                    <button
                      class="btn btn--outline btn--sm"
                      @click="copyFromGlobal(c)"
                      :disabled="alreadyCopied(c)"
                      :title="alreadyCopied(c) ? 'You already have this category' : 'Add to my categories'"
                    >
                      Use
                    </button>
                  </template>
                </template>
              </div>
            </td>
          </tr>

          <tr v-if="!cats.loading && cats.items.length === 0">
            <td colspan="3" style="text-align:center; padding:18px;">
              <span class="muted">No categories.</span>
            </td>
          </tr>

          <tr v-if="cats.loading">
            <td colspan="3" style="text-align:center; padding:18px;">
              Loading…
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <CategoryModal
    v-model="modalOpen"
    :category="current"
    @save="onSave"
    @closed="current=null"
  />

  <ConfirmDialog
    v-model="confirmOpen"
    title="Delete category?"
    :message="confirmMessage"
    confirm-text="Delete"
    cancel-text="Cancel"
    @confirm="doDelete"
  />
</template>

<script setup>
import { ref, onMounted, watch, computed} from "vue";
import { useCategoriesStore } from "@/stores/storeCategories";
import CategoryModal from "@/components/CategoryModal.vue";
import ConfirmDialog from "@/components/ConfirmDialog.vue";
import { useToast } from "@/composables/useToast";
import { useAuthStore } from "@/stores/auth";

const toast = useToast();
const cats = useCategoriesStore();
const auth = useAuthStore();
const isAdmin = computed(() => auth.user?.role === "ADMIN");
const modalOpen = ref(false);
const current = ref(null);
const confirmOpen = ref(false);
const confirmMessage = ref("");
let toDeleteId = null;

const filters = cats.filters;
const userId = computed(() => auth?.user?.id ?? null);

const isGlobal = (c) =>
  c?.predefined === true || c?.isGlobal === true || c?.system === true || c?.ownerId == null;

const isMine = (c) => {
  if (userId.value == null) return false;
  const owners = [c?.ownerId, c?.userId, c?.createdBy];
  return owners.some(v => Number(v) === Number(userId.value));
};

const rows = computed(() => {
  const q = (filters.search || "").trim().toLowerCase();
  const type = (filters.type || "").toUpperCase();
  return (cats.items || []).filter((c) => {
    const byType = !type || String(c.type).toUpperCase() === type;
    const byName = !q || String(c.name || "").toLowerCase().includes(q);
    const visibleByOwner =
      userId.value == null ? true : isMine(c) || isGlobal(c);
    return byType && byName && visibleByOwner;
  });
});

let typingTimer;
function debounceReload(delay = 300) {
  clearTimeout(typingTimer);
  typingTimer = setTimeout(() => {
    cats.fetch({ page: 0 });
  }, delay);
}
watch(
  () => filters.search,
  () => debounceReload(300)
);
watch(
  () => filters.type,
  () => cats.fetch({ page: 0 })
);

function typeStyle(t) {
  const isIncome = String(t).toUpperCase() === "INCOME";
  return { color: isIncome ? "var(--green-300, #22c55e)" : "var(--red-300, #ef4444)" };
}

async function copyFromGlobal(c) {
  try {
    await cats.createOne({
      name: c.name,
      type: String(c.type||"").toUpperCase(),
      ownerId: userId.value
    });
    await reload();
    toast.success(`Added "${c.name}" to your categories`);
  } catch (e) {
    toast.error(e?.response?.data?.message || "Copy failed");
  }
}

function alreadyCopied(c) {
  const name = String(c.name||"").trim().toLowerCase();
  const type = String(c.type||"").toUpperCase();
  return (cats.items||[]).some(x =>
    isMine(x) &&
    String(x.name||"").trim().toLowerCase() === name &&
    String(x.type||"").toUpperCase() === type
  );
}

async function reload() {
  await cats.fetch({ page: 0 });
}

function openCreate() {
  current.value = null;
  modalOpen.value = true;
}
function openEdit(c) {
  current.value = { ...c };
  modalOpen.value = true;
}

async function onSave(payload) {
  try {
    let fullPayload;
    if (isAdmin.value) {
      // Admin pravi globalnu kategoriju (bez ownerId, sa flagom ako treba)
      fullPayload = { ...payload, ownerId: null, predefined: true };
    } else {
      // Korisnik pravi svoju kategoriju
      fullPayload = { ...payload, ownerId: userId.value };
    }

    if (current.value && current.value.id) {
      // Editovanje: Admin može sve, korisnik može samo svoje
      if (!isMine(current.value) && !isAdmin.value) {
        toast.warning("Predefined categories cannot be edited. Create a personal copy instead.");
        return;
      }
      await cats.updateOne(current.value.id, fullPayload);
    } else {
      await cats.createOne(fullPayload);
    }
    await reload();
    modalOpen.value = false;
    current.value = null;
  } catch (e) {
    toast.error(e?.response?.data?.message || "Operation failed");
  }
}

function askDelete(c) {
    if (!isMine(c) && !isAdmin.value) {
        toast.warning("Predefined categories cannot be deleted.");
        return;
    }
    toDeleteId = c.id;
    confirmMessage.value = `This action is permanent. Delete category “${c.name}”?`;
    confirmOpen.value = true;
}

async function doDelete() {
  if (!toDeleteId) return;
  try {
    await cats.removeOne(toDeleteId);
    await reload();
  } catch (e) {
    toast.error(e?.response?.data?.message || "Delete failed");
  } finally {
    toDeleteId = null;
  }
}

onMounted(() => cats.fetch({ page: 0, size: 200 }));
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
.muted {
    color: var(--muted);
}

.badge {
    margin-left: 8px;
    padding: 2px 8px;
    border-radius: 999px;
    font-size: 12px;
    opacity: .85;
}
.badge--me {
    background: rgba(34,197,94,.15);
    color: #22c55e;
}
.badge--global {
    background: rgba(148,163,184,.15);
    color: #94a3b8;
}

</style>
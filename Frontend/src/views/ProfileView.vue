<template>
  <div class="container page">
    <form class="form card" @submit.prevent="submit">
      <h2>Profile settings</h2>
      <div class="form__row">
        <label class="form__label">First name</label>
        <input class="form__control" v-model="form.firstName" type="text" required maxlength="80" />
      </div>
      <div class="form__row">
        <label class="form__label">Last name</label>
        <input class="form__control" v-model="form.lastName" type="text" required maxlength="80" />
      </div>
      <div class="form__row">
        <label class="form__label">Username</label>
        <input class="form__control" v-model="form.username" type="text" required maxlength="80" />
      </div>
      <div class="form__row">
        <label class="form__label">Email</label>
        <input class="form__control" v-model="form.email" type="email" required maxlength="160" />
      </div>
      <div class="form__row">
        <label class="form__label">Birth date</label>
        <input class="form__control" v-model="form.birthDate" type="date" required />
      </div>
      <div class="form__row">
        <label class="form__label">Profile picture URL (optional)</label>
        <input class="form__control" v-model="form.avatarPath" type="text" maxlength="255" placeholder="e.g. /avatars/user.png" />
      </div>
      <div class="form__row">
        <label class="form__label">Preferred currency (optional)</label>
        <input class="form__control" v-model="form.preferredCurrencyCode" type="text" maxlength="3" placeholder="e.g. EUR, USD, RSD" />
      </div>
      <div style="margin-top:18px;">
        <button class="btn btn--primary" type="submit">Save changes</button>
      </div>
    </form>

    <!-- Admin notes section -->
    <section class="admin-notes card" style="width:100%; max-width:480px;">
      <h2>Notes from admin</h2>

      <div v-if="notesLoading" class="loading">Loading notes...</div>

      <div v-else-if="notesError" class="error">{{ notesError }}</div>

      <div v-else-if="notes.length === 0" class="empty">No notes from admin at this time.</div>

      <ul v-else class="notes-list">
        <li v-for="n in notes" :key="n.id" class="note">
          <div class="note-meta">
            <span class="note-date">{{ formatDate(n.createdAt) }}</span>
            <span class="note-admin">— {{ n.adminUsername || 'Administrator' }}</span>
          </div>
          <p class="note-text">{{ n.note }}</p>
        </li>
      </ul>

      <div class="pagination" v-if="totalPages > 1">
        <button :disabled="page === 0" @click="prevPage">Prethodna</button>
        <span>Strana {{ page + 1 }} od {{ totalPages }}</span>
        <button :disabled="page >= totalPages - 1" @click="nextPage">Sledeća</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { getCurrentUser, updateProfile } from "@/services/serviceUser";
import { getMyNotes } from "@/services/notesService";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();

const form = reactive({
  firstName: "",
  lastName: "",
  username: "",
  email: "",
  birthDate: "",
  avatarPath: "",
  preferredCurrencyCode: ""
});
const userId = ref(null);

// notes
const notes = ref([]);
const page = ref(0);
const size = ref(10);
const totalPages = ref(0);
const notesLoading = ref(false);
const notesError = ref(null);

function formatDate(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleString();
}

async function loadProfile() {
  try {
    const { data } = await getCurrentUser();
    form.firstName = data.firstName ?? "";
    form.lastName = data.lastName ?? "";
    form.username = data.username ?? "";
    form.email = data.email ?? "";
    form.birthDate = data.birthDate ?? "";
    form.avatarPath = data.avatarPath ?? "";
    form.preferredCurrencyCode = data.preferredCurrencyCode ?? "";
    userId.value = data.id;
  } catch (e) {
    alert("Ne mogu da učitam podatke: " + (e.response?.data?.message || e.message));
  }
}

async function submit() {
  try {
    // Pretvori datum u ISO format yyyy-mm-dd ako nije već
    if (form.birthDate && typeof form.birthDate !== "string") {
      form.birthDate = form.birthDate.toISOString().slice(0, 10);
    }
    await updateProfile(userId.value, form);
    alert("Uspešno sačuvano!");
    // opcionalno: osveži auth store user ako ga koristiš
    if (auth && typeof auth.fetchMe === "function") {
      await auth.fetchMe();
    }
  } catch (e) {
    alert("Greška pri čuvanju: " + (e.response?.data?.message || e.message));
  }
}

async function loadNotes() {
  notesLoading.value = true;
  notesError.value = null;
  try {
    const resp = await getMyNotes(page.value, size.value);
    notes.value = resp.data.content || [];
    totalPages.value = resp.data.totalPages || 0;
  } catch (e) {
    console.error("Failed to load notes", e);
    notesError.value = e?.response?.data?.message || e.message || "Greška pri učitavanju napomena.";
    notes.value = [];
    totalPages.value = 0;
  } finally {
    notesLoading.value = false;
  }
}

function prevPage() {
  if (page.value > 0) {
    page.value--;
    loadNotes();
  }
}
function nextPage() {
  if (page.value < totalPages.value - 1) {
    page.value++;
    loadNotes();
  }
}

onMounted(async () => {
  await loadProfile();
  // nakon učitavanja profila, učitaj i napomene
  await loadNotes();
});
</script>

<style scoped>
.container.page {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;
}

.form {
  max-width: 480px;
  width: 100%;
}

.form__row {
  display: grid;
  gap: 1rem;
}

.form__label {
  font-size: 0.95rem;
  color: var(--muted);
}

.form__control {
  --ctrl-bg: rgba(255, 255, 255, 0.04);
  --ctrl-brd: rgba(255, 255, 255, 0.12);
  --ctrl-fg: var(--txt);
  --ctrl-ph: #9fb0a9;
  width: 100%;
  height: 44px;
  padding: 0 12px;
  border-radius: 12px;
  border: 1px solid var(--ctrl-brd);
  background: var(--ctrl-bg);
  color: var(--ctrl-fg);
  outline: none;
  transition:
    border-color 160ms ease,
    box-shadow 160ms ease,
    background-color 160ms ease;
}

/* notes style */
.admin-notes {
  background: rgba(255,255,255,0.03);
  padding: 1rem;
  border-radius: 16px;
}
.notes-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.note {
  padding: .9rem;
  border-radius: 8px;
  background: rgba(0,0,0,0.2);
  margin-bottom: .8rem;
}

.note-meta {
  font-size: .9rem;
  color: #bfbfbf;
  margin-bottom: .4rem;
}

.note-text {
  white-space: pre-wrap;
}

.pagination {
  display:flex;
  gap: 1rem;
  align-items:center;
  margin-top: .8rem;
}

.loading {
  color: #aaa;
}

.empty {
  color: #9aa;
}

.error {
  color: #ff3e2d;
  font-weight: 600;
}
</style>
<template>
  <div class="admin-users page">
    <h1>Users</h1>

    <div v-if="loading" class="loading">Učitavanje korisnika...</div>
    <div v-else>
      <table class="users-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Username</th>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(u, i) in users" :key="u.id">
            <td>{{ i + 1 }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.email }}</td>
            <td>{{ u.role }}</td>
            <td>
              <span :style="{ color: u.blocked ? '#c84b31' : '#1f8c5a' }">
                {{ u.blocked ? "Blokiran" : "Aktivan" }}
              </span>
            </td>
            <td style="display: flex; gap: 7px;">
              <button
                v-if="u.id !== authUserId"
                class="btn btn--sm"
                @click="openAddNote(u)"
              >
                Add note
              </button>
              <button
                v-if="!u.blocked && u.id !== authUserId"
                class="btn btn--sm btn--danger"
                @click="setBlocked(u.id, true)"
                :disabled="blockLoading === u.id"
              >
                {{ blockLoading === u.id ? "..." : "Blokiraj" }}
              </button>
              <button
                v-else-if="u.id !== authUserId"
                class="btn btn--sm btn--success"
                @click="setBlocked(u.id, false)"
                :disabled="blockLoading === u.id"
              >
                {{ blockLoading === u.id ? "..." : "Odblokiraj" }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!users.length" class="empty">Nema korisnika</div>
    </div>

    <AddNoteModal
      v-model="modalVisible"
      :userId="selectedUser?.id || null"
      :username="selectedUser?.username || ''"
      @saved="onNoteSaved"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import AddNoteModal from '@/components/AddNoteModal.vue';
import api from '@/services/api';
import { useAuthStore } from "@/stores/auth";

const users = ref([]);
const loading = ref(false);
const blockLoading = ref(null);

const selectedUser = ref(null);
const modalVisible = ref(false);

const auth = useAuthStore();
const authUserId = auth.user?.id; // Trenutno ulogovani korisnik

async function loadUsers() {
  loading.value = true;
  try {
    const resp = await api.get('/admin/users?page=0&size=100');
    users.value = resp.data.content || resp.data || [];
  } catch (e) {
    console.error('Failed to load users', e);
    users.value = [];
  } finally {
    loading.value = false;
  }
}

async function setBlocked(id, blocked) {
  blockLoading.value = id;
  try {
    await api.patch(`/admin/users/${id}/block?blocked=${blocked}`);
    await loadUsers();
  } catch (e) {
    alert('Greška prilikom blokiranja/odblokiranja korisnika.');
  } finally {
    blockLoading.value = null;
  }
}

function openAddNote(user) {
  selectedUser.value = user;
  modalVisible.value = true;
}

function onNoteSaved() {
  console.log('Note saved for user', selectedUser.value?.id);
}

onMounted(() => {
  loadUsers();
});
</script>

<style scoped>
.admin-users { max-width: 1000px; margin: 1.2rem auto; }
.users-table { width: 100%; border-collapse: collapse; }
.users-table th, .users-table td { padding: 0.6rem 0.8rem; border-bottom: 1px solid #eee; text-align: left; }
.btn--sm { padding: 0.35rem 0.6rem; font-size: 0.9rem; }
.btn--danger {
  background: #c84b31;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.14s;
}
.btn--danger:disabled { opacity: .7; cursor: default; }
.btn--success {
  background: #1f8c5a;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.14s;
}
.btn--success:disabled { opacity: .7; cursor: default; }
.loading { color: #777; }
.empty { color: #999; margin-top: 1rem; }
</style>
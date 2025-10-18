<template>
  <div class="admin-users page">
    <h1>Users</h1>

    <div v-if="loading" class="loading">Učitavanje korisnika...</div>
    <div v-else>
      <table class="users-table">
        <thead>
          <tr><th>#</th><th>Username</th><th>Email</th><th>Role</th><th>Actions</th></tr>
        </thead>
        <tbody>
          <tr v-for="(u, i) in users" :key="u.id">
            <td>{{ i + 1 }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.email }}</td>
            <td>{{ u.role }}</td>
            <td>
              <button class="btn btn--sm" @click="openAddNote(u)">Add note</button>
              <!-- ovde možeš imati i Edit/Delete korisnika dugmad -->
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
import api from '@/services/api'; // koristi tvoj postojeći api client za dohvat korisnika
import { getNotesForUser } from '@/services/notesService'; // opcionalno, ako želiš da prikazuješ notes u admin UI

const users = ref([]);
const loading = ref(false);

const selectedUser = ref(null);
const modalVisible = ref(false);

async function loadUsers() {
  loading.value = true;
  try {
    // prilagodi endpoint ako već imaš admin users endpoint
    const resp = await api.get('/admin/users?page=0&size=100'); // primer; prilagodi stvarnom endpointu
    // ako backend vraća Page, koristi resp.data.content
    users.value = resp.data.content || resp.data || [];
  } catch (e) {
    console.error('Failed to load users', e);
    users.value = [];
  } finally {
    loading.value = false;
  }
}

function openAddNote(user) {
  selectedUser.value = user;
  modalVisible.value = true;
}

function onNoteSaved() {
  // opcionalno: obavesti admina ili osveži nešto
  // npr. osveži users list ili prikaži toast
  // ovde samo console log
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
.loading { color: #777; }
.empty { color: #999; margin-top: 1rem; }
</style>
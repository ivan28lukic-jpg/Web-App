<template>
  <div v-if="modelValue" class="modal-backdrop">
    <div class="modal">
      <header class="modal__header">
        <h3>Dodaj napomenu za {{ username }}</h3>
      </header>

      <div class="modal__body">
        <textarea v-model="note" rows="6" placeholder="Unesi tekst napomene..." class="textarea"></textarea>
        <div v-if="error" class="error">{{ error }}</div>
      </div>

      <footer class="modal__footer">
        <button class="btn btn--ghost" @click="close" :disabled="loading">Otkaži</button>
        <button class="btn btn--primary" @click="submit" :disabled="loading || !note.trim()">
          {{ loading ? 'Šaljem...' : 'Sačuvaj napomenu' }}
        </button>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { createNoteForUser } from '@/services/notesService';
import { useAuthStore } from '@/stores/auth';

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  userId: { type: [String, Number], required: true },
  username: { type: String, default: '' },
});
const emit = defineEmits(['update:modelValue', 'saved']);

const note = ref('');
const loading = ref(false);
const error = ref(null);
const auth = useAuthStore();

watch(() => props.modelValue, (v) => {
  if (v) {
    note.value = '';
    error.value = null;
  }
});

function close() {
  emit('update:modelValue', false);
}

async function submit() {
  error.value = null;
  if (!note.value.trim()) return;
  loading.value = true;
  try {
    // Poziv API-ja; api client će poslati Authorization header iz localStorage ("jwt")
    await createNoteForUser(props.userId, note.value.trim());
    // signal parentu da je sačuvano (može da osveži listu ili prikaže toast)
    emit('saved');
    close();
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'Greška pri čuvanju napomene.';
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(10,10,10,0.45);
  z-index: 60;
}
.modal {
  width: 520px;
  max-width: 95%;
  background: #fff;
  color: #111;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 8px 32px rgba(0,0,0,0.35);
}
.modal__header h3 { margin: 0 0 0.5rem 0; }
.textarea {
  width: 100%;
  padding: 0.8rem;
  border-radius: 8px;
  border: 1px solid #ddd;
  resize: vertical;
  font-size: 1rem;
}
.modal__footer {
  display:flex;
  justify-content:flex-end;
  gap:0.6rem;
  margin-top:0.8rem;
}
.error { color: #b00020; margin-top: 0.6rem; }
</style>
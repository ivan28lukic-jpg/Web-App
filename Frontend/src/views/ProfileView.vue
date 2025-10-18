<template>
    <div class="container page">
      <h2>Profile settings</h2>
      <form class="form card" @submit.prevent="submit">
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
    </div>
  </template>
  
  <script setup>
  import { reactive, ref, onMounted } from "vue";
  import { getCurrentUser, updateProfile } from "@/services/serviceUser";
  
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
  
  onMounted(async () => {
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
  });
  
  async function submit() {
    try {
      // Pretvori datum u ISO format yyyy-mm-dd ako nije već
      if (form.birthDate && typeof form.birthDate !== "string") {
        form.birthDate = form.birthDate.toISOString().slice(0, 10);
      }
      await updateProfile(userId.value, form);
      alert("Uspešno sačuvano!");
    } catch (e) {
      alert("Greška pri čuvanju: " + (e.response?.data?.message || e.message));
    }
  }
  </script>

<style scoped>

.container.page {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.form {
  max-width: 480px;
  width: 100%;
}

.form.card{
  margin: auto;
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
</style>
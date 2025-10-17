<template>
    <div class="container page">
      <h2>Podešavanja profila</h2>
      <form class="form" @submit.prevent="submit">
        <div class="form__row">
          <label class="form__label">Ime</label>
          <input class="form__control" v-model="form.firstName" type="text" required maxlength="80" />
        </div>
        <div class="form__row">
          <label class="form__label">Prezime</label>
          <input class="form__control" v-model="form.lastName" type="text" required maxlength="80" />
        </div>
        <div class="form__row">
          <label class="form__label">Korisničko ime</label>
          <input class="form__control" v-model="form.username" type="text" required maxlength="80" />
        </div>
        <div class="form__row">
          <label class="form__label">Email</label>
          <input class="form__control" v-model="form.email" type="email" required maxlength="160" />
        </div>
        <div class="form__row">
          <label class="form__label">Datum rođenja</label>
          <input class="form__control" v-model="form.birthDate" type="date" required />
        </div>
        <div class="form__row">
          <label class="form__label">Avatar URL (opciono)</label>
          <input class="form__control" v-model="form.avatarPath" type="text" maxlength="255" placeholder="npr. /avatars/user.png" />
        </div>
        <div class="form__row">
          <label class="form__label">Preferirana valuta (opciono)</label>
          <input class="form__control" v-model="form.preferredCurrencyCode" type="text" maxlength="3" placeholder="npr. EUR, USD, RSD" />
        </div>
        <div style="margin-top:18px;">
          <button class="btn btn--primary" type="submit">Sačuvaj izmene</button>
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
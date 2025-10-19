<template>
    <div class="container page">
      <h2>Podešavanja profila</h2>
      <form class="form" @submit.prevent="submit">
        <div class="form__row">
          <label class="form__label">Ime</label>
          <input class="form__control" v-model="form.firstName" type="text" required />
        </div>
        <div class="form__row">
          <label class="form__label">Prezime</label>
          <input class="form__control" v-model="form.lastName" type="text" required />
        </div>
        <div class="form__row">
          <label class="form__label">Email</label>
          <input class="form__control" v-model="form.email" type="email" required />
        </div>
        <!-- Dodaj avatar upload po potrebi -->
        <div style="margin-top:18px;">
          <button class="btn btn--primary" type="submit">Sačuvaj izmene</button>
        </div>
      </form>
    </div>
  </template>
  
  <script setup>
  import { reactive } from "vue";
  import { updateProfile } from "@/services/serviceUser"; // Napiši ovu funkciju
  
  // Pretpostavi da imaš user podatke iz store-a ili API-ja
  const form = reactive({
    firstName: "Ivan",
    lastName: "Lukić",
    email: "ivan@example.com",
  });
  
  async function submit() {
    try {
      await updateProfile(form);
      alert("Uspešno sačuvano!");
    } catch (e) {
      alert("Greška pri čuvanju.");
    }
  }
  </script>
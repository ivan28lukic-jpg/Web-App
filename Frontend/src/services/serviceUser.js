import api from "@/services/api";

// Dohvati podatke o trenutnom korisniku
export function getCurrentUser() {
  return api.get("/users/me");
}

// Izmeni podatke o korisniku po ID-u
export function updateProfile(id, payload) {
  // Očekuje se da proslediš korisnički id i izmenjene podatke
  return api.put(`/users/${id}`, payload);
}
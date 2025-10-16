import { defineStore } from "pinia";
import api from "@/services/api";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("jwt") || null,
    role: localStorage.getItem("role") || null, // "USER" | "ADMIN"
    user: null, // opciono: me endpoint
  }),
  getters: {
    isAuthenticated: (s) => !!s.token,
    isAdmin: (s) => s.role === "ADMIN",
  },
   actions: {
    async login(credentials) {
      // očekujemo { token, userId, role, expiresAt } iz /auth/login
      const { data } = await api.post("/auth/login", credentials);

      this.token = data.token;
      this.role = data.role || null;

      localStorage.setItem("jwt", data.token);
      if (data.role) localStorage.setItem("role", data.role);

      // ⬇⬇⬇ NOVO: sačuvaj ownerId za Saving Goals
      if (data.userId != null) {
        localStorage.setItem("ownerId", String(data.userId));
      }

      // opciono: await this.fetchMe();
    },

    logout() {
      this.token = null;
      this.role = null;
      this.user = null;
      localStorage.removeItem("jwt");
      localStorage.removeItem("role");
      // ⬇⬇⬇ NOVO: očisti i ownerId
      localStorage.removeItem("ownerId");
    },
  },
});
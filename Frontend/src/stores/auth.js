import { defineStore } from "pinia";
import api from "@/services/api";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("jwt") || null,
    role: localStorage.getItem("role") || null,
    user: null,
  }),
  getters: {
    isAuthenticated: (s) => !!s.token,
    isAdmin: (s) => s.role === "ADMIN",
  },
  actions: {
    async login(credentials) {
      const { data } = await api.post("/auth/login", credentials);

      this.token = data.token;
      this.role = data.role || null;
      localStorage.setItem("jwt", data.token);
      if (data.role) localStorage.setItem("role", data.role);

      if (data.userId != null) {
        localStorage.setItem("ownerId", String(data.userId));
      }

      // Dodaj ovo:
      await this.fetchMe();
    },

    async fetchMe() {
      // Podesi pravi endpoint po svom backendu!
      try {
        const { data } = await api.get("/users/me"); // ili /auth/me
        this.user = data;
      } catch (e) {
        this.user = null;
      }
    },

    logout() {
      this.token = null;
      this.role = null;
      this.user = null;
      localStorage.removeItem("jwt");
      localStorage.removeItem("role");
      localStorage.removeItem("ownerId");
      localStorage.removeItem("user");
    },
  },
});
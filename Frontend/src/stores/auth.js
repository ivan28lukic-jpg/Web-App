import { defineStore } from "pinia";
import api from "@/services/api";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("jwt") || null,
    role: localStorage.getItem("role") || null,
    user: null,
    userId: localStorage.getItem("ownerId") || null,
    __initialized: false,
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
        this.userId = String(data.userId);
      }

      await this.fetchMe();
    },

    async fetchMe() {
      try {
        const { data } = await api.get("/users/me"); // ili /auth/me
        this.user = data;
      } catch (e) {
        this.user = null;
      }
    },

    // DODAJ OVO:
    async initialize() {
      this.token = localStorage.getItem("jwt");
      this.role = localStorage.getItem("role");
      this.userId = localStorage.getItem("ownerId");
      this.__initialized = true;
      if (this.token) {
        await this.fetchMe();
      }
    },

    logout() {
      this.token = null;
      this.role = null;
      this.user = null;
      this.userId = null;
      localStorage.removeItem("jwt");
      localStorage.removeItem("role");
      localStorage.removeItem("ownerId");
      localStorage.removeItem("user");
    },
  },
});
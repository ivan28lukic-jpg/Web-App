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
      // očekujemo { token, role } iz /auth/login
      const { data } = await api.post("/auth/login", credentials);
      this.token = data.token;
      this.role = data.role;
      localStorage.setItem("jwt", data.token);
      localStorage.setItem("role", data.role);
      // opciono: this.fetchMe();
    },
    logout() {
      this.token = null;
      this.role = null;
      this.user = null;
      localStorage.removeItem("jwt");
      localStorage.removeItem("role");
    },
    async fetchMe() {
      const { data } = await api.get("/users/me");
      this.user = data;
    },
  },
});
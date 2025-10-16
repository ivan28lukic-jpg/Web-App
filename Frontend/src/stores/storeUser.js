import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    id: null,        // ID korisnika (broj)
    user: null,      // Objekat korisnika (možeš dodati ime, email itd.)
    token: null,     // Auth token (opciono)
  }),
  actions: {
    setUser(user) {
      this.user = user;
      this.id = user?.id || null;
    },
    logout() {
      this.user = null;
      this.id = null;
      this.token = null;
    },
    setToken(token) {
      this.token = token;
    }
  }
});
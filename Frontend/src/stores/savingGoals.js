import { defineStore } from "pinia";
import {
  listSavingGoalsByOwner,
  createSavingGoal,
  updateSavingGoal,
  deleteSavingGoal,
  contributeToGoal,
  withdrawFromGoal,
  getGoalProgress,
} from "@/services/savingGoals";

export const useSavingGoalsStore = defineStore("savingGoals", {
  state: () => ({
    ownerId: Number(localStorage.getItem("ownerId") || 0), // promeni po tvom auth-u
    includeArchived: false,
    items: [],
    loading: false,
    error: null,

    search: "",

    // progress
    progressLoading: false,
    progressError: null,
    progress: null,
  }),
  getters: {
    filtered(state) {
      const q = state.search.trim().toLowerCase();
      if (!q) return state.items;
      return state.items.filter((g) => (g.name || "").toLowerCase().includes(q));
    },
  },
  actions: {
    setOwner(id) { this.ownerId = Number(id); },

    async load() {
      if (!this.ownerId) {
        this.items = [];
        return;
      }
        this.loading = true;
        this.error = null;
      try {
        const { data } = await listSavingGoalsByOwner(this.ownerId, this.includeArchived);
        this.items = Array.isArray(data) ? data : (data?.content ?? []);
      } catch (e) {
        this.error = e?.response?.data?.message || e.message || "Unknown error";
      } finally {
        this.loading = false;
      }
    },

    async add(payload) { await createSavingGoal(payload); await this.load(); },
    async edit(id, payload) { await updateSavingGoal(id, payload); await this.load(); },
    async remove(id) { await deleteSavingGoal(id); await this.load(); },

    async contribute(id, payload) {
      try { await contributeToGoal(id, payload); await this.load(); }
      catch (e) { throw new Error(e?.response?.data?.message || e.message || "Contribute failed"); }
    },
    async withdraw(id, payload) {
      try { await withdrawFromGoal(id, payload); await this.load(); }
      catch (e) { throw new Error(e?.response?.data?.message || e.message || "Withdraw failed"); }
    },

    async loadProgress(id, params = {}) {
      this.progressLoading = true; this.progressError = null;
      try { const { data } = await getGoalProgress(id, params); this.progress = data; }
      catch (e) { this.progressError = e?.response?.data?.message || e.message; }
      finally { this.progressLoading = false; }
    },
  },
});

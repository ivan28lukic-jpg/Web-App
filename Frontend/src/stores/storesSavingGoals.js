import { defineStore } from "pinia";
import {
  listSavingGoalsAll,
  listSavingGoalsByOwner,
  createSavingGoal,
  updateSavingGoal,
  deleteSavingGoal,
  contributeToGoal,
  withdrawFromGoal,
  getGoalProgress,
} from "@/services/servicesSavingGoals";
import { useToast } from "@/composables/useToast";

export const useSavingGoalsStore = defineStore("savingGoals", {
  state: () => ({
    items: [],
    progress: {},
    loading: false,
    error: "",
    creating: false,
    contributing: false,
    ownerId: Number(localStorage.getItem("ownerId") || 0),
    userRole: localStorage.getItem("role") || "USER",
    includeArchived: false,
    search: "",
    progressLoading: false,
    progressError: null,
    progressData: null,
  }),

  getters: {
    active: (state) => state.items.filter((g) => !g.archived),
    archived: (state) => state.items.filter((g) => !!g.archived),
    byId: (state) => (id) => state.items.find((g) => g.id === id),
    filtered(state) {
      const q = state.search.trim().toLowerCase();
      if (!q) return state.items;
      return state.items.filter((g) => (g.name || "").toLowerCase().includes(q));
    },
  },

  actions: {
    setOwner(id) {
      this.ownerId = Number(id);
      localStorage.setItem("ownerId", id);
    },
    setRole(role) {
      this.userRole = role;
      localStorage.setItem("role", role);
    },
    setSearch(val) { this.search = val; },

    async load() {
      this.loading = true;
      this.error = "";
      try {
        let data;
        if (this.userRole === "ADMIN" && this.ownerId) {
          // Admin gleda ciljeve odabranog usera
          ({ data } = await listSavingGoalsByOwner(this.ownerId, this.includeArchived));
        } else if (this.userRole === "ADMIN") {
          // Admin nije izabrao usera — vidi sve ciljeve
          ({ data } = await listSavingGoalsAll(this.includeArchived));
        } else if (this.ownerId) {
          ({ data } = await listSavingGoalsByOwner(this.ownerId, this.includeArchived));
        } else {
          this.items = [];
          this.loading = false;
          return;
        }
        this.items = Array.isArray(data) ? data : (data?.content ?? []);
      } catch (e) {
        this.error = e?.response?.data?.message || e.message || "Greška pri dohvatanju ciljeva štednje.";
      } finally {
        this.loading = false;
      }
    },

    async fetchProgress(goalId) {
      this.progressLoading = true;
      this.progressError = null;
      try {
        const { data } = await getGoalProgress(goalId);
        this.progress[goalId] = data;
        this.progressData = data;
      } catch (e) {
        this.progress[goalId] = null;
        this.progressError = e?.response?.data?.message || e.message || "Greška pri dohvatanju napretka cilja.";
      } finally {
        this.progressLoading = false;
      }
    },

    async createGoal(payload) {
      this.creating = true;
      this.error = "";
      const toast = useToast?.();
      try {
        await createSavingGoal(payload);
        toast?.success?.("Cilj štednje uspešno kreiran!");
        await this.load();
      } catch (e) {
        const msg = e?.response?.data?.message || "Greška pri kreiranju cilja.";
        this.error = msg;
        toast?.error?.(msg);
        throw e;
      } finally {
        this.creating = false;
      }
    },

    async updateGoal(goalId, updatePayload) {
      const toast = useToast?.();
      try {
        await updateSavingGoal(goalId, updatePayload);
        toast?.success?.("Cilj ažuriran!");
        await this.load();
      } catch (e) {
        const msg = e?.response?.data?.message || "Greška pri ažuriranju cilja.";
        this.error = msg;
        toast?.error?.(msg);
        throw e;
      }
    },

    async contributeToGoal(goalId, req) {
      this.contributing = true;
      this.error = "";
      const toast = useToast?.();
      try {
        await contributeToGoal(goalId, req);
        toast?.success?.("Uplata uspešna!");
        await this.load();
      } catch (e) {
        const msg = e?.response?.data?.message || "Greška pri uplati na cilj.";
        this.error = msg;
        toast?.error?.(msg);
        throw e;
      } finally {
        this.contributing = false;
      }
    },

    async withdrawFromGoal(goalId, req) {
      this.contributing = true;
      this.error = "";
      const toast = useToast?.();
      try {
        await withdrawFromGoal(goalId, req);
        toast?.success?.("Isplata uspešna!");
        await this.load();
      } catch (e) {
        const msg = e?.response?.data?.message || "Greška pri isplati sa cilja.";
        this.error = msg;
        toast?.error?.(msg);
        throw e;
      } finally {
        this.contributing = false;
      }
    },

    async deleteGoal(goalId) {
      const toast = useToast?.();
      try {
        await deleteSavingGoal(goalId);
        toast?.success?.("Cilj obrisan.");
        await this.load();
      } catch (e) {
        const msg = e?.response?.data?.message || "Greška pri brisanju cilja.";
        this.error = msg;
        toast?.error?.(msg);
        throw e;
      }
    },
  },
});
import { defineStore } from "pinia";
import axios from "axios";
import { useToast } from "@/composables/useToast";

export const useSavingGoalsStore = defineStore("savingGoals", {
  state: () => ({
    items: [],
    progress: {},
    loading: false,
    error: "",
    creating: false,
    contributing: false,
  }),

  getters: {
    active: (s) => s.items.filter((g) => !g.archived),
    archived: (s) => s.items.filter((g) => !!g.archived),
    byId: (s) => (id) => s.items.find((g) => g.id === id),
  },

    actions: {
      async fetchAll(ownerId) {
        this.loading = true;
        this.error = "";
        try {
          let data;
          if (ownerId !== undefined && ownerId !== null) {
            // Samo za usera
            ({ data } = await axios.get(`/api/saving-goals/owner/${ownerId}`));
          } else {
            // Za admina: svi ciljevi
            ({ data } = await axios.get(`/api/saving-goals`));
          }
          this.items = data;
        } catch (e) {
          this.error = e?.response?.data?.message || "Greška pri dohvatanju ciljeva štednje.";
        } finally {
          this.loading = false;
        }
      
      },

    async fetchProgress(goalId) {
      try {
        const { data } = await axios.get(`/api/saving-goals/${goalId}/progress`);
        this.progress[goalId] = data;
      } catch (e) {
        this.progress[goalId] = null;
      }
    },

    async createGoal(payload) {
      this.creating = true;
      this.error = "";
      const toast = useToast?.();
      try {
        const { data } = await axios.post(`/api/saving-goals`, payload);
        this.items.push(data);
        toast?.success?.("Cilj štednje uspešno kreiran!");
        return data;
      } catch (e) {
        const msg = e?.response?.data?.message || "Greška pri kreiranju cilja.";
        this.error = msg;
        toast?.error?.(msg);
        throw e;
      } finally {
        this.creating = false;
      }
    },

    async contributeToGoal(goalId, req) {
      this.contributing = true;
      this.error = "";
      const toast = useToast?.();
      try {
        await axios.post(`/api/saving-goals/${goalId}/contribute`, req);
        const { data } = await axios.get(`/api/saving-goals/${goalId}`);
        const idx = this.items.findIndex((g) => g.id === goalId);
        if (idx !== -1) this.items[idx] = data;
        toast?.success?.("Uplata uspešna!");
      } catch (e) {
        const msg = e?.response?.data?.message || "Greška pri uplati na cilj.";
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
        await axios.delete(`/api/saving-goals/${goalId}`);
        this.items = this.items.filter((g) => g.id !== goalId);
        toast?.success?.("Cilj obrisan.");
      } catch (e) {
        const msg = e?.response?.data?.message || "Greška pri brisanju cilja.";
        this.error = msg;
        toast?.error?.(msg);
        throw e;
      }
    },

    async updateGoal(goalId, updatePayload) {
      const toast = useToast?.();
      try {
        const { data } = await axios.put(`/api/saving-goals/${goalId}`, updatePayload);
        const idx = this.items.findIndex((g) => g.id === goalId);
        if (idx !== -1) this.items[idx] = data;
        toast?.success?.("Cilj ažuriran.");
        return data;
      } catch (e) {
        const msg = e?.response?.data?.message || "Greška pri ažuriranju cilja.";
        this.error = msg;
        toast?.error?.(msg);
        throw e;
      }
    },
  },
});
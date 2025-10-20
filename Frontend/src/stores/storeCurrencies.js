import { defineStore } from "pinia";
import {
  adminListCurrencies,
  adminCreateCurrency,
  adminUpdateCurrency,
  adminDeleteCurrency,
  adminFetchCurrency,
  adminRefreshCurrency,
} from "@/services/currenciesService";
import { useToast } from "@/composables/useToast";

export const useCurrenciesStore = defineStore("currencies", {
  state: () => ({
    items: [],
    loading: false,
    error: "",
    suggestLoading: false,
    suggestValue: null,
    suggestSource: null,
    suggestFetchedAt: null,
  }),
  actions: {
    async fetchAll() {
      this.loading = true;
      try {
        const { data } = await adminListCurrencies();
        this.items = data;
      } catch (e) {
        this.error = e?.response?.data?.message || e.message || "Failed to load currencies";
      } finally {
        this.loading = false;
      }
    },
    async createOne(payload) {
      const toast = useToast();
      try {
        await adminCreateCurrency(payload);
        await this.fetchAll();
        toast.success("Currency created");
      } catch (e) {
        toast.error(e?.response?.data?.message || "Create failed");
        throw e;
      }
    },
    async updateOne(id, payload) {
      const toast = useToast();
      try {
        await adminUpdateCurrency(id, payload);
        await this.fetchAll();
        toast.success("Currency updated");
      } catch (e) {
        toast.error(e?.response?.data?.message || "Update failed");
        throw e;
      }
    },
    async removeOne(id) {
      const toast = useToast();
      try {
        await adminDeleteCurrency(id);
        await this.fetchAll();
        toast.success("Currency deleted");
      } catch (e) {
        toast.error(e?.response?.data?.message || "Delete failed");
        throw e;
      }
    },
    async fetchSuggestion(code) {
      this.suggestLoading = true;
      this.suggestValue = null;
      this.suggestSource = null;
      this.suggestFetchedAt = null;
      try {
        const { data } = await adminFetchCurrency(code);
        this.suggestValue = data.suggestedValueVsEur;
        this.suggestSource = data.source;
        this.suggestFetchedAt = data.fetchedAt;
      } catch (e) {
        this.suggestValue = null;
        this.suggestSource = null;
        this.suggestFetchedAt = null;
        this.error = e?.response?.data?.message || "No suggestion";
      } finally {
        this.suggestLoading = false;
      }
    },
    async refreshCurrency(id) {
      const toast = useToast();
      try {
        await adminRefreshCurrency(id);
        await this.fetchAll();
        toast.success("Currency refreshed");
      } catch (e) {
        toast.error(e?.response?.data?.message || "Refresh failed");
        throw e;
      }
    },
  },
});
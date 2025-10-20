import { defineStore } from "pinia";
import { adminListTransactions } from "@/services/adminTransactionsService";

export const useAdminTransactionsStore = defineStore("adminTransactions", {
  state: () => ({
    items: [],
    loading: false,
    error: "",
    filters: {
      ownerId: "",
      categoryId: "",
      min: "",
      max: "",
      fromDate: "",
      toDate: "",
      sort: "occurredAt,DESC",
      page: 0,
      size: 20
    },
    total: 0,
  }),
  actions: {
    async fetchAll() {
      this.loading = true;
      this.error = "";
      try {
        const { data } = await adminListTransactions(this.filters);
        this.items = data.content || data; // zavisi da li backend vraća paginaciju
        this.total = data.totalElements || (data.content ? data.content.length : data.length);
      } catch (e) {
        this.error = e?.response?.data?.message || "Failed to load transactions";
      } finally {
        this.loading = false;
      }
    },
    setFilter(key, value) {
      this.filters[key] = value;
    },
    resetFilters() {
      this.filters = {
        ownerId: "",
        categoryId: "",
        min: "",
        max: "",
        fromDate: "",
        toDate: "",
        sort: "occurredAt,DESC",
        page: 0,
        size: 20
      };
    }
  }
});
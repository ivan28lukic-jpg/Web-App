import { defineStore } from "pinia";
import {
  listTransactions,
  createTransaction,
  updateTransaction,
  deleteTransaction,
} from "@/services/transactions";
import { useToast } from "@/composables/useToast";

export const useTransactionsStore = defineStore("transactions", {
  state: () => ({
    items: [],
    loading: false,
    error: "",
    page: 0,
    size: 20,
    total: 0,
    filters: {
      search: "",
      walletId: "",
      type: "",           // INCOME | EXPENSE | ""(all)
      dateFrom: "",
      dateTo: "",
      categoryId: "",
    },
  }),

  actions: {
    async fetch(p = {}) {
      this.loading = true;
      this.error = "";
      try {
        const params = {
          page: this.page,
          size: this.size,
          ...this.filters,
          ...p,
        };
        const { data } = await listTransactions(params);

        if (Array.isArray(data)) {
          this.items = data;
          this.total = data.length;
        } else {
          this.items = data.content || [];
          this.total = data.totalElements ?? this.items.length;
          this.page = data.number ?? params.page ?? 0;
          this.size = data.size ?? params.size ?? 20;
        }
      } catch (e) {
        this.error = e?.response?.data?.message || "Failed to load transactions";
      } finally {
        this.loading = false;
      }
    },

    async createOne(payload) {
      const toast = useToast();
      try {
        // normalizacija amount-a (zarez -> tačka)
        const amount = Number(String(payload.amount).replace(",", "."));
        await createTransaction({ ...payload, amount });
        await this.fetch({ page: 0 }); // osveži listu
        toast.success("Transaction created");
      } catch (e) {
        const msg = e?.response?.data?.message || "Create failed";
        toast.error(msg);
        throw e;
      }
    },

    async updateOne(id, payload) {
      const toast = useToast();
      try {
        const amount =
          payload.amount == null ? undefined : Number(String(payload.amount).replace(",", "."));
        await updateTransaction(id, { ...payload, amount });
        await this.fetch(); // refetch (pokrije 204 bez tela)
        toast.success("Transaction updated");
      } catch (e) {
        const msg = e?.response?.data?.message || "Update failed";
        toast.error(msg);
        throw e;
      }
    },

    async removeOne(id) {
      const toast = useToast();
      try {
        await deleteTransaction(id);
        await this.fetch();
        toast.success("Transaction deleted");
      } catch (e) {
        const status = e?.response?.status;
        if (status === 403 || status === 405) {
          toast.warning("Deleting transactions is not allowed by server.");
        } else {
          toast.error(e?.response?.data?.message || "Delete failed");
        }
        throw e;
      }
    },
  },
});

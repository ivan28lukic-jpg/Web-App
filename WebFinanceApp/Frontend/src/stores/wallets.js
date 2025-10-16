import { defineStore } from "pinia";
import {
  listWallets,
  createWallet,
  updateWallet,
  deleteWallet,
  archiveWalletFull,
  getWallet,
} from "@/services/wallets";
import { getCurrencies } from "@/services/meta";
import { useToast } from "@/composables/useToast";

export const useWalletsStore = defineStore("wallets", {
  state: () => ({
    items: [],
    loading: false,
    error: "",
    page: 0,
    size: 20,
    total: 0,
    currencies: [],
  }),

  getters: {
    active: (s) => s.items.filter((w) => !w.archived),
    archived: (s) => s.items.filter((w) => !!w.archived),
  },

  actions: {
    async fetch({ search = "", page = 0, size = 100, ownerId } = {}) {
      this.loading = true;
      this.error = "";
      try {
        const params = { search, page, size };
        if (ownerId) params.ownerId = ownerId;
        const { data } = await listWallets(params);
        if (Array.isArray(data)) {
          this.items = data;
          this.total = data.length;
        } else {
          this.items = data.content || [];
          this.total = data.totalElements ?? this.items.length;
          this.page = data.number ?? page;
          this.size = data.size ?? size;
        }
      } catch (e) {
        this.error = e?.response?.data?.message || "Failed to load wallets";
      } finally {
        this.loading = false;
      }
    },

    async fetchAll({ page = 0, size = 100 } = {}) {
      this.loading = true;
      this.error = "";
      try {
        const { data } = await listWallets({ page, size });
        if (Array.isArray(data)) {
          this.items = data;
          this.total = data.length;
        } else {
          this.items = data.content || [];
          this.total = data.totalElements ?? this.items.length;
          this.page = data.number ?? page;
          this.size = data.size ?? size;
        }
      } catch (e) {
        this.error = e?.response?.data?.message || "Failed to load all wallets";
      } finally {
        this.loading = false;
      }
    },

    async ensureCurrencies() {
      if (this.currencies.length) return this.currencies;
      this.currencies = await getCurrencies();
      return this.currencies;
    },

    async createOne(payload) {
      const toast = useToast();
      try {
        const balance =
          payload.balance === "" || payload.balance == null
            ? undefined
            : Number(String(payload.balance).replace(",", "."));
        await createWallet({ ...payload, balance });
        await this.fetch();
        toast.success("Wallet created");
      } catch (e) {
        const msg = e?.response?.data?.message || "Create failed";
        toast.error(msg);
        throw e;
      }
    },

    async updateOne(id, payload) {
      const toast = useToast();
      try {
        const balance =
          payload.balance === "" || payload.balance == null
            ? undefined
            : Number(String(payload.balance).replace(",", "."));
        await updateWallet(id, { ...payload, balance });
        await this.fetch(); // osveži jer backend često vraća 204
        toast.success("Wallet updated");
      } catch (e) {
        const msg = e?.response?.data?.message || "Update failed";
        toast.error(msg);
        throw e;
      }
    },

    async setArchived(id, archived = true) {
      const toast = useToast();
      try {
        let w = this.items.find((x) => x.id === id);
        if (!w) {
          const { data } = await getWallet(id);
          w = data;
        }
        await archiveWalletFull(id, w, archived); // PUT sa celim telom
        await this.fetch();
        toast.success(archived ? "Wallet archived" : "Wallet restored");
      } catch (e) {
        const msg = e?.response?.data?.message || "Archive toggle failed";
        toast.error(msg);
        throw e;
      }
    },

    async removeOne(id) {
      const toast = useToast();
      try {
        await deleteWallet(id);
        await this.fetch();
        toast.success("Wallet deleted");
      } catch (e) {
        const msg = e?.response?.data?.message || "Delete failed";
        toast.error(msg);
        throw e;
      }
    },
  },
});
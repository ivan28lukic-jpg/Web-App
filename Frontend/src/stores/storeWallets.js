import { defineStore } from "pinia";
import {
  listWallets,
  listWalletsByOwner,
  createWallet,
  updateWallet,
  deleteWallet,
  archiveWalletFull,
  getWallet,
} from "@/services/serviceWallets";
import { getCurrencies } from "@/services/meta";
import { useToast } from "@/composables/useToast";
import { useAuthStore } from "@/stores/auth";

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
    mine: (s) => {
      const auth = useAuthStore();
      const userId = auth.user?.id;
      return s.items.filter((w) => w.ownerId === userId);
    },
  },

  actions: {
    async fetch({ search = "", page = 0, size = 100, ownerId = null } = {}) {
      this.loading = true;
      this.error = "";
      try {
        let data;
        if (ownerId) {
          // Dohvati samo za datog vlasnika
          const res = await listWalletsByOwner(ownerId);
          data = Array.isArray(res.data) ? res.data : (res.data?.content ?? []);
          this.items = data;
          this.total = data.length;
        } else {
          // Svi novčanici (ili po search parametru)
          const { data: dataRaw } = await listWallets({ search, page, size });
          if (Array.isArray(dataRaw)) {
            this.items = dataRaw;
            this.total = dataRaw.length;
          } else {
            this.items = dataRaw.content || [];
            this.total = dataRaw.totalElements ?? this.items.length;
            this.page = dataRaw.number ?? page;
            this.size = dataRaw.size ?? size;
          }
        }
      } catch (e) {
        this.error = e?.response?.data?.message || "Failed to load wallets";
      } finally {
        this.loading = false;
      }
    },

    async fetchMine() {
      const auth = useAuthStore();
      const userId = auth.user?.id;
      if (userId) {
        await this.fetch({ ownerId: userId });
      }
    },

    async fetchForOwner(ownerId) {
      // Wrapper za jasnoću
      return this.fetch({ ownerId });
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
        await this.fetchMine();
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
        await this.fetchMine();
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
        await archiveWalletFull(id, w, archived);
        await this.fetchMine();
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
        await this.fetchMine();
        toast.success("Wallet deleted");
      } catch (e) {
        const msg = e?.response?.data?.message || "Delete failed";
        toast.error(msg);
        throw e;
      }
    },
  },
});
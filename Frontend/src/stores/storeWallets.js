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
    ownerId: null, // admin bira usera, user ne dira ovo
  }),

  getters: {
    active: (s) => s.items.filter((w) => !w.archived),
    archived: (s) => s.items.filter((w) => !!w.archived),
    mine: (s) => {
      const auth = useAuthStore();
      const userId = auth.user?.id;
      return s.items.filter((w) => w.ownerId === userId);
    },
    visible: (s) => {
      const auth = useAuthStore();
      return auth.user?.role === "ADMIN"
        ? s.items
        : s.items.filter((w) => w.ownerId === auth.user?.id);
    },
  },

  actions: {
    setOwner(id) { this.ownerId = Number(id) || null; },

    async fetch({ search = "", page = 0, size = 100 } = {}) {
      this.loading = true;
      this.error = "";
      try {
        const auth = useAuthStore();
        let data;
        // ADMIN bira usera
        if (auth.user?.role === "ADMIN" && this.ownerId) {
          const res = await listWalletsByOwner(this.ownerId);
          data = Array.isArray(res.data) ? res.data : (res.data?.content ?? []);
          this.items = data;
          this.total = data.length;
        // ADMIN vidi sve
        } else if (auth.user?.role === "ADMIN") {
          const { data: dataRaw } = await listWallets({ search, page, size }); // NEMA ownerId!
          if (Array.isArray(dataRaw)) {
            this.items = dataRaw;
            this.total = dataRaw.length;
          } else {
            this.items = dataRaw.content || [];
            this.total = dataRaw.totalElements ?? this.items.length;
            this.page = dataRaw.number ?? page;
            this.size = dataRaw.size ?? size;
          }
        // OBICAN user vidi svoje
        } else if (auth.user?.id) {
          const res = await listWalletsByOwner(auth.user.id);
          data = Array.isArray(res.data) ? res.data : (res.data?.content ?? []);
          this.items = data;
          this.total = data.length;
        } else {
          this.items = [];
          this.loading = false;
          return;
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
        await this.fetch();
      }
    },

    async fetchForOwner(ownerId) {
      this.setOwner(ownerId);
      return this.fetch();
    },

    async ensureCurrencies() {
      if (this.currencies.length) return this.currencies;
      this.currencies = await getCurrencies();
      return this.currencies;
    },

    async createOne(payload) {
      const toast = useToast();
      const auth = useAuthStore();
      try {
        // Odredi pravi ownerId
        let ownerId = auth.user?.role === "ADMIN" && this.ownerId
          ? this.ownerId
          : auth.user?.id;

        const balance =
          payload.balance === "" || payload.balance == null
            ? undefined
            : Number(String(payload.balance).replace(",", "."));
        await createWallet({ ...payload, ownerId, balance });
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
        await this.fetch();
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
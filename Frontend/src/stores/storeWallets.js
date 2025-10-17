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
    ownerId: null, // koristi se samo za filtriranje/admin prikaz, NE za create
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

    // IZMENJENO: Dodali smo parametar includeArchived za filter
    async fetch({ search = "", page = 0, size = 100, includeArchived = false } = {}) {
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
          const { data: dataRaw } = await listWallets({ search, page, size, includeArchived }); // NEMA ownerId!
          if (Array.isArray(dataRaw)) {
            this.items = dataRaw;
            this.total = dataRaw.length;
          } else {
            this.items = dataRaw.content || [];
            this.total = dataRaw.totalElements ?? this.items.length;
            this.page = dataRaw.number ?? page;
            this.size = dataRaw.size ?? size;
          }
        // OBICAN user vidi svoje + filter archived
        } else if (auth.user?.id) {
          const res = await listWallets({
            search,
            page,
            size,
            ownerId: auth.user.id,
            includeArchived
          });
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

    async fetchMine(includeArchived = false) {
      const auth = useAuthStore();
      const userId = auth.user?.id;
      if (userId) {
        await this.fetch({ includeArchived });
      }
    },

    async fetchForOwner(ownerId, includeArchived = false) {
      this.setOwner(ownerId);
      return this.fetch({ includeArchived });
    },

    async ensureCurrencies() {
      if (this.currencies.length) return this.currencies;
      this.currencies = await getCurrencies();
      return this.currencies;
    },

    async createOne(payload) {
      const toast = useToast();
      try {
        // NEMOJ slati ownerId, backend ga određuje iz tokena!
        // OVO JE KLJUČNA IZMJENA:
        // Ako payload ima initialBalance (iz forme), mapiraj ga u balance
        let balance = payload.balance;
        if (balance === undefined && payload.initialBalance !== undefined) {
          balance = payload.initialBalance;
        }
        balance =
          balance === "" || balance == null
            ? undefined
            : Number(String(balance).toString().replace(",", "."));
        await createWallet({ ...payload, balance }); // šalje balance backendu!
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
        let balance = payload.balance;
        if (balance === undefined && payload.initialBalance !== undefined) {
          balance = payload.initialBalance;
        }
        balance =
          balance === "" || balance == null
            ? undefined
            : Number(String(balance).toString().replace(",", "."));
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
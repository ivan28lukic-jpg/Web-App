import { defineStore } from "pinia";
import {
  listCategoriesByOwner,
  createCategory,
  updateCategory,
  deleteCategory,
} from "@/services/servicesCategories";
import { useToast } from "@/composables/useToast";

export const useCategoriesStore = defineStore("categories", {
  state: () => ({
    items: [],
    loading: false,
    error: "",
    page: 0,
    size: 100,
    total: 0,
    filters: {
      search: "",
      type: "", // INCOME | EXPENSE | "" (all) – ako backend podržava
    },
  }),

  actions: {
    async fetch(p = {}) {
      this.loading = true;
      this.error = "";
      try {
        const ownerId = localStorage.getItem("ownerId");

        if (!ownerId) {
          throw new Error("Nije pronađen ownerId! Korisnik mora biti ulogovan.");
        }

        const params = {
          page: this.page,
          size: this.size,
          ...this.filters,
          ...p,
          includeGlobal: true,
        };

        const { data } = await listCategoriesByOwner(ownerId, params);

        if (Array.isArray(data)) {
          this.items = data;
          this.total = data.length;
        } else {
          this.items = data.content || [];
          this.total = data.totalElements ?? this.items.length;
          this.page = data.number ?? params.page ?? 0;
          this.size = data.size ?? params.size ?? 100;
        }
      } catch (e) {
        this.error =
          e?.response?.data?.message || e.message || "Failed to load categories";
      } finally {
        this.loading = false;
      }
    },

    async createOne(payload) {
      const toast = useToast();
      try {
        const ownerId = localStorage.getItem("ownerId");
        // Dodaj ownerId u payload da bi bila lična kategorija
        const data = {
          ...payload,
          ownerId,
        };
        await createCategory(data);
        await this.fetch({ page: 0 });
        toast.success("Category created");
      } catch (e) {
        toast.error(e?.response?.data?.message || "Create failed");
        throw e;
      }
    },

    async updateOne(id, payload) {
      const toast = useToast();
      try {
        const body = {
          ...payload,
          type: payload.type ? String(payload.type).toUpperCase() : undefined,
        };

        await updateCategory(id, body);

        const i = this.items.findIndex(x => x.id === id);
        if (i !== -1) {
          this.items[i] = { ...this.items[i], ...body };
        }

        toast.success("Category updated");
      } catch (e) {
        toast.error(e?.response?.data?.message || "Update failed");
        throw e;
      }
    },

    async removeOne(id) {
      const toast = useToast();
      try {
        await deleteCategory(id);
        await this.fetch();
        toast.success("Category deleted");
      } catch (e) {
        const status = e?.response?.status;
        if (status === 403 || status === 405) {
          toast.warning("Deleting categories is not allowed by server.");
        } else {
          toast.error(e?.response?.data?.message || "Delete failed");
        }
        throw e;
      }
    },
  },
});
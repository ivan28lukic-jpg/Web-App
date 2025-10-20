import { defineStore } from "pinia";
import {
  listCategoriesByOwner,
  createCategory,
  updateCategory,
  deleteCategory,
  adminListCategories,
  adminCreateCategory,
  adminUpdateCategory,
  adminDeleteCategory,
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
      type: "",
    },
    adminMode: false, // Flag for admin mode
  }),

  actions: {
    setAdminMode(isAdmin) {
      this.adminMode = !!isAdmin;
    },

    async fetch(p = {}) {
      this.loading = true;
      this.error = "";
      try {
        if (this.adminMode) {
          // ADMIN: koristi admin endpoint za listanje GLOBAL
          const params = {
            page: this.page,
            size: this.size,
            ...this.filters,
            ...p,
          };
          const { data } = await adminListCategories(params);
          this.items = data.content || [];
          this.total = data.totalElements ?? this.items.length;
          this.page = data.number ?? params.page ?? 0;
          this.size = data.size ?? params.size ?? 100;
        } else {
          // USER: koristi user endpoint
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
        }
      } catch (e) {
        this.error =
          e?.response?.data?.message || e.message || "Failed to load categories";
      } finally {
        this.loading = false;
      }
    },

    async createOne(payload, isAdmin = false) {
      const toast = useToast();
      try {
        let data = { ...payload };
        if (isAdmin || this.adminMode) {
          // ADMIN: koristi admin endpoint, bez ownerId
          if ("ownerId" in data) delete data.ownerId;
          await adminCreateCategory(data);
        } else {
          // USER: koristi user endpoint, mora ownerId
          data.ownerId = payload.ownerId ?? localStorage.getItem("ownerId");
          await createCategory(data);
        }
        await this.fetch({ page: 0 });
        toast.success("Category created");
      } catch (e) {
        toast.error(e?.response?.data?.message || "Create failed");
        throw e;
      }
    },

    async updateOne(id, payload, isAdmin = false) {
      const toast = useToast();
      try {
        if (isAdmin || this.adminMode) {
          // ADMIN: koristi admin endpoint
          await adminUpdateCategory(id, payload);
        } else {
          // USER: koristi user endpoint
          const body = {
            ...payload,
            ownerId: payload.ownerId ?? localStorage.getItem("ownerId"),
            type: payload.type ? String(payload.type).toUpperCase() : undefined,
          };
          await updateCategory(id, body);
        }

        // Refetch
        await this.fetch();
        toast.success("Category updated");
      } catch (e) {
        toast.error(e?.response?.data?.message || "Update failed");
        throw e;
      }
    },

    async removeOne(id, isAdmin = false) {
      const toast = useToast();
      try {
        if (isAdmin || this.adminMode) {
          await adminDeleteCategory(id);
        } else {
          await deleteCategory(id);
        }
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
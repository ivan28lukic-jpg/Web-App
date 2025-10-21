<template>
    <div class="top-expenses">
      <h2>Top 10 expenses for period</h2>
      <form class="top-expenses-filter" @submit.prevent="reload">
        <input type="date" v-model="from" />
        <input type="date" v-model="to" />
        <select v-model="categoryId">
          <option value="">All categories</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
        <input type="number" v-model="minAmount" placeholder="Min amount" />
        <input type="number" v-model="maxAmount" placeholder="Max amount" />
        <button type="submit">Show</button>
      </form>
      <div v-if="loading" class="loading">Loading...</div>
      <div v-if="error" class="error">{{ error }}</div>
      <table v-if="!loading && expenses.length">
        <thead>
          <tr>
            <th>#</th>
            <th>Category</th>
            <th>Description</th>
            <th>Amount</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(tx, i) in expenses" :key="tx.id">
            <td>{{ i + 1 }}</td>
            <td>{{ tx.categoryName }}</td>
            <td>{{ tx.description }}</td>
            <td>{{ tx.amount }}</td>
            <td>{{ new Date(tx.occurredAt).toLocaleDateString() }}</td>
          </tr>
        </tbody>
      </table>
      <div v-if="!loading && !expenses.length" style="padding:1rem;">No expenses for the selected period.</div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from "vue";
  import { getTopExpenses } from "@/services/statsService";
  import { useCategoriesStore } from "@/stores/storeCategories";
  import { useAuthStore } from "@/stores/auth";
  
  const authStore = useAuthStore();
  const ownerId = authStore.user?.id;
  const categoriesStore = useCategoriesStore();
  
  const from = ref(new Date(new Date().getFullYear(), 0, 1).toISOString().slice(0, 10));
  const to = ref(new Date().toISOString().slice(0, 10));
  const categoryId = ref("");
  const min = ref("");
  const max = ref("");
  const expenses = ref([]);
  const loading = ref(false);
  const error = ref("");
  
  const categories = categoriesStore.items;
  
  async function reload() {
    loading.value = true;
    error.value = "";
    try {
      const { data } = await getTopExpenses({
        ownerId,
        from: from.value,
        to: to.value,
        categoryId: categoryId.value || undefined,
        min: min.value || undefined,
        max: max.value || undefined,
        limit: 10,
      });
      expenses.value = data;
    } catch (e) {
      error.value = e?.response?.data?.message || e.message || "Greška pri učitavanju.";
    } finally {
      loading.value = false;
    }
  }
  
  onMounted(() => {
    if (!categoriesStore.items.length && typeof categoriesStore.fetch === "function") {
      categoriesStore.fetch();
    }
    reload();
  });
  </script>
  
  <style scoped>
  .top-expenses {
  background: rgba(60,80,75,0.94);
  border-radius: 2rem;
  max-width: 950px;
  margin: 2.5rem auto 0 auto;
  padding: 2.2rem 2rem 2rem 2rem;
  box-shadow: 0 6px 32px rgba(30,60,60,0.22);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.top-expenses h2 {
  color: #eafcf5;
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 1.4rem;
  letter-spacing: 1px;
  text-align: center;
}

.top-expenses-filter {
  display: flex;
  gap: 1.1rem;
  align-items: center;
  margin-bottom: 1.7rem;
  flex-wrap: wrap;
  justify-content: center;
  width: 100%;
}

.top-expenses-filter input[type="date"],
.top-expenses-filter select,
.top-expenses-filter input[type="number"] {
  padding: 0.7rem 1.1rem;
  border-radius: 0.7rem;
  border: 2px solid #b2d3c2;
  font-size: 1rem;
  background: #f9fafb;
  color: #17453c;
  box-shadow: 0 1px 8px rgba(30,60,60,0.10);
  transition: border .19s, box-shadow .19s;
  min-width: 120px;
}

.top-expenses-filter select:focus,
.top-expenses-filter input:focus {
  border-color: #20c589;
  outline: none;
  box-shadow: 0 2px 10px rgba(32,197,137,0.14);
}

.top-expenses-filter button {
  padding: 0.7rem 1.2rem;
  border-radius: 1.2rem;
  background: linear-gradient(90deg,#20c589 70%,#1ba37e 100%);
  color: #fff;
  font-weight: 600;
  border: none;
  font-size: 1.07rem;
  box-shadow: 0 2px 12px rgba(30,60,90,0.13);
  transition: background 0.19s,transform 0.19s;
  cursor: pointer;
}

.top-expenses-filter button:hover,
.top-expenses-filter button:focus {
  background: linear-gradient(90deg,#1ba37e 60%,#20c589 100%);
  transform: translateY(-2px) scale(1.04);
  outline: none;
}

/* TABELA */
table {
  width: 100%;
  background: rgba(255,255,255,0.99);
  border-radius: 1.1rem;
  overflow: hidden;
  box-shadow: 0 2px 16px rgba(30,90,90,0.09);
  margin-top: 0.5rem;
}
thead {
  background: #f5fcfa;
}
th, td {
  padding: 0.85rem 1rem;
  text-align: left;
  font-size: 1.04rem;
  font-weight: 500;
}
th {
  color: #20c589;
  font-size: 1.07rem;
  font-weight: 700;
  background: #eafcf5;
  border-bottom: 2px solid #def9f1;
}
tbody tr {
  border-bottom: 1px solid #e6f2f3;
  transition: background 0.13s;
}
tbody tr:hover {
  background: #f1fbf7;
}
td {
  color: #173e2e;
}
.loading, .error {
  text-align: center;
  padding: 1rem;
  font-size: 1.1rem;
}
.loading { color: #aaa; }
.error {
  color: #ff3e2d;
  font-weight: bold;
  border-radius: 0.5rem;
  background: rgba(255,70,70,0.07);
}

/* Responsive */
@media (max-width: 900px) {
  .top-expenses {
    padding: 1rem 0.5rem;
    max-width: 99vw;
  }
  table th, table td {
    padding: 0.6rem 0.3rem;
    font-size: 0.97rem;
  }
  .top-expenses-filter {
    gap: 0.5rem;
  }
}
  </style>
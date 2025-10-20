<template>
    <div class="container page">
      <h2>Admin: Transactions</h2>
  
      <form class="filters" @submit.prevent="onFilter">
        <input v-model="store.filters.ownerId" type="number" placeholder="User ID" />
        <input v-model="store.filters.categoryId" type="number" placeholder="Category ID" />
        <input v-model="store.filters.min" type="number" step="0.01" placeholder="Min amount" />
        <input v-model="store.filters.max" type="number" step="0.01" placeholder="Max amount" />
        <input v-model="store.filters.fromDate" type="date" />
        <input v-model="store.filters.toDate" type="date" />
        <select v-model="store.filters.sort">
          <option value="occurredAt,DESC">Date ↓</option>
          <option value="occurredAt,ASC">Date ↑</option>
          <option value="amount,DESC">Amount ↓</option>
          <option value="amount,ASC">Amount ↑</option>
          <option value="id,ASC">ID ↑</option>
          <option value="id,DESC">ID ↓</option>
        </select>
        <button class="btn btn--primary" type="submit">Filter</button>
        <button class="btn btn--outline" type="button" @click="reset">Reset</button>
      </form>
  
      <div v-if="store.loading">Loading...</div>
      <div v-if="store.error" class="muted">{{ store.error }}</div>
  
      <table class="wf__table" v-if="store.items.length">
        <thead>
          <tr>
            <th>ID</th>
            <th>User ID</th>
            <th>Wallet</th>
            <th>Category</th>
            <th>Type</th>
            <th>Amount</th>
            <th>Description</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="t in store.items" :key="t.id">
            <td>{{ t.id }}</td>
            <td>{{ t.ownerId || '-' }}</td>
            <td>{{ t.walletId }}</td>
            <td>{{ t.categoryName }}</td>
            <td>{{ t.categoryType }}</td>
            <td>{{ t.amount }}</td>
            <td>{{ t.description }}</td>
            <td>{{ new Date(t.occurredAt).toLocaleString() }}</td>
          </tr>
        </tbody>
      </table>
  
      <div v-if="store.total > store.filters.size" class="pagination">
        <button :disabled="store.filters.page === 0" @click="prevPage">Prev</button>
        <span>Page {{ store.filters.page + 1 }}</span>
        <button :disabled="(store.filters.page + 1) * store.filters.size >= store.total" @click="nextPage">Next</button>
      </div>
    </div>
  </template>
  
  <script setup>
  import { onMounted } from "vue";
  import { useAdminTransactionsStore } from "@/stores/adminTransactionsStore";
  const store = useAdminTransactionsStore();
  
  function onFilter() {
    store.filters.page = 0;
    store.fetchAll();
  }
  
  function reset() {
    store.resetFilters();
    store.fetchAll();
  }
  
  function prevPage() {
    if (store.filters.page > 0) {
      store.filters.page--;
      store.fetchAll();
    }
  }
  
  function nextPage() {
    if ((store.filters.page + 1) * store.filters.size < store.total) {
      store.filters.page++;
      store.fetchAll();
    }
  }
  
  onMounted(() => {
    store.fetchAll();
  });
  </script>
  
  <style scoped>
  .filters {
    display: flex;
    gap: 10px;
    margin-bottom: 18px;
    flex-wrap: wrap;
  }
  .muted { color: #789; margin-top: 12px;}
  .wf__table { width:100%; border-collapse:collapse; }
  .wf__table th, .wf__table td { padding: 8px 12px; border-bottom: 1px solid #e5e7eb; }
  .pagination { margin-top: 18px;}
  </style>
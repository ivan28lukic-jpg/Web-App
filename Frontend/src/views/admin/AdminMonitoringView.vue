<template>
    <div class="container page">
      <h2 class="admin-headline">Admin: Transactions</h2>
  
      <!-- Filter forma sa modernim izgledom -->
      <form class="filters" @submit.prevent="onFilter">
        <input v-model="store.filters.ownerUsername" placeholder="Username" />
        <input v-model="store.filters.categoryName" placeholder="Category name" />
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
  
      <div v-if="store.loading" class="loading">Loading...</div>
      <div v-if="store.error" class="muted">{{ store.error }}</div>
  
      <div class="admin-table-wrapper">
        <table class="wf__table" v-if="store.items.length">
          <thead>
            <tr>
              <th>ID</th>
              <th>Wallet name</th>
              <th>Owner username</th>
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
              <td>{{ t.walletName || "-" }}</td>
              <td>{{ t.ownerUsername || "-" }}</td>
              <td>{{ t.categoryName || "-" }}</td>
              <td :class="t.categoryType === 'EXPENSE' ? 'type-expense' : 'type-income'">
                {{ t.categoryType || "-" }}
              </td>
              <td>{{ t.amount }}</td>
              <td>{{ t.description }}</td>
              <td>{{ new Date(t.occurredAt).toLocaleString() }}</td>
            </tr>
          </tbody>
        </table>
      </div>
  
      <div v-if="store.total > store.filters.size" class="pagination">
        <button :disabled="store.filters.page === 0" @click="prevPage">Prev</button>
        <span>Page {{ store.filters.page + 1 }}</span>
        <button :disabled="(store.filters.page + 1) * store.filters.size >= store.total" @click="nextPage">Next</button>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from "vue";
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
  .container.page {
    background: #0d2820;
    min-height: 100vh;
    padding: 2.5rem 1rem;
    color: #f7fff9;
    font-family: 'Segoe UI', Arial, sans-serif;
  }
  .admin-headline {
    font-size: 2.1rem;
    font-weight: bold;
    color: #16ff97;
    margin-bottom: 2.2rem;
    letter-spacing: 1px;
  }
  
  .filters {
    display: flex;
    flex-wrap: wrap;
    gap: 1.1rem;
    margin-bottom: 2.2rem;
    align-items: center;
  }
  
  .filters input,
  .filters select {
    padding: 0.7rem 1.1rem;
    border-radius: 11px;
    border: none;
    background: #17392e;
    color: #f7fff9;
    font-size: 1.09rem;
    margin-right: 0.2em;
    box-shadow: 0 2px 9px #164d3b33;
    transition: background 0.15s;
    outline: none;
  }
  
  .filters input:focus,
  .filters select:focus {
    background: #254e3d;
  }
  
  .btn {
    padding: 0.7rem 1.8rem;
    border-radius: 13px;
    border: none;
    font-size: 1.09rem;
    font-weight: 600;
    cursor: pointer;
    margin-left: 0.2em;
    transition: background 0.14s, color 0.14s;
  }
  
  .btn--primary {
    background: linear-gradient(90deg, #13ff97 60%, #13d96b 100%);
    color: #04321c;
    box-shadow: 0 2px 14px #13ff9757;
  }
  .btn--primary:hover {
    background: linear-gradient(90deg, #13d96b 60%, #13ff97 100%);
    color: #fff;
  }
  
  .btn--outline {
    background: #194e3a;
    color: #fff;
    box-shadow: 0 2px 8px #13ff9722;
  }
  .btn--outline:hover {
    background: #c84b31;
    color: #fff;
  }
  
  .loading {
    font-style: italic;
    color: #b5ffea;
    margin-bottom: 1.5rem;
    font-size: 1.3rem;
  }
  
  .muted {
    color: #ffbaba;
    background: #800000;
    padding: 1rem 2rem;
    border-radius: 8px;
    font-weight: 600;
    margin-bottom: 1.5rem;
  }
  
  .admin-table-wrapper {
    background: rgba(255,255,255,0.03);
    box-shadow: 0 2px 10px 0 rgba(0,0,0,0.07);
    border-radius: 14px;
    padding: 1.5rem 1rem;
    overflow-x: auto;
    margin-bottom: 2.2rem;
  }
  
  .wf__table {
    width: 100%;
    border-collapse: collapse;
    font-size: 1.08rem;
    min-width: 950px;
    background: none;
    border-radius: 14px;
    overflow: hidden;
    box-shadow: 0 2px 10px 0 rgba(0,0,0,0.07);
  }
  
  .wf__table th, .wf__table td {
    padding: 0.9rem 1.1rem;
    text-align: left;
    border-bottom: 1px solid #164d3b;
  }
  
  .wf__table th {
    background: #183c2b;
    color: #16ff97;
    font-weight: 600;
    letter-spacing: 0.8px;
    font-size: 1.05rem;
    border-bottom: 2px solid #1f5740;
  }
  
  .wf__table tbody tr:nth-child(even) {
    background: rgba(255,255,255,0.012);
  }
  
  .wf__table tbody tr:hover {
    background: rgba(20,80,60,0.24);
    transition: background 0.16s;
  }
  
  .wf__table td {
    color: #f7fff9;
  }
  
  .wf__table td:nth-child(5) {
    font-weight: bold;
    letter-spacing: 1px;
  }
  .wf__table td.type-expense {
    color: #c84b31;
  }
  .wf__table td.type-income {
    color: #16ff97;
  }
  
  .pagination {
    display: flex;
    align-items: center;
    gap: 1.1rem;
    margin: 2.1rem 0 1.2rem 0;
  }
  
  .pagination button {
    padding: 0.6rem 1.2rem;
    border-radius: 11px;
    border: none;
    font-size: 1.07rem;
    font-weight: 600;
    background: #16ff97;
    color: #133b25;
    cursor: pointer;
    box-shadow: 0 1px 7px #16ff9757;
    transition: background 0.13s, color 0.13s;
  }
  .pagination button:disabled {
    background: #194e3a;
    color: #fff;
    opacity: 0.6;
    cursor: default;
  }
  .pagination span {
    font-size: 1.09rem;
    color: #16ff97;
    font-weight: 700;
  }
  
  /* Responsive */
  @media (max-width: 950px) {
    .wf__table {
      font-size: 0.96rem;
      min-width: 650px;
    }
    .filters {
      flex-direction: column;
      gap: 0.7rem;
    }
    .wf__table th, .wf__table td {
      padding: 0.6rem 0.5rem;
    }
    .container.page {
      padding: 1.2rem 0.3rem;
    }
  }
  </style>
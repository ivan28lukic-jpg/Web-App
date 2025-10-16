<template>
    <div class="container page">
      <div v-if="dashboard.error" class="error">{{ dashboard.error }}</div>
      <div class="card" v-if="!dashboard.loading">
        <h2>Admin Dashboard</h2>
        <div class="stats-grid">
          <div class="stat-box">
            <h3>Ukupno korisnika</h3>
            <div>{{ dashboard.totalUsers }}</div>
          </div>
          <div class="stat-box">
            <h3>Aktivnih korisnika (30 dana)</h3>
            <div>{{ dashboard.activeUsersLast30d }}</div>
          </div>
          <div class="stat-box">
            <h3>Ukupna količina novca</h3>
            <div>{{ fmt(dashboard.totalBalanceAll) }}</div>
          </div>
          <div class="stat-box">
            <h3>Prosek kod aktivnih</h3>
            <div>{{ fmt(dashboard.avgBalanceActive) }}</div>
          </div>
        </div>
  
        <h3 style="margin-top:32px;">Top 10 transakcija (poslednjih 30 dana)</h3>
        <table class="wf__table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Korisnik</th>
              <th>Novčanik</th>
              <th>Kategorija</th>
              <th>Iznos</th>
              <th>Opis</th>
              <th>Datum</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in dashboard.top30d" :key="t.id">
              <td>{{ t.id }}</td>
              <td>{{ t.ownerId }}</td>
              <td>{{ t.walletId }}</td>
              <td>{{ t.categoryName }}</td>
              <td>{{ fmt(t.amount) }}</td>
              <td>{{ t.description }}</td>
              <td>{{ formatDate(t.occurredAt) }}</td>
            </tr>
          </tbody>
        </table>
  
        <h3 style="margin-top:32px;">Top 10 transakcija (poslednja 2 minuta)</h3>
        <table class="wf__table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Korisnik</th>
              <th>Novčanik</th>
              <th>Kategorija</th>
              <th>Iznos</th>
              <th>Opis</th>
              <th>Datum</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in dashboard.top2m" :key="t.id">
              <td>{{ t.id }}</td>
              <td>{{ t.ownerId }}</td>
              <td>{{ t.walletId }}</td>
              <td>{{ t.categoryName }}</td>
              <td>{{ fmt(t.amount) }}</td>
              <td>{{ t.description }}</td>
              <td>{{ formatDate(t.occurredAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else>Učitavanje...</div>
    </div>
  </template>
  
  <script setup>
  import { onMounted } from "vue"
  import { useAdminDashboardStore } from "@/stores/adminDashboard"
  const dashboard = useAdminDashboardStore()
  function fmt(n) {
    if (n === null || n === undefined) return "—"
    return Number(n).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  }
  function formatDate(d) {
    if (!d) return "—"
    return new Date(d).toLocaleString()
  }
  onMounted(() => dashboard.fetchDashboard())
  </script>
  
  <style scoped>
  .stats-grid {
    display: flex;
    gap: 24px;
    flex-wrap: wrap;
    margin-bottom: 32px;
  }
  .stat-box {
    background: #23272f;
    padding: 18px 24px;
    border-radius: 12px;
    min-width: 180px;
    box-shadow: 0 2px 8px #0002;
    text-align: center;
  }
  .wf__table {
    width: 100%;
    margin-bottom: 24px;
    border-collapse: collapse;
  }
  .wf__table th, .wf__table td {
    padding: 8px 12px;
    border-bottom: 1px solid #333;
  }
  .error {
    color: red;
    margin: 16px 0;
  }
  </style>
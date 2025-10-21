<template>
  <div class="dashboard">
    <h1>Admin Dashboard</h1>
    <div v-if="error" class="error">{{ error }}</div>
    <div v-else-if="loading" class="loading">Učitavanje podataka...</div>
    <div v-else>
      <div class="stats-bar">
        <div class="stat-card">
          <div class="stat-label">Ukupan broj korisnika</div>
          <div class="stat-value">{{ dashboard.totalUsers }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Aktivni korisnici (30 dana)</div>
          <div class="stat-value">{{ dashboard.activeUsersLast30d }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Ukupno sredstava u walletima</div>
          <div class="stat-value">{{ dashboard.totalBalanceAll }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Prosečan balans aktivnih korisnika</div>
          <div class="stat-value">{{ dashboard.avgBalanceActive }}</div>
        </div>
      </div>

      <div class="table-wrapper">
        <h2>Top 10 transakcija (poslednjih 30 dana)</h2>
        <table>
          <thead>
            <tr>
              <th>Vlasnik</th>
              <th>Wallet</th> <!-- Dodato -->
              <th>Naziv kategorije</th>
              <th>Tip</th>
              <th>Iznos</th>
              <th>Opis</th>
              <th>Datum</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tx in dashboard.topTransactionsLast30d" :key="tx.id">
              <td>{{ tx.ownerUsername }}</td>
              <td>{{ tx.walletName }}</td> <!-- Dodato -->
              <td>{{ tx.categoryName }}</td>
              <td>
                <span class="pill" :class="tx.categoryType === 'INCOME' ? 'income' : 'expense'">
                  {{ tx.categoryType }}
                </span>
              </td>
              <td><span class="pill amount">{{ tx.amount }}</span></td>
              <td>{{ tx.description }}</td>
              <td>{{ formatDate(tx.occurredAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="table-wrapper">
        <h2>Top 10 transakcija (poslednje 2 minute)</h2>
        <table>
          <thead>
            <tr>
              <th>Vlasnik</th>
              <th>Wallet</th> <!-- Dodato -->
              <th>Naziv kategorije</th>
              <th>Tip</th>
              <th>Iznos</th>
              <th>Opis</th>
              <th>Datum</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tx in dashboard.topTransactionsLast2m" :key="tx.id">
              <td>{{ tx.ownerUsername }}</td>
              <td>{{ tx.walletName }}</td> <!-- Dodato -->
              <td>{{ tx.categoryName }}</td>
              <td>
                <span class="pill" :class="tx.categoryType === 'INCOME' ? 'income' : 'expense'">
                  {{ tx.categoryType }}
                </span>
              </td>
              <td><span class="pill amount">{{ tx.amount }}</span></td>
              <td>{{ tx.description }}</td>
              <td>{{ formatDate(tx.occurredAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import api from "@/services/api";

export default {
  name: 'Dashboard',
  data() {
    return {
      dashboard: {
        totalUsers: 0,
        activeUsersLast30d: 0,
        totalBalanceAll: 0,
        avgBalanceActive: 0,
        topTransactionsLast30d: [],
        topTransactionsLast2m: []
      },
      loading: true,
      error: null
    };
  },
  created() {
    api.get("/admin/dashboard")
      .then(response => {
        const data = response.data;
        this.dashboard = {
          totalUsers: data.totalUsers,
          activeUsersLast30d: data.activeUsersLast30d,
          totalBalanceAll: data.totalBalanceAllWallets,
          avgBalanceActive: data.avgBalanceActiveUsers,
          topTransactionsLast30d: data.top10Last30d,
          topTransactionsLast2m: data.top10Last2m
        };
        this.loading = false;
      })
      .catch(error => {
        if (error.response && error.response.data && error.response.data.error) {
          this.error = error.response.data.error + " (" + error.response.data.status + ")";
        } else {
          this.error = 'Greška prilikom učitavanja dashboarda: ' + error.message;
        }
        this.loading = false;
      });
  },
  methods: {
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString();
    }
  }
};
</script>

<style scoped>
.dashboard {
  background: #0d2820;
  min-height: 100vh;
  padding: 2.5rem 1rem;
  color: #f7fff9;
  font-family: 'Segoe UI', Arial, sans-serif;
}
h1 {
  font-size: 2.8rem;
  margin-bottom: 2.5rem;
  font-weight: bold;
  letter-spacing: 2px;
}
.stats-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  margin-bottom: 2.5rem;
  justify-content: flex-start;
}
.stat-card {
  background: rgba(255,255,255,0.13);
  color: #fff;
  padding: 1.3rem 2.2rem;
  border-radius: 16px;
  box-shadow: 0 2px 18px 0 rgba(0,0,0,0.10);
  display: flex;
  flex-direction: column;
  font-size: 1.25rem;
  min-width: 250px;
  min-height: 110px;
  margin-bottom: 0.5rem;
  align-items: flex-start;
  justify-content: center;
}
.stat-label {
  font-weight: 500;
  opacity: 0.97;
  font-size: 1.05rem;
  margin-bottom: 0.7rem;
  letter-spacing: 1.1px;
}
.stat-value {
  margin-top: 0.1rem;
  font-size: 2.1rem;
  font-weight: bold;
  letter-spacing: 1px;
  color: #16ff97;
  text-shadow: 0 2px 9px #164d3b33;
}
.table-wrapper {
  overflow-x: auto;
  margin-bottom: 2.2rem;
  border-radius: 12px;
  box-shadow: 0 2px 10px 0 rgba(0,0,0,0.07);
  background: rgba(255,255,255,0.03);
  padding: 1.5rem 1rem;
}
h2 {
  font-size: 1.35rem;
  font-weight: 700;
  margin-bottom: 1.1rem;
  letter-spacing: 1px;
  color: #16ff97;
}
table {
  width: 100%;
  border-collapse: collapse;
  font-size: 1.08rem;
  min-width: 950px;
}
th, td {
  padding: 0.85rem 1rem;
  text-align: left;
}
th {
  background: #183c2b;
  color: #fff;
  font-weight: 600;
  border-bottom: 2px solid #1f5740;
}
tbody tr:nth-child(even) {
  background: rgba(255,255,255,0.012);
}
tbody tr:hover {
  background: rgba(20,80,60,0.22);
  transition: background 0.18s;
}
td {
  border-bottom: 1px solid #1a3930;
}
.pill {
  display: inline-block;
  padding: 0.25em 1.15em;
  border-radius: 14px;
  font-size: 0.99em;
  font-weight: 500;
  letter-spacing: 1px;
  color: #fff;
  background: #444;
  margin-right: 0.08em;
  margin-bottom: 2px;
}
.pill.income {
  background: #1f8c5a;
}
.pill.expense {
  background: #c84b31;
}
.pill.amount {
  background: #16ff97;
  color: #133b25;
  font-weight: 700;
  font-size: 1.06em;
  letter-spacing: 1.2px;
  box-shadow: 0 0.5px 8px #16ff9757;
}
.error {
  color: #ffbaba;
  background: #800000;
  padding: 1rem 2rem;
  border-radius: 8px;
  font-weight: 600;
  margin-bottom: 1.5rem;
}
.loading {
  font-style: italic;
  color: #b5ffea;
  margin-bottom: 1.5rem;
  font-size: 1.3rem;
}
@media (max-width: 1100px) {
  .stats-bar {
    flex-direction: column;
    gap: 1rem;
  }
  .stat-card {
    min-width: 90vw;
    align-items: flex-start;
  }
  table {
    font-size: 0.98rem;
    min-width: 650px;
  }
}
@media (max-width: 700px) {
  .table-wrapper {
    padding: 0.5rem 0.3rem;
  }
  th, td {
    padding: 0.45rem 0.55rem;
  }
}
</style>
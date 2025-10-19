<template>
  <div class="dashboard">
    <h1>Admin Dashboard</h1>
    <div v-if="error" class="error">{{ error }}</div>
    <div v-else-if="loading" class="loading">Učitavanje podataka...</div>
    <div v-else>
      <div class="stats">
        <div><strong>Ukupan broj korisnika:</strong> {{ dashboard.totalUsers }}</div>
        <div><strong>Aktivni korisnici (30 dana):</strong> {{ dashboard.activeUsersLast30d }}</div>
        <div><strong>Ukupno sredstava u walletima:</strong> {{ dashboard.totalBalanceAll }}</div>
        <div><strong>Prosečan balans aktivnih korisnika:</strong> {{ dashboard.avgBalanceActive }}</div>
      </div>
      <h2>Top 10 transakcija (poslednjih 30 dana)</h2>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Vlasnik</th>
            <th>Wallet</th>
            <th>Kategorija ID</th>
            <th>Naziv kategorije</th>
            <th>Tip</th>
            <th>Iznos</th>
            <th>Opis</th>
            <th>Datum</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tx in dashboard.topTransactionsLast30d" :key="tx.id">
            <td>{{ tx.id }}</td>
            <td>{{ tx.ownerId }}</td>
            <td>{{ tx.walletId }}</td>
            <td>{{ tx.categoryId }}</td>
            <td>{{ tx.categoryName }}</td>
            <td>{{ tx.categoryType }}</td>
            <td>{{ tx.amount }}</td>
            <td>{{ tx.description }}</td>
            <td>{{ formatDate(tx.occurredAt) }}</td>
          </tr>
        </tbody>
      </table>
      <h2>Top 10 transakcija (poslednje 2 minute)</h2>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Vlasnik</th>
            <th>Wallet</th>
            <th>Kategorija ID</th>
            <th>Naziv kategorije</th>
            <th>Tip</th>
            <th>Iznos</th>
            <th>Opis</th>
            <th>Datum</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tx in dashboard.topTransactionsLast2m" :key="tx.id">
            <td>{{ tx.id }}</td>
            <td>{{ tx.ownerId }}</td>
            <td>{{ tx.walletId }}</td>
            <td>{{ tx.categoryId }}</td>
            <td>{{ tx.categoryName }}</td>
            <td>{{ tx.categoryType }}</td>
            <td>{{ tx.amount }}</td>
            <td>{{ tx.description }}</td>
            <td>{{ formatDate(tx.occurredAt) }}</td>
          </tr>
        </tbody>
      </table>
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
          totalBalanceAll: data.totalBalanceAllWallets,      // mapiranje!
          avgBalanceActive: data.avgBalanceActiveUsers,      // mapiranje!
          topTransactionsLast30d: data.top10Last30d,         // mapiranje!
          topTransactionsLast2m: data.top10Last2m            // mapiranje!
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
.stats {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
  margin-bottom: 2.5rem;
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
  min-width: 290px;
  margin-bottom: 0.5rem;
}
.stat-label {
  font-weight: 500;
  opacity: 0.97;
  font-size: 1.05rem;
}
.stat-value {
  margin-top: 0.5rem;
  font-size: 1.8rem;
  font-weight: bold;
  letter-spacing: 1px;
}
.table-wrapper {
  overflow-x: auto;
  margin-bottom: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 10px 0 rgba(0,0,0,0.07);
  background: rgba(255,255,255,0.03);
}
table {
  width: 100%;
  border-collapse: collapse;
  font-size: 1.08rem;
  min-width: 1000px;
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
  background: rgba(255,255,255,0.025);
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
  padding: 0.23em 1em;
  border-radius: 12px;
  font-size: 0.98em;
  font-weight: 500;
  letter-spacing: 1px;
  color: #fff;
  background: #444;
}
.pill.income {
  background: #1f8c5a;
}
.pill.expense {
  background: #c84b31;
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
@media (max-width: 900px) {
  .stats {
    flex-direction: column;
    gap: 1rem;
  }
  .stat-card {
    min-width: 90vw;
  }
  table {
    font-size: 0.98rem;
    min-width: 650px;
  }
}
</style>
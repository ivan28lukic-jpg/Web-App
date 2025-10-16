<template>
    <div class="dashboard">
      <h1>Admin Dashboard</h1>
  
      <!-- 🧮 Statistika -->
      <div v-if="error" class="error">{{ error }}</div>
      <div v-else-if="loading" class="loading">Učitavanje podataka...</div>
      <div v-else>
        <div class="stats">
          <div><strong>Ukupan broj korisnika:</strong> {{ dashboard.totalUsers }}</div>
          <div><strong>Aktivni korisnici (30 dana):</strong> {{ dashboard.activeUsersLast30d }}</div>
          <div><strong>Ukupno sredstava u walletima:</strong> {{ dashboard.totalBalanceAll }}</div>
          <div><strong>Prosečan balans aktivnih korisnika:</strong> {{ dashboard.avgBalanceActive }}</div>
        </div>
  
        <!-- 📊 Top transakcije - poslednjih 30 dana -->
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
  
        <!-- ⏳ Top transakcije - poslednje 2 min -->
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
  import axios from 'axios';
  
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
      axios
        .get('http://localhost:8080/api/admin/dashboard')  // 👈 Ako koristiš proxy, možeš skratiti na '/api/admin/dashboard'
        .then(response => {
          this.dashboard = response.data;
          this.loading = false;
        })
        .catch(error => {
          this.error = 'Greška prilikom učitavanja dashboarda: ' + error.message;
          this.loading = false;
        });
    },
    methods: {
      formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleString();
      }
    }
  };
  </script>
  
  <style scoped>
  .dashboard {
    max-width: 1000px;
    margin: auto;
    padding: 2rem;
    font-family: Arial, sans-serif;
  }
  
  .stats {
    display: flex;
    flex-wrap: wrap;
    gap: 2rem;
    margin-bottom: 2rem;
  }
  
  .stats > div {
    min-width: 220px;
    background: #f9f9f9;
    padding: 1rem;
    border-radius: 8px;
    box-shadow: 0 0 4px rgba(0,0,0,0.1);
  }
  
  table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 2rem;
  }
  
  th, td {
    border: 1px solid #ddd;
    padding: 0.6rem;
    text-align: left;
  }
  
  th {
    background: #f1f1f1;
  }
  
  .error {
    color: red;
    font-weight: bold;
    margin-bottom: 1rem;
  }
  
  .loading {
    font-style: italic;
    color: #555;
  }
  </style>
  
<template>
    <div class="stats-period">
      <form class="stats-filter" @submit.prevent="reload">
        <select v-model="periodType">
          <option value="daily">Daily</option>
          <option value="weekly">Weekly</option>
          <option value="monthly">Monthly</option>
          <option value="yearly">Yearly</option>
        </select>
        <input type="date" v-model="from" />
        <input type="date" v-model="to" />
        <button type="submit">Show</button>
      </form>
      <div class="chart-wrap">
        <canvas ref="chartEl"></canvas>
      </div>
      <div v-if="loading" class="loading">Učitavanje...</div>
      <div v-if="error" class="error">{{ error }}</div>
    </div>
</template>

<script setup>
import { ref, watch, onMounted } from "vue";
import { getStatsByPeriod } from "@/services/statsService";
import Chart from "chart.js/auto";

const props = defineProps({
  ownerId: { type: Number, required: false },
  isAdmin: { type: Boolean, default: false }
});

const periodType = ref("monthly");
const from = ref(new Date(new Date().getFullYear(), 0, 1).toISOString().slice(0, 10)); // 1. januar tekuće godine
const to = ref(new Date().toISOString().slice(0, 10)); // danas

const loading = ref(false);
const error = ref("");
const stats = ref([]);
const chart = ref(null);
const chartEl = ref();

async function reload() {
    console.log("isAdmin:", props.isAdmin, "ownerId:", props.ownerId);
  loading.value = true;
  error.value = "";
  try {
    const params = {
      type: periodType.value,
      from: from.value,
      to: to.value,
    };
    // ownerId šalji samo ako NIJE admin
    if (!props.isAdmin && props.ownerId) {
      params.ownerId = props.ownerId;
    }
    const { data } = await getStatsByPeriod(params);
    stats.value = data;
    renderChart();
  } catch (e) {
    error.value = e?.response?.data?.message || e.message || "Greška pri učitavanju statistike.";
  } finally {
    loading.value = false;
  }
}

function renderChart() {
  if (!chartEl.value) return;
  if (chart.value) chart.value.destroy();

  const labels = stats.value.map((row) => row.period);
  const income = stats.value.map((row) => row.income);
  const expense = stats.value.map((row) => row.expense);

  chart.value = new Chart(chartEl.value, {
    type: "bar",
    data: {
      labels,
      datasets: [
        {
          label: "Income",
          backgroundColor: "#20c589",
          data: income,
        },
        {
          label: "Expense",
          backgroundColor: "#ff7d75",
          data: expense,
        },
      ],
    },
    options: {
      responsive: true,
      plugins: {
        legend: { position: "top" },
      },
      scales: {
        y: { beginAtZero: true },
      },
    },
  });
}

onMounted(reload);
watch([periodType, from, to], reload);
</script>

<style scoped>
.stats-period {
  padding: 2rem;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 1rem;
  max-width: 900px;
  margin: auto;
}
.stats-filter {
  display: flex;
  gap: 1rem;
  align-items: center;
  margin-bottom: 2rem;
}
.stats-filter select,
.stats-filter input[type="date"] {
  padding: 0.5rem;
  border-radius: 0.5rem;
  border: 1px solid #ddd;
}
.chart-wrap {
  background: rgba(255, 255, 255, 1);
  padding: 1rem;
  border-radius: 1rem;
}
.loading {
  color: #888;
  padding: 1rem;
}
.error {
  color: #ff574d;
  font-weight: bold;
  padding: 1rem;
}

option{
  background: rgba(255, 255, 255, 1);
}

button{
  padding: 0.5rem 1rem;
  border-radius: 2rem;
  background-color: var(--brand);
  transition: 0.3s ease;
  height: 100%;
  cursor: pointer;
}

button:hover{
  background-color: var(--brand-2);
  transform: translateY(-5px);
}

</style>
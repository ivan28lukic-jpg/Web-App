<template>
    <div class="stats-period">
      <form class="stats-filter" @submit.prevent="reload">
        <select v-model="periodType">
          <option value="daily">Dnevni</option>
          <option value="weekly">Nedeljni</option>
          <option value="monthly">Mesečni</option>
          <option value="yearly">Godišnji</option>
        </select>
        <input type="date" v-model="from" />
        <input type="date" v-model="to" />
        <button type="submit">Prikaži</button>
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
  
  // Pretpostavljeni props - ownerId dobijaš iz auth/user store-a
  const props = defineProps({
    ownerId: { type: Number, required: true },
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
    loading.value = true;
    error.value = "";
    try {
      const { data } = await getStatsByPeriod({
        type: periodType.value,
        ownerId: props.ownerId,
        from: from.value,
        to: to.value,
      });
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
            label: "Prihodi",
            backgroundColor: "#20c589",
            data: income,
          },
          {
            label: "Troškovi",
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
  .stats-period { padding: 2rem; background: #fafbff; border-radius: 1rem; max-width: 900px; margin: auto; }
  .stats-filter { display: flex; gap: 1rem; align-items: center; margin-bottom: 2rem; }
  .stats-filter select, .stats-filter input[type="date"] { padding: 0.5rem; border-radius: 0.5rem; border: 1px solid #ddd; }
  .chart-wrap { background: #fff; padding: 1rem; border-radius: 1rem; }
  .loading { color: #888; padding: 1rem; }
  .error { color: #ff574d; font-weight: bold; padding: 1rem; }
  </style>
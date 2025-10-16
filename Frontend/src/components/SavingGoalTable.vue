<script setup>
import { ref, onMounted, watch } from "vue";

const props = defineProps({
  items: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
});
const emit = defineEmits(["edit", "delete", "progress", "contribute", "withdraw"]);

const pct = (g) =>
  g.targetAmount > 0
    ? Math.min(100, Math.round((g.currentAmount / g.targetAmount) * 100))
    : 0;

// animation refs
const animatedValues = ref({});
const animatedPct = (id) => animatedValues.value[id] || 0;

// when component mounts or items change → animate bars
const animateBars = () => {
  setTimeout(() => {
    props.items.forEach((g) => {
      animatedValues.value[g.id] = pct(g);
    });
  }, 200);
};

onMounted(animateBars);
watch(
  () => props.items,
  () => animateBars(),
  { deep: true }
);
</script>

<template>
  <div>
    <div v-if="loading" style="padding: 1rem;">Loading…</div>

    <table v-else class="data-table">
      <thead>
        <tr>
          <th>Name</th>
          <th>Progress</th>
          <th>Target</th>
          <th>Current</th>
          <th>Due</th>
          <th style="width: 300px;">Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="items.length === 0">
          <td colspan="6" class="data-table__empty">No results.</td>
        </tr>

        <tr v-for="g in items" :key="g.id">
          <td>{{ g.name }}</td>
          <td>
            <div class="bar">
              <div
                class="fill"
                :style="{ width: animatedPct(g.id) + '%' }"
              ></div>
            </div>
            <span>{{ animatedPct(g.id) }}%</span>
          </td>
          <td>{{ g.targetAmount }}</td>
          <td>{{ g.currentAmount }}</td>
          <td>{{ g.dueDate ? String(g.dueDate).slice(0, 10) : "—" }}</td>
          <td class="table-actions">
            <button class="btn-chip" @click="$emit('contribute', g)">Contribute</button>
            <button class="btn-chip" @click="$emit('withdraw', g)">Withdraw</button>
            <button class="btn-chip" @click="$emit('edit', g)">Edit</button>
            <button class="btn-chip btn-chip--danger" @click="$emit('delete', g)">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.bar {
  position: relative;
  width: 160px;
  height: 10px;
  border-radius: 999px;
  background: #0f1316;
  border: 1px solid #233038;
  overflow: hidden;
  display: inline-block;
  margin-right: 0.5rem;
}

.fill {
  height: 100%;
  background: #20c589;
  width: 0;
  border-radius: 999px;
  transition: width 1s ease-in-out; /* smooth animation */
}
</style>

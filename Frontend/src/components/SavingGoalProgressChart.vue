<script setup>
const props = defineProps({
  progress: { type: Object, required: true }, // ima: name, targetAmount, currentAmount, progressPercent
});
const pct = computed(() => {
  const t = Number(props.progress?.targetAmount ?? 0);
  const c = Number(props.progress?.currentAmount ?? 0);
  if (!(t > 0)) return 0;
  return Math.max(0, Math.min(100, Math.round((c / t) * 100)));
});
</script>

<template>
  <div class="wrap">
    <div class="title">
      <strong>{{ progress.name }}</strong>
      <span>Target: {{ progress.targetAmount }} | Current: {{ progress.currentAmount }} ({{ pct }}%)</span>
    </div>
    <div class="pbar">
      <div class="pfill" :style="{ width: pct + '%' }"></div>
    </div>
    <div class="ppct">{{ pct }}%</div>
  </div>
</template>

<style scoped>
:root{--panel:#141a1e;--fg:#e9efec;--line:#26323a}
@media (prefers-color-scheme: light){
  :root{--panel:#ffffff;--fg:#0b1114;--line:#e6eaee}
}
.wrap{display:grid;gap:.6rem;color:var(--fg)}
.title{display:flex;gap:1rem;flex-wrap:wrap;align-items:center}
.pbar{width:100%;height:20px;border-radius:999px;background:#0f1316;border:1px solid #233038;overflow:hidden}
.pfill{height:100%;background:#20c589}
.ppct{font-weight:800}
</style>

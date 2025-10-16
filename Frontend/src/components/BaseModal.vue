<script setup>
import { onMounted, onBeforeUnmount } from "vue";
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: "" },
  width: { type: String, default: "680px" },
  closeOnOverlay: { type: Boolean, default: true },
  closeOnEsc: { type: Boolean, default: true },
});
const emit = defineEmits(["update:modelValue", "close"]);
function close(){ emit("update:modelValue", false); emit("close"); }
function onOverlay(e){ if (props.closeOnOverlay && e.target === e.currentTarget) close(); }
function onKey(e){ if (props.closeOnEsc && e.key === "Escape") close(); }
onMounted(()=>window.addEventListener("keydown", onKey));
onBeforeUnmount(()=>window.removeEventListener("keydown", onKey));
</script>

<template>
  <div v-if="modelValue" class="overlay" @click="onOverlay">
    <div class="card" :style="{ width }" role="dialog" aria-modal="true">
      <header class="head">
        <h2 v-if="title">{{ title }}</h2>
        <button class="x" @click="close" aria-label="Close">✕</button>
      </header>
      <div class="body"><slot /></div>
      <footer v-if="$slots.footer" class="foot"><slot name="footer" /></footer>
    </div>
  </div>
</template>

<style scoped>
.overlay{position:fixed;inset:0;z-index:50;display:grid;place-items:center;background:rgba(0,0,0,.5)}
.card{
  max-width:95vw;background:var(--panel);color:var(--fg);
  border:1px solid var(--line);border-radius:1rem;box-shadow:0 20px 60px rgba(0,0,0,.45)
}
.head{display:flex;justify-content:space-between;align-items:center;padding:1rem 1.1rem;border-bottom:1px solid var(--line)}
.body{padding:1.1rem}
.foot{padding:.8rem 1.1rem;border-top:1px solid var(--line)}
.x{background:transparent;border:none;font-size:1.2rem;color:var(--fg);cursor:pointer}
:root{--panel:#0f1316;--fg:#e9efec;--line:#1f2a2f}
@media (prefers-color-scheme: light){
  :root{--panel:#ffffff;--fg:#0b1114;--line:#e6eaee}
}
</style>

<script setup>
import { reactive, watch, computed, ref, onMounted } from "vue";
import { getSavingsWallets } from "@/services/serviceWallets";

const props = defineProps({
  initial: { type: Object, default: null }, // null => create
  ownerId: { type: Number, required: true },
});
const emit = defineEmits(["submit","cancel"]);

const s = reactive({ name:"", targetAmount:"", dueDate:"", walletId:"", archived:false });
const isEdit = computed(()=> !!props.initial?.id);

const wallets = ref([]); const loading = ref(false); const wErr = ref(null);
onMounted(async () => {
  if (isEdit.value) return; // pri izmeni ne menjamo wallet
  try { loading.value = true; wallets.value = await getSavingsWallets(props.ownerId); }
  catch(e){ wErr.value = e?.response?.data?.message || e.message; }
  finally{ loading.value = false; }
});

function fill(){
  const i = props.initial;
  s.name = i?.name ?? "";
  s.targetAmount = i?.targetAmount != null ? String(i.targetAmount) : "";
  s.dueDate = i?.dueDate ? String(i.dueDate).slice(0,10) : "";
  s.walletId = "";
  s.archived = i?.archived ?? false;
}
fill(); watch(()=>props.initial, fill, { deep:true });

function submit(){
  if (isEdit.value){
    emit("submit", {
      name: s.name.trim(),
      targetAmount: s.targetAmount === "" ? null : s.targetAmount,
      dueDate: s.dueDate || null,
      archived: s.archived,
    });
  } else {
    emit("submit", {
      ownerId: props.ownerId,
      walletId: Number(s.walletId),
      name: s.name.trim(),
      targetAmount: s.targetAmount === "" ? null : s.targetAmount,
      dueDate: s.dueDate || null,
    });
  }
}
</script>

<template>
  <form class="f" @submit.prevent="submit">
    <div class="row two" v-if="!isEdit">
      <label> Savings wallet
        <select v-model="s.walletId" class="inp" required>
          <option value="" disabled>— choose savings wallet —</option>
          <option v-if="wErr" disabled>{{ wErr }}</option>
          <option v-else-if="loading" disabled>Loading…</option>
          <option v-for="w in wallets" :key="w.id" :value="w.id">
            {{ w.name }} ({{ w.currencyCode || w.currency || '' }})
          </option>
        </select>
      </label>
      <div />
    </div>

    <div class="row">
      <label> Name
        <input v-model.trim="s.name" type="text" required class="inp"/>
      </label>
    </div>

    <div class="row two">
      <label> Target amount
        <input v-model="s.targetAmount" type="number" step="0.01" min="0" required class="inp"/>
      </label>
      <label> Due date
        <input v-model="s.dueDate" type="date" class="inp"/>
      </label>
    </div>

    <div class="row" v-if="isEdit">
      <label class="chk"><input type="checkbox" v-model="s.archived" /> Archive goal</label>
    </div>

    <div class="actions">
      <button type="button" class="btn" @click="$emit('cancel')">Cancel</button>
      <button type="submit" class="btn primary">Save</button>
    </div>
  </form>
</template>

<style scoped>
.f{display:grid;gap:1rem;color:var(--fg)}
.row{display:grid;gap:.4rem}
.two{grid-template-columns:1fr 1fr}
label{display:grid;gap:.35rem;font-weight:600}
.inp{padding:.65rem .8rem;border:1px solid var(--line2);border-radius:.7rem;background:var(--panel2);color:var(--fg)}
.inp:focus{outline:none;border-color:#20c589;box-shadow:0 0 0 3px rgba(32,197,137,.18)}
.chk{display:flex;align-items:center;gap:.5rem}
.actions{display:flex;justify-content:flex-end;gap:.5rem}
.btn{padding:.6rem .9rem;border-radius:.8rem;border:1px solid #20c589;color:#20c589;background:transparent;font-weight:700}
.btn.primary{background:#20c589;color:#0b1114}
:root{--panel2:#11161a;--line2:#25323a;--fg:#e9efec}
@media (prefers-color-scheme: light){
  :root{--panel2:#f7faf9;--line2:#e2e8f0;--fg:#0b1114}
}
</style>

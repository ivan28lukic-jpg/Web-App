<script setup>
import { ref, onMounted } from "vue";
import WalletTransferForm from "@/components/WalletTransferForm.vue";
import { useWalletsStore } from "@/stores/storeWallets";
import { listCurrencies } from "@/services/currenciesService";
import { transferFunds } from "@/services/transferService";

const walletsStore = useWalletsStore();
const currencies = ref([]);
const loading = ref(false);
const error = ref("");

onMounted(async () => {
  loading.value = true;
  await walletsStore.fetch(); // učitaj sve wallet-e
  try {
    const { data } = await listCurrencies();
    currencies.value = data;
  } catch (e) {
    error.value = e.message || "Ne mogu da učitam valute";
  } finally {
    loading.value = false;
  }
});

async function handleTransfer(payload) {
  try {
    await transferFunds(payload); // transfer na backend
    alert("Transfer uspešan!");
    await walletsStore.fetch(); // osveži wallet-e
  } catch (e) {
    alert("Greška pri transferu: " + (e.response?.data?.message || e.message));
  }
}

function closeTransferForm() {
  // Sakrij modal ili resetuj state po potrebi
}
</script>

<template>
  <section style="max-width: 700px; margin: 2rem auto;">
    <h2>Transfer između novčanika</h2>
    <div v-if="loading">Učitavam...</div>
    <div v-if="error" style="color:red;">{{ error }}</div>
    <WalletTransferForm
      v-if="!loading"
      :wallets="walletsStore.items"
      :currencies="currencies"
      @submit="handleTransfer"
      @cancel="closeTransferForm"
    />
  </section>
</template>
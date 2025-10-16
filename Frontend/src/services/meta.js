import api from "@/services/api";

export async function getCurrencies() {
  try {
    const { data } = await api.get("/currencies"); // prilagodi ako je drugačije
    // očekujemo listu stringova ili [{code:'RSD', name:'Serbian Dinar'}]
    if (Array.isArray(data) && data.length) {
      if (typeof data[0] === "string") return data;
      return data.map((c) => c.code || c.id || c.name).filter(Boolean);
    }
  } catch (_) {}
  // fallback ako backend još nema endpoint
  return ["RSD", "EUR", "USD"];
}

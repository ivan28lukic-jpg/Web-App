import axios from "axios";
import { useAuthStore } from "@/stores/auth";

const BASE_URL = "http://localhost:8080/api/transactions/stats";

export async function getStatsByPeriod({ type, ownerId, from, to }) {
  const authStore = useAuthStore();
  const token = authStore.token;

  let url = `${BASE_URL}/${type}`;
  const query = { ownerId, from, to };
  Object.keys(query).forEach((k) => (query[k] === undefined || query[k] === null) && delete query[k]);

  const headers = token ? { Authorization: `Bearer ${token}` } : {};

  const response = await axios.get(url, {
    params: query,
    headers,
  });
  return { data: response.data };
}
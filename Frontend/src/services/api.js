// src/services/api.js
import axios from "axios";

// Uzmemo bazu iz env-a ili padnemo na '/api' (Vite proxy ili nginx location)
const RAW_BASE = import.meta.env.VITE_API_BASE_URL || "/api";

// Skini sve završne kosе crte (da ne bude '/api/' pa da se duplira)
const BASE_URL = RAW_BASE.replace(/\/+$/, "");

const api = axios.create({
  baseURL: BASE_URL, // npr. '/api' ili 'http://localhost:8080/api' bez trailing slash-a
  withCredentials: false, // promeni po potrebi
});

// --- Request interceptor ---
// 1) Dodaj JWT ako postoji
// 2) NORMALIZUJ URL: skini vodeće "/" osim ako je apsolutni URL (http/https)
//    Ovo sprečava '/api' (base) + '/nesto' (ruta) => '/api/nesto' ✔ i
//    '/api' (base) + '/api/nesto' (ruta) => '/api/api/nesto' ✖ (to izbegnemo)
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("jwt");
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }

  if (typeof config.url === "string") {
    // Ako je apsolutan URL, ne diramo ga
    const isAbsolute = /^https?:\/\//i.test(config.url);
    if (!isAbsolute) {
      // Skini vodeće kosе crte da se ne lepi još jedan '/api'
      config.url = config.url.replace(/^\/+/, "");
    }
  }
  return config;
});

// Globalni 401 handler -> logout i redirect na /login
api.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err?.response?.status === 401) {
      localStorage.removeItem("jwt");
      localStorage.removeItem("role");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

export default api;
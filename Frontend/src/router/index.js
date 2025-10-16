import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/auth";

// Lazy-loaded views
const Landing = () => import("@/views/LandingView.vue");
const Login = () => import("@/views/LoginView.vue");
const Register = () => import("@/views/RegisterView.vue");
const Dashboard = () => import("@/views/DashboardView.vue");

const Wallets = () => import("@/views/WalletsView.vue");
const Transactions = () => import("@/views/TransactionsView.vue");
const Categories = () => import("@/views/CategoriesView.vue");
const Savings = () => import("@/views/SavingGoalsView.vue");
const Stats = () => import("@/views/StatsView.vue");
const Profile = () => import("@/views/ProfileView.vue");

const AdminDashboard = () => import("@/views/admin/AdminDashboardView.vue");
const AdminUsers = () => import("@/views/admin/AdminUsersView.vue");
const AdminCategories = () => import("@/views/admin/AdminCategoriesView.vue");
const AdminCurrencies = () => import("@/views/admin/AdminCurrenciesView.vue");
const AdminMonitoring = () => import("@/views/admin/AdminMonitoringView.vue");

const routes = [
  // Public
  { path: "/", name: "landing", component: Landing, meta: { public: true } },
  { path: "/login", name: "login", component: Login, meta: { public: true } },
  { path: "/register", name: "register", component: Register, meta: { public: true } },

  // User
  { path: "/dashboard", name: "dashboard", component: Dashboard },
  { path: "/wallets", name: "wallets", component: Wallets },
  { path: "/transactions", name: "transactions", component: Transactions },
  { path: "/categories", name: "categories", component: Categories },
  { path: "/savings", name: "savings", component: Savings },
  { path: "/stats", name: "stats", component: Stats },
  { path: "/profile", name: "profile", component: Profile },

  // Admin
  { path: "/admin", name: "admin-dashboard", component: AdminDashboard, meta: { requiresAdmin: true } },
  { path: "/admin/users", name: "admin-users", component: AdminUsers, meta: { requiresAdmin: true } },
  { path: "/admin/categories", name: "admin-categories", component: AdminCategories, meta: { requiresAdmin: true } },
  { path: "/admin/currencies", name: "admin-currencies", component: AdminCurrencies, meta: { requiresAdmin: true } },
  { path: "/admin/monitoring", name: "admin-monitoring", component: AdminMonitoring, meta: { requiresAdmin: true } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Global guard
router.beforeEach((to, from, next) => {
  const auth = useAuthStore();
  const isPublic = to.meta?.public === true;

  if (isPublic) return next();

  // zaštićene rute
  if (!auth.isAuthenticated) return next({ name: "login" });

  // admin-only
  if (to.meta?.requiresAdmin && !auth.isAdmin) return next({ name: "dashboard" });

  next();
});

export default router;
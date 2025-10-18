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

// Recurring Transactions
const Recurring = () => import("@/views/RecurringView.vue");

// Admin
const AdminDashboard = () => import("@/views/admin/AdminDashboardView.vue");
const AdminUsers = () => import("@/views/admin/AdminUsersView.vue");
const AdminCategories = () => import("@/views/admin/AdminCategoriesView.vue");
const AdminCurrencies = () => import("@/views/admin/AdminCurrenciesView.vue");
const AdminMonitoring = () => import("@/views/admin/AdminMonitoringView.vue");

const routes = [
  { path: "/", name: "landing", component: Landing, meta: { public: true } },
  { path: "/login", name: "login", component: Login, meta: { public: true } },
  { path: "/register", name: "register", component: Register, meta: { public: true } },

  // Dashboard (ADMIN ONLY)
  { path: "/dashboard", name: "dashboard", component: Dashboard, meta: { requiresAdmin: true } },

  // Authenticated user routes
  { path: "/wallets", name: "wallets", component: Wallets },
  { path: "/transactions", name: "transactions", component: Transactions },
  { path: "/categories", name: "categories", component: Categories },
  { path: "/savings", name: "savings", component: Savings },
  { path: "/stats", name: "stats", component: Stats },
  // Profile - keep route defined (UI unchanged) but guarded below
  { path: "/profile", name: "profile", component: Profile },
  { path: "/recurring", name: "recurring", component: Recurring },

  // Admin routes
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
router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore();

  // If auth store has an async initializer, call/await it once.
  if (typeof auth.initialize === "function" && !auth.__initialized) {
    try {
      await auth.initialize(); // should set __initialized = true inside the store
    } catch (e) {
      // initialization failed but we continue (unauthenticated state)
      console.warn("Auth initialization failed:", e);
    }
  }

  const isPublic = to.meta?.public === true;

  if (isPublic) {
    if ((to.name === "login" || to.name === "register") && auth.isAuthenticated) {
      return next({ name: "wallets", replace: true });
    }
    return next();
  }

  // require login for non-public routes
  if (!auth.isAuthenticated) {
    return next({ name: "landing", replace: true });
  }

  // Prevent admins from accessing the Profile page (both nav & direct URL)
  if (to.name === "profile" && auth.isAdmin) {
    return next({ name: "admin-dashboard", replace: true });
  }

  // admin routes
  if (to.meta?.requiresAdmin && !auth.isAdmin) {
    return next({ name: "wallets", replace: true });
  }

  next();
});

export default router;
<template>
  <header class="nav-wrap">
    <nav class="nav container">
      <div class="left">
        <RouterLink to="/" class="brand">
          <span class="dot"></span>
          TrackYourFinances
          <span class="inverse-dot"></span>
        </RouterLink>
      </div>

      <button
        class="burger"
        @click="open = !open"
        aria-label="Toggle menu"
      >
        <svg viewBox="0 0 24 24" width="32" height="32">
          <path fill="currentColor" d="M3 6h18M3 12h18M3 18h18" />
        </svg>
      </button>

      <div class="links" :class="{ open }">
        <RouterLink v-if="!auth.isAuthenticated" to="/login" class="btn btn--outline btn--sm">
          Login
        </RouterLink>
        <RouterLink v-if="!auth.isAuthenticated" to="/register" class="btn btn--primary btn--sm">
          Register
        </RouterLink>

        <template v-if="auth.isAuthenticated">
          <RouterLink to="/dashboard">Dashboard</RouterLink>
          <RouterLink to="/wallets">Wallets</RouterLink>
          <RouterLink to="/transactions">Transactions</RouterLink>
          <RouterLink to="/recurring">Recurring Transactions</RouterLink>
          <RouterLink to="/categories">Categories</RouterLink>
          <RouterLink to="/savings">Saving Goals</RouterLink>
          <RouterLink to="/stats">Stats</RouterLink>
          <RouterLink to="/profile">Profile</RouterLink>
          <RouterLink v-if="auth.isAdmin" to="/admin">Admin</RouterLink>

          <button class="btn btn--danger btn--sm" @click="logout">
            Logout
          </button>
        </template>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { ref } from "vue";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();
const open = ref(false);

const logout = () => {
  auth.logout();
  window.location.href = "/login";
};
</script>

<style scoped>
.nav-wrap {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(to bottom, rgba(5, 10, 8, 0.85), rgba(5, 10, 8, 0.55));
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 64px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: radial-gradient(circle at 30% 30%, #a7f3d0, var(--brand));
    box-shadow: 0 0 12px var(--brand);
    animation: pulse 1.2s alternate infinite;
}

.nav-wrap .nav.container .left .brand .inverse-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: radial-gradient(circle at 30% 30%, #a7f3d0, var(--brand));
    box-shadow: 0 0 12px var(--brand);
    animation: inverse-pulse 1.2s alternate infinite;
}

@keyframes pulse {
    0% {
        box-shadow: 0 0 12px var(--brand);
        transform: translateY(-7px);
    }

    100% {
        box-shadow: 0 0 12px var(--brand);
        transform: translateY(7px);
    }
}

@keyframes inverse-pulse {
    0% {
        box-shadow: 0 0 12px var(--brand);
        transform: translateY(7px);
    }

    100% {
        box-shadow: 0 0 12px var(--brand);
        transform: translateY(-7px);
    }
}

/* linkovi */
.links {
  display: flex;
  align-items: center;
  gap: 12px;
}

.links a {
  padding: 10px 12px;
  border-radius: 12px;
  transition: all 0.18s ease;
  color: var(--txt);
}

.links a:hover {
  background: rgba(255, 255, 255, 0.06);
}

/* burger (mobile) */
.burger {
  display: none;
  background: none;
  border: none;
  color: var(--txt);
  padding: 6px;
  border-radius: 8px;
}

.burger:hover {
  background: rgba(232, 232, 232, 0.06);
}

@media (max-width: 880px) {
  .burger {
    display: block;
  }

  .links {
    position: absolute;
    right: 16px;
    top: 68px;
    background: rgba(9, 14, 12, 0.92);
    border: 1px solid rgba(255, 255, 255, 0.06);
    border-radius: 16px;
    padding: 12px;
    gap: 8px;
    display: none;
    flex-direction: column;
    min-width: 220px;
    box-shadow: 0 12px 36px rgba(0, 0, 0, 0.35);
  }

  .links.open {
    display: flex;
  }
}
</style>
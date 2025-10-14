<template>
  <div class="container page" style="max-width: 520px;">
    <div class="card">
      <h1 style="margin-top: 0; margin-bottom: 6px;">Sign in</h1>
      <p class="muted" style="margin-top: 0;">Welcome back 👋 Please enter your details.</p>

      <form class="form" @submit.prevent="onSubmit">
        <!-- Username / email -->
        <div class="form__group">
          <label class="form__label" for="login-username">Username or email</label>

          <div class="input-group">
            <span class="affix affix--left">📧</span>
            <input
              id="login-username"
              v-model="form.username"
              type="text"
              class="form__control"
              placeholder="you@example.com"
              required
            />
          </div>

          <p class="form__help">Use the credentials you registered with.</p>
        </div>

        <!-- Password -->
        <div class="form__group">
          <label class="form__label" for="login-password">Password</label>

          <div class="input-group">
            <span class="affix affix--left">🔒</span>
            <input
              id="login-password"
              v-model="form.password"
              :type="show ? 'text' : 'password'"
              class="form__control"
              placeholder="••••••••"
              required
            />
            <button
              type="button"
              class="affix affix--right btn btn--ghost btn--sm"
              @click="show = !show"
            >
              {{ show ? 'Hide' : 'Show' }}
            </button>
          </div>

          <p v-if="error" class="form__error">{{ error }}</p>
        </div>

        <!-- Row helpers -->
        <div class="form__row form__row--2">
          <label class="check">
            <input type="checkbox" v-model="remember" />
            <span>Remember me</span>
          </label>

          <div style="text-align: right;">
            <RouterLink to="/reset" class="btn btn--ghost btn--sm">
              Forgot password?
            </RouterLink>
          </div>
        </div>

        <!-- Actions -->
        <div style="display: flex; gap: 12px; justify-content: flex-end;">
          <RouterLink to="/" class="btn btn--secondary">Cancel</RouterLink>
          <button
            type="submit"
            class="btn btn--primary"
            :class="{'is-loading': loading}"
            :disabled="loading"
          >
            {{ loading ? 'Signing in...' : 'Login' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();
const router = useRouter();

const form = reactive({ username: "", password: "" });
const loading = ref(false);
const error = ref("");
const remember = ref(true);
const show = ref(false);

const onSubmit = async () => {
  error.value = "";
  loading.value = true;
  try {
    await auth.login(form);
    if (!remember.value) {
      // ako ne želi da “pamti”, samo očisti storage – token ostaje u memoriji kroz store
      localStorage.removeItem("jwt");
      localStorage.removeItem("role");
    }
    router.push({ name: "dashboard" });
  } catch (e) {
    error.value = e?.response?.data?.message || "Invalid credentials";
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.muted {
  color: var(--muted);
}
</style>
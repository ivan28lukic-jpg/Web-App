<template>
  <FormCard
    title="Create account"
    subtitle="Join WebFinance — it’s quick and free."
    maxWidth="620px"
  >
    <form class="form" @submit.prevent="onSubmit">
      <div class="form__row form__row--2">
        <div class="form__group">
          <label class="form__label" for="reg-first">First name</label>
          <input
            id="reg-first"
            v-model.trim="form.firstName"
            type="text"
            class="form__control"
            placeholder="Nikola"
            required
          />
        </div>

        <div class="form__group">
          <label class="form__label" for="reg-last">Last name</label>
          <input
            id="reg-last"
            v-model.trim="form.lastName"
            type="text"
            class="form__control"
            placeholder="Vučković"
            required
          />
        </div>
      </div>

      <div class="form__group">
        <label class="form__label" for="reg-username">Username</label>
        <div class="input-group">
          <span class="affix affix--left">👤</span>
          <input
            id="reg-username"
            v-model.trim="form.username"
            type="text"
            class="form__control"
            placeholder="nikola_v"
            required
          />
        </div>
        <p class="form__help">3–32 karaktera, bez razmaka.</p>
      </div>

      <div class="form__group">
        <label class="form__label" for="reg-email">Email</label>
        <div class="input-group">
          <span class="affix affix--left">📧</span>
          <input
            id="reg-email"
            v-model.trim="form.email"
            type="email"
            class="form__control"
            placeholder="you@example.com"
            required
          />
        </div>
      </div>

      <div class="form__row form__row--2">
        <div class="form__group">
          <label class="form__label" for="reg-pass">Password</label>
          <div class="input-group">
            <span class="affix affix--left">🔒</span>
            <input
              id="reg-pass"
              v-model="form.password"
              :type="show ? 'text' : 'password'"
              class="form__control"
              placeholder="••••••••"
              required
              :class="{ 'is-invalid': passError }"
            />
            <button
              type="button"
              class="affix affix--right btn btn--ghost btn--sm"
              @click="show = !show"
            >
              {{ show ? 'Hide' : 'Show' }}
            </button>
          </div>
        </div>

        <div class="form__group">
          <label class="form__label" for="reg-pass2">Confirm password</label>
          <input
            id="reg-pass2"
            v-model="form.confirm"
            :type="show ? 'text' : 'password'"
            class="form__control"
            placeholder="repeat password"
            required
            :class="{ 'is-invalid': passError }"
          />
          <p v-if="passError" class="form__error">Passwords don’t match.</p>
        </div>
      </div>

      <label class="check">
        <input type="checkbox" v-model="tos" />
        <span>I agree with Terms of Service</span>
      </label>

      <div style="display: flex; gap: 12px; justify-content: flex-end;">
        <RouterLink to="/login" class="btn btn--secondary">Back to login</RouterLink>
        <button
          type="submit"
          class="btn btn--primary"
          :class="{ 'is-loading': loading }"
          :disabled="loading || passError || !tos"
        >
          {{ loading ? 'Creating…' : 'Create account' }}
        </button>
      </div>

      <p v-if="error" class="form__error" style="margin-top: 8px;">{{ error }}</p>
      <p v-if="success" class="form__help" style="margin-top: 8px;">
        Account created. Redirecting to login…
      </p>
    </form>

    <template #footer>
      <p class="form__help">
        Already have an account? <RouterLink to="/login">Sign in</RouterLink>.
      </p>
    </template>
  </FormCard>
</template>

<script setup>
import { reactive, ref, computed } from "vue";
import { useRouter } from "vue-router";
import api from "@/services/api";
import FormCard from "@/components/FormCard.vue";

const router = useRouter();

const form = reactive({
  firstName: "",
  lastName: "",
  username: "",
  email: "",
  password: "",
  confirm: "",
});

const show = ref(false);
const loading = ref(false);
const error = ref("");
const success = ref(false);
const tos = ref(true);

const passError = computed(() => !!form.password && !!form.confirm && form.password !== form.confirm);

const onSubmit = async () => {
  error.value = "";
  success.value = false;

  if (passError.value) return;

  loading.value = true;
  try {
    // prilagodi polja backendu ako se razlikuju
    await api.post("/auth/register", {
      firstName: form.firstName,
      lastName: form.lastName,
      username: form.username,
      email: form.email,
      password: form.password,
    });

    success.value = true;
    // mali timeout pa login
    setTimeout(() => router.push({ name: "login" }), 900);
  } catch (e) {
    error.value =
      e?.response?.data?.message ||
      e?.response?.data?.error ||
      "Registration failed";
  } finally {
    loading.value = false;
  }
};
</script>
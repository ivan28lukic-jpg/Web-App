<template>
  <div class="container page" :style="maxWidthStyle">
    <div class="card">
      <header v-if="title || subtitle" class="fc__header">
        <h1 class="fc__title" v-if="title">{{ title }}</h1>
        <p class="fc__subtitle" v-if="subtitle">{{ subtitle }}</p>
      </header>

      <section class="fc__body">
        <slot />
      </section>

      <footer class="fc__footer" v-if="$slots.footer">
        <slot name="footer" />
      </footer>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  title: { type: String, default: "" },
  subtitle: { type: String, default: "" },
  maxWidth: { type: String, default: "560px" }, // npr. "720px"
});

const maxWidthStyle = computed(() => `max-width: ${props.maxWidth};`);
</script>

<style scoped>
.fc__header {
  margin-bottom: 14px;
}
.fc__title {
  margin: 0 0 6px 0;
}
.fc__subtitle {
  margin: 0;
  color: var(--muted);
}
.fc__body {
  display: block;
}
.fc__footer {
  margin-top: 16px;
}
</style>
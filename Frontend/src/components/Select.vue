<template>
  <div class="form__group">
    <label v-if="label" class="form__label" :for="id">{{ label }}</label>

    <div class="input-group">
      <span v-if="$slots.left" class="affix affix--left">
        <slot name="left" />
      </span>

      <select
        :id="id"
        :name="name || id"
        class="form__control"
        :class="{ 'is-invalid': invalid }"
        :disabled="disabled"
        :value="modelValue"
        @change="onChange"
        @blur="$emit('blur')"
        @focus="$emit('focus')"
      >
        <option v-if="placeholder" disabled value="">{{ placeholder }}</option>
        <option
          v-for="opt in normalizedOptions"
          :key="opt.value"
          :value="opt.value"
        >
          {{ opt.label }}
        </option>
      </select>

      <span v-if="$slots.right" class="affix affix--right">
        <slot name="right" />
      </span>
    </div>

    <p v-if="help && !error" class="form__help">{{ help }}</p>
    <p v-if="error" class="form__error">{{ error }}</p>
  </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  modelValue: { type: [String, Number], default: "" },
  id: { type: String, default: undefined },
  name: { type: String, default: undefined },
  label: { type: String, default: "" },
  placeholder: { type: String, default: "" },
  help: { type: String, default: "" },
  error: { type: String, default: "" },
  options: { type: Array, default: () => [] },
  disabled: { type: Boolean, default: false },
  invalid: { type: Boolean, default: false },
});

const normalizedOptions = computed(() =>
  (props.options || []).map((o) =>
    typeof o === "object" ? o : { value: o, label: String(o) }
  )
);

const emit = defineEmits(["update:modelValue", "blur", "focus"]);

function onChange(e) {
  emit("update:modelValue", e.target && e.target.value);
}
</script>
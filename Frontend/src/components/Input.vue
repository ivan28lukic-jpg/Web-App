<template>
  <div class="form__group">
    <label v-if="label" class="form__label" :for="id">{{ label }}</label>

    <div class="input-group">
      <span v-if="$slots.left" class="affix affix--left">
        <slot name="left" />
      </span>

      <input
        :id="id"
        :name="name || id"
        :type="type"
        :placeholder="placeholder"
        class="form__control"
        :class="{ 'is-invalid': invalid }"
        :autocomplete="autocomplete"
        :disabled="disabled"
        :readonly="readonly"
        :min="min"
        :max="max"
        :step="step"
        :value="modelValue"
        @input="onInput"
        @blur="$emit('blur')"
        @focus="$emit('focus')"
      />

      <span v-if="$slots.right" class="affix affix--right">
        <slot name="right" />
      </span>
    </div>

    <p v-if="help && !error" class="form__help">{{ help }}</p>
    <p v-if="error" class="form__error">{{ error }}</p>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: [String, Number], default: "" },
  id: { type: String, default: undefined },
  name: { type: String, default: undefined },
  label: { type: String, default: "" },
  placeholder: { type: String, default: "" },
  help: { type: String, default: "" },
  error: { type: String, default: "" },
  type: { type: String, default: "text" },
  autocomplete: { type: String, default: "off" },
  disabled: { type: Boolean, default: false },
  readonly: { type: Boolean, default: false },
  invalid: { type: Boolean, default: false },
  min: { type: [String, Number], default: undefined },
  max: { type: [String, Number], default: undefined },
  step: { type: [String, Number], default: undefined },
});

const emit = defineEmits(["update:modelValue", "blur", "focus"]);

function onInput(e) {
  emit("update:modelValue", e.target && e.target.value);
}
</script>
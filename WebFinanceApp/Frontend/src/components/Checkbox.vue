<template>
  <label class="check">
    <input
      type="checkbox"
      :id="id"
      :name="name || id"
      :disabled="disabled"
      :checked="isChecked"
      @change="onChange"
    />
    <span><slot>{{ label }}</slot></span>
  </label>
  <p v-if="help && !error" class="form__help" style="margin-left: 28px;">{{ help }}</p>
  <p v-if="error" class="form__error" style="margin-left: 28px;">{{ error }}</p>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  modelValue: { type: [Boolean, Array], default: undefined },
  value: { type: [String, Number], default: undefined }, // za checkbox-group
  id: { type: String, default: undefined },
  name: { type: String, default: undefined },
  label: { type: String, default: "" },
  help: { type: String, default: "" },
  error: { type: String, default: "" },
  disabled: { type: Boolean, default: false },
});

const emit = defineEmits(["update:modelValue"]);

const isGroup = computed(() => Array.isArray(props.modelValue));
const isChecked = computed(() => {
  return isGroup.value
    ? (props.modelValue || []).includes(props.value)
    : !!props.modelValue;
});

function onChange(e) {
  const checked = e.target && e.target.checked;

  if (isGroup.value) {
    const arr = new Set([...(props.modelValue || [])]);
    if (checked) arr.add(props.value);
    else arr.delete(props.value);
    emit("update:modelValue", Array.from(arr));
  } else {
    emit("update:modelValue", !!checked);
  }
}
</script>
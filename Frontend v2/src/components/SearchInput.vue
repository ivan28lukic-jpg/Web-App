<template>
    <div class="search-input">
        <input
            v-model="model"
            type="text"
            :placeholder="placeholder || 'Pretraži po imenu...'"
            class="search-input__field"
            aria-label="Search"
        />
    </div>
</template>


<script setup>
import { computed } from 'vue'


const props = defineProps({
    modelValue: { type: String, default: '' },
    placeholder: { type: String, default: '' },
})
const emit = defineEmits(['update:modelValue'])

const model = computed({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v),
})
</script>


<style scoped>

.search-input {
    display: flex;
    align-items: center;
    width: 100%;
}

.search-input__field {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #e6e6e6;
    border-radius: 10px;
    outline: none;
}

.search-input__field:focus {
    border-color: var(--c-green);
    box-shadow: 0 0 0 3px rgba(0, 168, 107, 0.15);
}

</style>
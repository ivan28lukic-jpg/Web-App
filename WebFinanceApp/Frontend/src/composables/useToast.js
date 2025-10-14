import { reactive } from "vue";

const state = reactive({
  items: [],
  seq: 1,
});

function push(type, message, ttl = 2500) {
  const id = state.seq++;
  state.items.push({ id, type, message, ttl });

  setTimeout(() => {
    const idx = state.items.findIndex((t) => t.id === id);
    if (idx !== -1) state.items.splice(idx, 1);
  }, ttl);
}

export function useToast() {
  return {
    items: state.items,
    info: (msg, ttl) => push("info", msg, ttl),
    success: (msg, ttl) => push("success", msg, ttl),
    error: (msg, ttl) => push("error", msg, ttl),
    warning: (msg, ttl) => push("warning", msg, ttl),
    remove: (id) => {
      const idx = state.items.findIndex((t) => t.id === id);
      if (idx !== -1) state.items.splice(idx, 1);
    },
  };
}
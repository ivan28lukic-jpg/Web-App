import { ref, watch, onBeforeUnmount } from 'vue'


export function useLiveSearch(fetcher, opts = {}) {
    const query = ref('')
    const results = ref(null)
    const isLoading = ref(false)
    const error = ref(null)

    let timer
    let ctrl = null

    const run = async (q) => {
        if (ctrl) ctrl.abort()
        ctrl = new AbortController()

        isLoading.value = true
        error.value = null
        try {
            results.value = await fetcher(q, ctrl.signal)
        } catch (e) {
            if (e?.name !== 'CanceledError' && e?.name !== 'AbortError') {
                error.value = e
            }
        } finally {
            isLoading.value = false
        }
    }

    const schedule = (q) => {
        if (timer) window.clearTimeout(timer)
        const delay = opts.delayMs ?? 250
        timer = window.setTimeout(() => run(q), delay)
    }

    watch(
        query,
        (qRaw) => {
            const q = qRaw.trim()
            const minLen = opts.minLength ?? 0
            if (q.length < minLen) {
                schedule('')
                return
            }
        schedule(q)
    },
    { immediate: !!opts.immediate }
    )

    onBeforeUnmount(() => {
        if (timer) window.clearTimeout(timer)
        if (ctrl) ctrl.abort()
    })

    return { query, results, isLoading, error, run }
}
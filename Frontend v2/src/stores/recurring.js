import { defineStore } from 'pinia'
import { getWithAbort, http } from '@/api/http'
import { useAuthStore } from '@/stores/auth'


export const useRecurringsStore = defineStore('recurrings', {
state: () => ({
items: [],
loading: false,
error: null,
}),
actions: {
async fetchList(search = '', signal) {
this.loading = true
this.error = null
try {
const auth = useAuthStore()
const data = await getWithAbort('/recurrings', {
ownerId: auth.userId,
search: search || undefined,
}, signal)
this.items = Array.isArray(data) ? data : (data.items || [])
return this.items
} catch (e) {
this.error = e
throw e
} finally {
this.loading = false
}
},
async create(payload) {
const data = await http.post('/recurrings', payload).then(r => r.data)
await this.fetchList('')
return data
},
async toggleActive(id, active) {
const data = await http.put(`/recurrings/${id}`, { active }).then(r => r.data)
await this.fetchList('')
return data
},
async remove(id) {
await http.delete(`/recurrings/${id}`)
await this.fetchList('')
},
async runDue() {
// Ako backend ima endpoint za ručno generisanje dospelih
return http.post('/recurrings/run-due').then(r => r.data)
},
},
})
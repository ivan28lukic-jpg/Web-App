import { defineStore } from 'pinia'
import { getWithAbort, http } from '@/api/http'
import { useAuthStore } from '@/stores/auth'


export const useWalletsStore = defineStore('wallets', {
state: () => ({
list: [],
loading: false,
error: null,
}),
actions: {
async fetchList(search, includeArchived = false, signal) {
this.loading = true
this.error = null
try {
const auth = useAuthStore()
const data = await getWithAbort('/wallets', {
ownerId: auth.userId,
includeArchived,
search: search || undefined, // backend trenutno nema search, ignoriše parametar
}, signal)
this.list = data
return data
} catch (e) {
this.error = e
throw e
} finally {
this.loading = false
}
},
async create(payload) {
const data = await http.post('/wallets', payload).then(r => r.data)
await this.fetchList('')
return data
},
async update(id, payload) {
const data = await http.put(`/wallets/${id}`, payload).then(r => r.data)
await this.fetchList('')
return data
},
async remove(id) {
await http.delete(`/wallets/${id}`)
await this.fetchList('')
},
async setArchived(id, archived) {
// može preko PUT (payload.archived) ili PATCH /{id}/archive ?archived=true
await http.put(`/wallets/${id}`, { name: undefined, archived })
await this.fetchList('')
},
},
})
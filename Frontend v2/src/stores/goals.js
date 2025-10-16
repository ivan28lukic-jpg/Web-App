import { defineStore } from 'pinia'
import { getWithAbort, http } from '@/api/http'
import { useAuthStore } from '@/stores/auth'


export const useGoalsStore = defineStore('goals', {
state: () => ({ items: [], loading:false, error:null }),
actions: {
async fetchList(search = '', signal){
this.loading = true
this.error = null
try{
const auth = useAuthStore()
const data = await getWithAbort('/goals', { ownerId: auth.userId, search: search || undefined }, signal)
this.items = Array.isArray(data) ? data : (data.items || [])
return this.items
}catch(e){ this.error=e; throw e }
finally{ this.loading=false }
},
async create(payload){ const d = await http.post('/goals', payload).then(r=>r.data); await this.fetchList(''); return d },
async update(id, payload){ const d = await http.put(`/goals/${id}`, payload).then(r=>r.data); await this.fetchList(''); return d },
async remove(id){ await http.delete(`/goals/${id}`); await this.fetchList('') },
}
})
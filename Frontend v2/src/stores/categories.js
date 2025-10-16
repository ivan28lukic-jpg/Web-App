import { defineStore } from 'pinia'
import { getWithAbort, http } from '@/api/http'


export const useCategoriesStore = defineStore('categories', {
state: () => ({ items: [], loading:false, error:null }),
actions: {
async fetchList(search = '', signal){
this.loading = true
this.error = null
try{
const data = await getWithAbort('/categories', { scope: 'mine+global', search: search || undefined }, signal)
// očekujemo niz; obeleži global vs my
this.items = (Array.isArray(data) ? data : (data.items||[])).map(c => ({ ...c, _isGlobal: !!c.predefined || c.userId==null }))
return this.items
}catch(e){ this.error=e; throw e }
finally{ this.loading=false }
},
async create(payload){ const d = await http.post('/categories', payload).then(r=>r.data); await this.fetchList(''); return d },
async remove(id){ await http.delete(`/categories/${id}`); await this.fetchList('') },
}
})
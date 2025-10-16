import { defineStore } from 'pinia'
import { getWithAbort } from '@/api/http'
import { useAuthStore } from '@/stores/auth'


export const useStatsStore = defineStore('stats', {
state: () => ({
summary: null,
byCategory: [],
topExpenses: [],
loading: false,
error: null,
}),
actions: {
async fetch(period = 'MONTH', filters = {}, signal){
this.loading = true
this.error = null
try{
const auth = useAuthStore()
const params = { ownerId: auth.userId, period, ...filters }
const data = await getWithAbort('/stats', params, signal)
// očekujemo { summary:{income,expense,balance}, byCategory:[{name,amount}], topExpenses:[...] }
this.summary = data.summary || null
this.byCategory = data.byCategory || []
this.topExpenses = data.topExpenses || []
return data
}catch(e){ this.error=e; throw e }
finally{ this.loading=false }
}
}
})
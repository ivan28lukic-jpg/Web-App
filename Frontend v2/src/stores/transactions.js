import { defineStore } from 'pinia'
import { getWithAbort, http } from '@/api/http'
import { useAuthStore } from '@/stores/auth'


export const useTransactionsStore = defineStore('transactions', {
state: () => ({
page: 1,
size: 20,
total: 0,
items: [],
loading: false,
error: null,
lastFilters: { search: '', walletId: null, type: 'ALL', dateFrom: '', dateTo: '' },
}),
actions: {
async fetchList(filters = {}, signal) {
this.loading = true
this.error = null
try {
const auth = useAuthStore()
const params = {
ownerId: auth.userId,
page: this.page,
size: this.size,
search: filters.search || undefined,
walletId: filters.walletId || undefined,
type: filters.type && filters.type !== 'ALL' ? filters.type : undefined, // INCOME/EXPENSE
dateFrom: filters.dateFrom || undefined, // ISO yyyy-MM-dd
dateTo: filters.dateTo || undefined,
}
const data = await getWithAbort('/transactions', params, signal)
// očekujemo { items, total, page, size } ili sličnu paginaciju; ako vrati niz, prilagodi
if (Array.isArray(data)) {
this.items = data
this.total = data.length
} else {
this.items = data.items || []
this.total = data.total || 0
this.page = data.page || 1
this.size = data.size || this.size
}
this.lastFilters = { ...this.lastFilters, ...filters }
return data
} catch (e) {
this.error = e
throw e
} finally {
this.loading = false
}
},
async create(payload) {
const data = await http.post('/transactions', payload).then(r => r.data)
await this.fetchList(this.lastFilters)
return data
},
async transfer(payload) {
// npr. POST /transactions/transfer { fromWalletId, toWalletId, amount, currencyCode?, note? }
const data = await http.post('/transactions/transfer', payload).then(r => r.data)
await this.fetchList(this.lastFilters)
return data
},
async remove(id) {
await http.delete(`/transactions/${id}`)
await this.fetchList(this.lastFilters)
},
},
})
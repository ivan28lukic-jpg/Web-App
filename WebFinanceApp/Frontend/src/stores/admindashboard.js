import { defineStore } from 'pinia'
import axios from 'axios'

export const useAdminDashboardStore = defineStore('adminDashboard', {
  state: () => ({
    totalUsers: 0,
    activeUsersLast30d: 0,
    totalBalanceAll: 0,
    avgBalanceActive: 0,
    top30d: [],
    top2m: [],
    loading: false,
    error: null,
  }),
  actions: {
    async fetchDashboard() {
      this.loading = true
      try {
        const res = await axios.get('/api/admin/dashboard')
        this.totalUsers = res.data.totalUsers
        this.activeUsersLast30d = res.data.activeUsersLast30d
        this.totalBalanceAll = res.data.totalBalanceAll
        this.avgBalanceActive = res.data.avgBalanceActive
        this.top30d = res.data.top30d || []
        this.top2m = res.data.top2m || []
        this.error = null
      } catch (e) {
        this.error = e?.response?.data?.message || 'Greška pri dohvatanju dashboard podataka.'
      }
      this.loading = false
    }
  }
})
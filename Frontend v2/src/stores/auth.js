import { defineStore } from 'pinia'
import { http } from '@/api/http'


export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('jwt') || '',
        role: localStorage.getItem('role') || '',
        userId: localStorage.getItem('userId') || '',
}),
getters: {
    isLoggedIn: (s) => !!s.token,
    isAdmin: (s) => s.role === 'ADMIN',
},
actions: {
    async login(username, password) {
        const res = await http.post('/login', { username, password })
        const { token, role, userId } = res.data
        this.token = token
        this.role = role
        this.userId = String(userId)
        localStorage.setItem('jwt', token)
        localStorage.setItem('role', role)
        localStorage.setItem('userId', String(userId))
    },
    logout() {
        this.token = ''
        this.role = ''
        this.userId = ''
        localStorage.removeItem('jwt')
        localStorage.removeItem('role')
        localStorage.removeItem('userId')
        },
    },
})
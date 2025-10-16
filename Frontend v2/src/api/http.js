import axios from 'axios'
import { useAuthStore } from '@/stores/auth'


export const http = axios.create({
    baseURL: '/api',
})


http.interceptors.request.use((config) => {
    const auth = useAuthStore()
    if (auth.token) {
        config.headers.Authorization = `Bearer ${auth.token}`
    }
return config
})


export function getWithAbort(url, params, signal) {
    return http.get(url, { params, signal }).then(r => r.data)
}
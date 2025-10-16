import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'


const Login = () => import('@/views/auth/Login.vue')
const WalletsList = () => import('@/views/wallets/WalletsList.vue')
const WalletDetail = () => import('@/views/wallets/WalletDetail.vue')
const TransactionsList = () => import('@/views/transactions/TransactionsList.vue')
const RecurringsList = () => import('@/views/recurrings/RecurringsList.vue')
const CategoriesList = () => import('@/views/categories/CategoriesList.vue')
const GoalsList = () => import('@/views/goals/GoalsList.vue')
const StatsDashboard = () => import('@/views/stats/StatsDashboard.vue')
const Profile = () => import('@/views/profile/Profile.vue')


const routes = [
{ path: '/', redirect: '/wallets' },
{ path: '/login', component: Login, meta: { public: true } },
{ path: '/wallets', component: WalletsList },
{ path: '/wallets/:id', component: WalletDetail, props: true },
{ path: '/transactions', component: TransactionsList },
{ path: '/recurrings', component: RecurringsList },
{ path: '/categories', component: CategoriesList },
{ path: '/goals', component: GoalsList },
{ path: '/stats', component: StatsDashboard },
{ path: '/profile', component: Profile },
]


const router = createRouter({
history: createWebHistory(),
routes,
})


router.beforeEach((to) => {
const auth = useAuthStore()
if (!to.meta.public && !auth.isLoggedIn) {
return { path: '/login' }
}
})


export default router
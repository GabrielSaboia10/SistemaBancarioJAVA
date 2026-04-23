import axios from 'axios'

const STORAGE_KEY = 'bancoapp_auth'

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

client.interceptors.request.use((config) => {
  const stored = localStorage.getItem(STORAGE_KEY)
  if (stored) {
    try {
      const auth = JSON.parse(stored)
      config.headers.Authorization = `Bearer ${auth.token}`
    } catch {}
  }
  return config
})

client.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem(STORAGE_KEY)
      window.location.href = '/login'
      return new Promise(() => {})
    }
    const msg = err.response?.data?.erro ?? 'Erro desconhecido'
    return Promise.reject(new Error(msg))
  }
)

export default client

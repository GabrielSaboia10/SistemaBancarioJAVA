import { createContext, useContext, useState, useCallback, ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { AuthUser } from '../types'
import { authApi } from '../api/auth'

const STORAGE_KEY = 'bancoapp_auth'

interface AuthContextValue {
  user: AuthUser | null
  login: (cpf: string, senha: string) => Promise<void>
  logout: () => void
  isAdmin: boolean
  isCliente: boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

function loadFromStorage(): AuthUser | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as AuthUser) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(loadFromStorage)
  const navigate = useNavigate()

  const login = useCallback(async (cpf: string, senha: string) => {
    const data = await authApi.login(cpf, senha)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
    setUser(data)
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY)
    setUser(null)
    navigate('/login', { replace: true })
  }, [navigate])

  return (
    <AuthContext.Provider value={{
      user,
      login,
      logout,
      isAdmin: user?.role === 'ADMIN',
      isCliente: user?.role === 'CLIENTE',
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth deve ser usado dentro de AuthProvider')
  return ctx
}

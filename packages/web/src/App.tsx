import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute'
import Layout from './components/Layout/Layout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Pessoas from './pages/Pessoas'
import Agencias from './pages/Agencias'
import Contas from './pages/Contas'
import Operacoes from './pages/Operacoes'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }>
            <Route index element={<Dashboard />} />
            <Route path="pessoas" element={
              <ProtectedRoute roles={['ADMIN']}>
                <Pessoas />
              </ProtectedRoute>
            } />
            <Route path="agencias" element={
              <ProtectedRoute roles={['ADMIN']}>
                <Agencias />
              </ProtectedRoute>
            } />
            <Route path="contas" element={
              <ProtectedRoute roles={['ADMIN']}>
                <Contas />
              </ProtectedRoute>
            } />
            <Route path="operacoes" element={<Operacoes />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

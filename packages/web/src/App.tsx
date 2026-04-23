import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Layout from './components/Layout/Layout'
import Dashboard from './pages/Dashboard'
import Pessoas from './pages/Pessoas'
import Agencias from './pages/Agencias'
import Contas from './pages/Contas'
import Operacoes from './pages/Operacoes'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="pessoas" element={<Pessoas />} />
          <Route path="agencias" element={<Agencias />} />
          <Route path="contas" element={<Contas />} />
          <Route path="operacoes" element={<Operacoes />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

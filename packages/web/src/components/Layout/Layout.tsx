import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'

export default function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* Top bar visível só no mobile */}
      <div className="mobile-topbar">
        <button
          onClick={() => setSidebarOpen(true)}
          style={{
            background: 'none',
            border: 'none',
            color: '#fff',
            fontSize: 22,
            padding: '4px 8px',
            cursor: 'pointer',
            lineHeight: 1,
          }}
          aria-label="Abrir menu"
        >
          ☰
        </button>
        <span style={{ color: '#fff', fontWeight: 700, fontSize: 16 }}>🏦 BancoApp</span>
      </div>

      {/* Overlay escuro quando sidebar aberta no mobile */}
      {sidebarOpen && (
        <div className="sidebar-overlay" onClick={() => setSidebarOpen(false)} />
      )}

      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <main className="layout-main">
        <Outlet />
      </main>
    </div>
  )
}

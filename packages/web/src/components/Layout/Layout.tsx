import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'

export default function Layout() {
  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar />
      <main style={{
        marginLeft: 'var(--sidebar-w)',
        flex: 1,
        padding: '32px',
        background: 'var(--bg)',
        minHeight: '100vh',
      }}>
        <Outlet />
      </main>
    </div>
  )
}

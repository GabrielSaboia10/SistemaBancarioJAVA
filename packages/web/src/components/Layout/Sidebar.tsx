import { NavLink } from 'react-router-dom'

const links = [
  { to: '/',           label: 'Dashboard',   icon: '▦' },
  { to: '/pessoas',    label: 'Clientes',     icon: '👤' },
  { to: '/agencias',   label: 'Agências',     icon: '🏦' },
  { to: '/contas',     label: 'Contas',       icon: '💳' },
  { to: '/operacoes',  label: 'Operações',    icon: '↔' },
]

export default function Sidebar() {
  return (
    <aside style={{
      width: 'var(--sidebar-w)',
      minHeight: '100vh',
      background: 'var(--primary)',
      display: 'flex',
      flexDirection: 'column',
      position: 'fixed',
      top: 0,
      left: 0,
    }}>
      <div style={{ padding: '28px 20px 20px', borderBottom: '1px solid rgba(255,255,255,.1)' }}>
        <div style={{ color: '#fff', fontSize: 18, fontWeight: 700, letterSpacing: '.5px' }}>🏦 BancoApp</div>
        <div style={{ color: 'rgba(255,255,255,.5)', fontSize: 12, marginTop: 4 }}>Sistema Bancário</div>
      </div>

      <nav style={{ flex: 1, padding: '12px 0' }}>
        {links.map((l) => (
          <NavLink
            key={l.to}
            to={l.to}
            end={l.to === '/'}
            style={({ isActive }) => ({
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              padding: '11px 20px',
              color: isActive ? '#fff' : 'rgba(255,255,255,.65)',
              background: isActive ? 'rgba(255,255,255,.12)' : 'transparent',
              borderLeft: isActive ? '3px solid #60a5fa' : '3px solid transparent',
              textDecoration: 'none',
              fontWeight: isActive ? 600 : 400,
              fontSize: 14,
              transition: 'all .15s',
            })}
          >
            <span style={{ fontSize: 16 }}>{l.icon}</span>
            {l.label}
          </NavLink>
        ))}
      </nav>

      <div style={{ padding: '16px 20px', borderTop: '1px solid rgba(255,255,255,.1)', color: 'rgba(255,255,255,.4)', fontSize: 11 }}>
        v1.0.0
      </div>
    </aside>
  )
}

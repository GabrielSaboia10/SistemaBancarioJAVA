import { NavLink } from 'react-router-dom'
import { useAuth } from '../../contexts/AuthContext'
import { Role } from '../../types'

const allLinks: { to: string; label: string; icon: string; roles: Role[] }[] = [
  { to: '/',          label: 'Dashboard',  icon: '▦',  roles: ['ADMIN', 'CLIENTE'] },
  { to: '/pessoas',   label: 'Clientes',   icon: '👤', roles: ['ADMIN'] },
  { to: '/agencias',  label: 'Agências',   icon: '🏦', roles: ['ADMIN'] },
  { to: '/contas',    label: 'Contas',     icon: '💳', roles: ['ADMIN'] },
  { to: '/operacoes', label: 'Operações',  icon: '↔',  roles: ['ADMIN', 'CLIENTE'] },
]

interface Props {
  isOpen: boolean
  onClose: () => void
}

export default function Sidebar({ isOpen, onClose }: Props) {
  const { user, logout, isAdmin } = useAuth()
  const links = allLinks.filter(l => user && l.roles.includes(user.role))

  return (
    <aside className={`sidebar${isOpen ? ' open' : ''}`}>
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
            onClick={onClose}
            style={({ isActive }) => ({
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              padding: '13px 20px',
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

      <div style={{ padding: '16px 20px', borderTop: '1px solid rgba(255,255,255,.1)' }}>
        <div style={{ color: 'rgba(255,255,255,.8)', fontSize: 13, fontWeight: 600, marginBottom: 2 }}>
          {user?.nome}
        </div>
        <div style={{ marginBottom: 12 }}>
          <span style={{
            fontSize: 10,
            fontWeight: 700,
            letterSpacing: '.5px',
            textTransform: 'uppercase',
            padding: '2px 7px',
            borderRadius: 20,
            background: isAdmin ? 'rgba(251,191,36,.2)' : 'rgba(96,165,250,.2)',
            color: isAdmin ? '#fbbf24' : '#60a5fa',
          }}>
            {user?.role}
          </span>
        </div>
        <button
          onClick={logout}
          style={{
            width: '100%',
            padding: '9px 0',
            borderRadius: 'var(--radius)',
            border: '1px solid rgba(255,255,255,.2)',
            background: 'transparent',
            color: 'rgba(255,255,255,.7)',
            fontSize: 13,
            cursor: 'pointer',
          }}
        >
          Sair
        </button>
      </div>
    </aside>
  )
}

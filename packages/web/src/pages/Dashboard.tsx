import { useEffect, useState } from 'react'
import { contasApi } from '../api/contas'
import type { Stats } from '../types'

function StatCard({ label, value, icon, color }: { label: string; value: string; icon: string; color: string }) {
  return (
    <div style={{
      background: '#fff', borderRadius: 12, padding: '20px 24px',
      boxShadow: 'var(--shadow)', border: '1px solid var(--border)',
      display: 'flex', alignItems: 'center', gap: 16,
    }}>
      <div style={{
        width: 48, height: 48, borderRadius: 12, background: color,
        display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 22, flexShrink: 0,
      }}>
        {icon}
      </div>
      <div>
        <div style={{ fontSize: 22, fontWeight: 700, color: 'var(--text)' }}>{value}</div>
        <div style={{ fontSize: 13, color: 'var(--text-muted)', marginTop: 2 }}>{label}</div>
      </div>
    </div>
  )
}

export default function Dashboard() {
  const [stats, setStats] = useState<Stats | null>(null)

  useEffect(() => {
    contasApi.getStats().then(setStats).catch(() => {})
  }, [])

  const fmt = (n: number) => n.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

  return (
    <div>
      <div style={{ marginBottom: 28 }}>
        <h1 style={{ fontSize: 22, fontWeight: 700, color: 'var(--text)' }}>Dashboard</h1>
        <p style={{ color: 'var(--text-muted)', marginTop: 4 }}>Visão geral do sistema bancário</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 16, marginBottom: 32 }}>
        <StatCard label="Total de Clientes"  value={String(stats?.totalPessoas ?? '—')}  icon="👤" color="#dbeafe" />
        <StatCard label="Agências Ativas"    value={String(stats?.totalAgencias ?? '—')} icon="🏦" color="#dcfce7" />
        <StatCard label="Contas Abertas"     value={String(stats?.totalContas ?? '—')}   icon="💳" color="#fef9c3" />
        <StatCard label="Saldo Total"        value={stats ? fmt(stats.saldoTotal) : '—'} icon="💰" color="#fce7f3" />
      </div>

      <div style={{
        background: '#fff', borderRadius: 12, padding: '24px',
        border: '1px solid var(--border)', boxShadow: 'var(--shadow)',
      }}>
        <h2 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16 }}>Acesso Rápido</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 12 }}>
          {[
            { href: '/pessoas',   label: 'Gerenciar Clientes',  bg: '#1a3a5c' },
            { href: '/agencias',  label: 'Gerenciar Agências',  bg: '#0066cc' },
            { href: '/contas',    label: 'Gerenciar Contas',    bg: '#16a34a' },
            { href: '/operacoes', label: 'Operações Bancárias', bg: '#d97706' },
          ].map((item) => (
            <a key={item.href} href={item.href} style={{
              display: 'block', background: item.bg, color: '#fff',
              padding: '14px 16px', borderRadius: 8, fontWeight: 600, fontSize: 13,
              textDecoration: 'none', transition: 'opacity .15s',
            }}
              onMouseEnter={(e) => (e.currentTarget.style.opacity = '.85')}
              onMouseLeave={(e) => (e.currentTarget.style.opacity = '1')}
            >
              {item.label}
            </a>
          ))}
        </div>
      </div>
    </div>
  )
}

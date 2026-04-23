import { useState, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { formatCpf, stripCpf } from '../lib/cpf'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [cpf, setCpf] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(false)

  function handleCpfChange(e: React.ChangeEvent<HTMLInputElement>) {
    setCpf(formatCpf(e.target.value))
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (stripCpf(cpf).length !== 11) { setErro('CPF inválido'); return }
    if (!senha) { setErro('Informe a senha'); return }
    setErro('')
    setCarregando(true)
    try {
      await login(cpf, senha)
      navigate('/', { replace: true })
    } catch (err: any) {
      setErro(err.message ?? 'Erro ao entrar')
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'var(--bg)',
    }}>
      <div style={{
        background: 'var(--surface)',
        borderRadius: 'var(--radius)',
        boxShadow: '0 4px 24px rgba(0,0,0,.12)',
        padding: '40px 36px',
        width: '100%',
        maxWidth: 380,
      }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div style={{ fontSize: 40, marginBottom: 8 }}>🏦</div>
          <h1 style={{ fontSize: 22, fontWeight: 700, color: 'var(--primary)', margin: 0 }}>BancoApp</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: 14, margin: '4px 0 0' }}>Acesso ao sistema bancário</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, marginBottom: 6, color: 'var(--text)' }}>
              CPF
            </label>
            <input
              type="text"
              value={cpf}
              onChange={handleCpfChange}
              placeholder="000.000.000-00"
              maxLength={14}
              style={{
                width: '100%',
                padding: '10px 12px',
                borderRadius: 'var(--radius)',
                border: '1px solid var(--border)',
                fontSize: 15,
                boxSizing: 'border-box',
                outline: 'none',
              }}
              autoFocus
            />
          </div>

          <div className="form-group">
            <label style={{ display: 'block', fontSize: 13, fontWeight: 600, marginBottom: 6, color: 'var(--text)' }}>
              Senha
            </label>
            <input
              type="password"
              value={senha}
              onChange={e => setSenha(e.target.value)}
              placeholder="••••••••"
              style={{
                width: '100%',
                padding: '10px 12px',
                borderRadius: 'var(--radius)',
                border: '1px solid var(--border)',
                fontSize: 15,
                boxSizing: 'border-box',
                outline: 'none',
              }}
            />
          </div>

          {erro && <p className="error-msg" style={{ marginBottom: 16 }}>{erro}</p>}

          <button
            type="submit"
            className="btn btn-primary"
            disabled={carregando}
            style={{ width: '100%', justifyContent: 'center', padding: '11px 0', fontSize: 15 }}
          >
            {carregando ? 'Entrando...' : 'Entrar'}
          </button>
        </form>

        <p style={{ textAlign: 'center', fontSize: 12, color: 'var(--text-muted)', marginTop: 24 }}>
          Clientes: senha inicial = últimos 6 dígitos do CPF
        </p>
      </div>
    </div>
  )
}

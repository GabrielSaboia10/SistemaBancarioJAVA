import { useEffect, useState } from 'react'
import { useContas } from '../hooks/useContas'
import { contasApi } from '../api/contas'
import { useAuth } from '../contexts/AuthContext'
import type { ContaBancaria } from '../types'

type OpType = 'deposito' | 'saque' | 'transferencia'

const fmt = (n: number) => n.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

const opConfig: Record<OpType, { label: string; color: string; icon: string }> = {
  deposito:     { label: 'Depósito',     color: 'var(--success)', icon: '↓' },
  saque:        { label: 'Saque',        color: 'var(--accent)',  icon: '↑' },
  transferencia:{ label: 'Transferência',color: 'var(--warning)', icon: '↔' },
}

export default function Operacoes() {
  const { isCliente } = useAuth()
  const { contas, atualizar } = useContas()
  const [op, setOp] = useState<OpType>('deposito')
  const [origemId, setOrigemId] = useState('')
  const [destinoId, setDestinoId] = useState('')
  const [destinoNumConta, setDestinoNumConta] = useState('')
  const [valor, setValor] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<{ type: 'success' | 'error'; msg: string } | null>(null)

  useEffect(() => {
    if (isCliente && contas.length === 1 && !origemId) {
      setOrigemId(String(contas[0].id))
    }
  }, [contas, isCliente, origemId])

  const contaOrigem: ContaBancaria | undefined = contas.find((c) => c.id === Number(origemId))

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setResult(null)
    try {
      const v = Number(valor)
      let conta: ContaBancaria
      if (op === 'deposito') {
        conta = await contasApi.depositar(Number(origemId), v)
        atualizar(conta)
        setResult({ type: 'success', msg: `Depósito de ${fmt(v)} realizado. Novo saldo: ${fmt(conta.saldo)}` })
      } else if (op === 'saque') {
        conta = await contasApi.sacar(Number(origemId), v)
        atualizar(conta)
        setResult({ type: 'success', msg: `Saque de ${fmt(v)} realizado. Novo saldo: ${fmt(conta.saldo)}` })
      } else {
        if (isCliente) {
          conta = await contasApi.transferirPorNumConta(Number(origemId), Number(destinoNumConta), v)
        } else {
          conta = await contasApi.transferir(Number(origemId), Number(destinoId), v)
        }
        atualizar(conta)
        setResult({ type: 'success', msg: `Transferência de ${fmt(v)} realizada. Novo saldo da origem: ${fmt(conta.saldo)}` })
      }
      setValor('')
    } catch (err) {
      setResult({ type: 'error', msg: (err as Error).message })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div style={{ marginBottom: 28 }}>
        <h1 style={{ fontSize: 22, fontWeight: 700 }}>Operações Bancárias</h1>
        <p style={{ color: 'var(--text-muted)', marginTop: 4 }}>Realize depósitos, saques e transferências</p>
      </div>

      <div style={{ maxWidth: 560 }}>
        <div style={{ display: 'flex', gap: 8, marginBottom: 24 }}>
          {(Object.keys(opConfig) as OpType[]).map((tipo) => (
            <button key={tipo} onClick={() => { setOp(tipo); setResult(null) }}
              style={{
                flex: 1, padding: '12px 8px', borderRadius: 8, fontWeight: 600, fontSize: 13,
                border: `2px solid ${op === tipo ? opConfig[tipo].color : 'var(--border)'}`,
                background: op === tipo ? opConfig[tipo].color : '#fff',
                color: op === tipo ? '#fff' : 'var(--text-muted)',
                cursor: 'pointer', transition: 'all .15s',
              }}
            >
              {opConfig[tipo].icon} {opConfig[tipo].label}
            </button>
          ))}
        </div>

        <div style={{ background: '#fff', borderRadius: 12, padding: 24, border: '1px solid var(--border)', boxShadow: 'var(--shadow)' }}>
          {result && (
            <div style={{
              padding: '12px 16px', borderRadius: 8, marginBottom: 20,
              background: result.type === 'success' ? '#f0fdf4' : '#fef2f2',
              border: `1px solid ${result.type === 'success' ? '#bbf7d0' : '#fecaca'}`,
              color: result.type === 'success' ? '#166534' : '#991b1b',
              fontWeight: 500, fontSize: 13,
            }}>
              {result.type === 'success' ? '✓ ' : '✕ '}{result.msg}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>{op === 'transferencia' ? 'Conta Origem' : 'Conta'}</label>
              {isCliente ? (
                <input
                  value={contaOrigem ? `${contaOrigem.numContaCorrente} — ${contaOrigem.correntista?.nome ?? ''} (${fmt(contaOrigem.saldo)})` : ''}
                  readOnly
                  style={{ background: '#f8fafc', cursor: 'default' }}
                />
              ) : (
                <select value={origemId} onChange={(e) => setOrigemId(e.target.value)} required>
                  <option value="">Selecione a conta...</option>
                  {contas.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.numContaCorrente} — {c.correntista.nome} ({fmt(c.saldo)})
                    </option>
                  ))}
                </select>
              )}
            </div>

            {contaOrigem && (
              <div style={{
                background: '#f8fafc', borderRadius: 8, padding: '10px 14px',
                marginBottom: 16, fontSize: 13, color: 'var(--text-muted)',
              }}>
                Saldo: <strong style={{ color: contaOrigem.saldo < 0 ? 'var(--danger)' : 'var(--success)' }}>{fmt(contaOrigem.saldo)}</strong>
                &nbsp;|&nbsp;Limite: <strong>{fmt(contaOrigem.limiteChequeEspecial)}</strong>
                &nbsp;|&nbsp;Disponível: <strong>{fmt(contaOrigem.saldo + contaOrigem.limiteChequeEspecial)}</strong>
              </div>
            )}

            {op === 'transferencia' && (
              <div className="form-group">
                <label>Conta Destino</label>
                {isCliente ? (
                  <input
                    type="number"
                    value={destinoNumConta}
                    onChange={(e) => setDestinoNumConta(e.target.value)}
                    placeholder="Número da conta destino"
                    required
                  />
                ) : (
                  <select value={destinoId} onChange={(e) => setDestinoId(e.target.value)} required>
                    <option value="">Selecione a conta destino...</option>
                    {contas.filter((c) => c.id !== Number(origemId)).map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.numContaCorrente} — {c.correntista.nome}
                      </option>
                    ))}
                  </select>
                )}
              </div>
            )}

            <div className="form-group">
              <label>Valor (R$)</label>
              <input type="number" step="0.01" min="0.01" value={valor}
                onChange={(e) => setValor(e.target.value)} placeholder="0,00" required />
            </div>

            <button type="submit" disabled={loading} style={{
              width: '100%', padding: '12px', borderRadius: 8, fontWeight: 700, fontSize: 15,
              background: opConfig[op].color, color: '#fff', border: 'none', cursor: 'pointer',
              opacity: loading ? .7 : 1, transition: 'opacity .15s',
            }}>
              {loading ? 'Processando...' : `Confirmar ${opConfig[op].label}`}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}

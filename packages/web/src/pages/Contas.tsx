import { useState } from 'react'
import { useContas } from '../hooks/useContas'
import { usePessoas } from '../hooks/usePessoas'
import { useAgencias } from '../hooks/useAgencias'
import Table from '../components/Table/Table'
import Modal from '../components/Modal/Modal'
import type { ContaBancaria } from '../types'

const fmt = (n: number) => n.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })

export default function Contas() {
  const { contas, loading, error, create, remove } = useContas()
  const { pessoas } = usePessoas()
  const { agencias } = useAgencias()
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState({ numContaCorrente: '', limiteChequeEspecial: '', saldo: '', pessoaId: '', agenciaId: '' })
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      await create({
        numContaCorrente: Number(form.numContaCorrente),
        limiteChequeEspecial: Number(form.limiteChequeEspecial),
        saldo: Number(form.saldo),
        pessoaId: Number(form.pessoaId),
        agenciaId: Number(form.agenciaId),
      })
      setModal(false)
    } catch (err) {
      setFormError((err as Error).message)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (c: ContaBancaria) => {
    if (!confirm(`Excluir conta ${c.numContaCorrente}?`)) return
    try { await remove(c.id) } catch (err) { alert((err as Error).message) }
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700 }}>Contas Bancárias</h1>
          <p style={{ color: 'var(--text-muted)', marginTop: 4 }}>{contas.length} conta(s) ativa(s)</p>
        </div>
        <button className="btn btn-primary" onClick={() => { setForm({ numContaCorrente: '', limiteChequeEspecial: '', saldo: '', pessoaId: '', agenciaId: '' }); setFormError(''); setModal(true) }}>
          + Nova Conta
        </button>
      </div>

      {error && <div className="error-msg">{error}</div>}

      <Table
        data={contas}
        loading={loading}
        columns={[
          { key: 'numContaCorrente', label: 'Nº Conta', render: (c) => <span className="badge badge-blue">{c.numContaCorrente}</span> },
          { key: 'correntista', label: 'Titular', render: (c) => c.correntista.nome },
          { key: 'agencia', label: 'Agência', render: (c) => `AG ${String(c.agencia.numero).padStart(4, '0')} — ${c.agencia.cidade}` },
          { key: 'saldo', label: 'Saldo', render: (c) => (
            <span style={{ color: c.saldo < 0 ? 'var(--danger)' : 'var(--success)', fontWeight: 600 }}>
              {fmt(c.saldo)}
            </span>
          )},
          { key: 'limiteChequeEspecial', label: 'Limite', render: (c) => fmt(c.limiteChequeEspecial) },
        ]}
        onDelete={handleDelete}
        emptyMessage="Nenhuma conta cadastrada."
      />

      <Modal isOpen={modal} onClose={() => setModal(false)} title="Nova Conta Bancária">
        <form onSubmit={handleSubmit}>
          {formError && <div className="error-msg">{formError}</div>}
          <div className="form-group">
            <label>Número da Conta</label>
            <input type="number" value={form.numContaCorrente} onChange={(e) => setForm({ ...form, numContaCorrente: e.target.value })} placeholder="1000 a 99999" required />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="form-group">
              <label>Saldo Inicial (R$)</label>
              <input type="number" step="0.01" value={form.saldo} onChange={(e) => setForm({ ...form, saldo: e.target.value })} placeholder="0.00" />
            </div>
            <div className="form-group">
              <label>Limite Especial (R$)</label>
              <input type="number" step="0.01" value={form.limiteChequeEspecial} onChange={(e) => setForm({ ...form, limiteChequeEspecial: e.target.value })} placeholder="0.00" />
            </div>
          </div>
          <div className="form-group">
            <label>Titular</label>
            <select value={form.pessoaId} onChange={(e) => setForm({ ...form, pessoaId: e.target.value })} required>
              <option value="">Selecione o titular...</option>
              {pessoas.map((p) => <option key={p.id} value={p.id}>{p.nome} — {p.cpf}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>Agência</label>
            <select value={form.agenciaId} onChange={(e) => setForm({ ...form, agenciaId: e.target.value })} required>
              <option value="">Selecione a agência...</option>
              {agencias.map((a) => <option key={a.id} value={a.id}>AG {String(a.numero).padStart(4, '0')} — {a.cidade}</option>)}
            </select>
          </div>
          <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 8 }}>
            <button type="button" className="btn btn-ghost" onClick={() => setModal(false)}>Cancelar</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Salvando...' : 'Abrir Conta'}</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

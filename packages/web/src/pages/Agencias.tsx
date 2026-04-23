import { useState } from 'react'
import { useAgencias } from '../hooks/useAgencias'
import Table from '../components/Table/Table'
import Modal from '../components/Modal/Modal'
import type { AgenciaBancaria } from '../types'

type Form = { numero: string; endereco: string; cidade: string }
const empty: Form = { numero: '', endereco: '', cidade: '' }

export default function Agencias() {
  const { agencias, loading, error, create, update, remove } = useAgencias()
  const [modal, setModal] = useState<'create' | 'edit' | null>(null)
  const [selected, setSelected] = useState<AgenciaBancaria | null>(null)
  const [form, setForm] = useState<Form>(empty)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  const openCreate = () => { setForm(empty); setFormError(''); setModal('create') }
  const openEdit = (a: AgenciaBancaria) => {
    setSelected(a)
    setForm({ numero: String(a.numero), endereco: a.endereco, cidade: a.cidade })
    setFormError('')
    setModal('edit')
  }
  const closeModal = () => { setModal(null); setSelected(null) }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSaving(true)
    setFormError('')
    try {
      if (modal === 'create') {
        await create({ numero: Number(form.numero), endereco: form.endereco, cidade: form.cidade })
      } else if (selected) {
        await update(selected.id, { endereco: form.endereco, cidade: form.cidade })
      }
      closeModal()
    } catch (err) {
      setFormError((err as Error).message)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (a: AgenciaBancaria) => {
    if (!confirm(`Excluir agência ${a.numero}?`)) return
    try { await remove(a.id) } catch (err) { alert((err as Error).message) }
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700 }}>Agências Bancárias</h1>
          <p style={{ color: 'var(--text-muted)', marginTop: 4 }}>{agencias.length} agência(s) cadastrada(s)</p>
        </div>
        <button className="btn btn-primary" onClick={openCreate}>+ Nova Agência</button>
      </div>

      {error && <div className="error-msg">{error}</div>}

      <Table
        data={agencias}
        loading={loading}
        columns={[
          { key: 'numero',   label: 'Número', render: (a) => <span className="badge badge-blue">AG {a.numero.toString().padStart(4, '0')}</span> },
          { key: 'cidade',   label: 'Cidade' },
          { key: 'endereco', label: 'Endereço' },
        ]}
        onEdit={openEdit}
        onDelete={handleDelete}
        emptyMessage="Nenhuma agência cadastrada."
      />

      <Modal isOpen={modal !== null} onClose={closeModal} title={modal === 'create' ? 'Nova Agência' : 'Editar Agência'}>
        <form onSubmit={handleSubmit}>
          {formError && <div className="error-msg">{formError}</div>}
          <div className="form-group">
            <label>Número</label>
            <input type="number" value={form.numero} onChange={(e) => setForm({ ...form, numero: e.target.value })}
              placeholder="1 a 10000" disabled={modal === 'edit'} required />
          </div>
          <div className="form-group">
            <label>Endereço</label>
            <input value={form.endereco} onChange={(e) => setForm({ ...form, endereco: e.target.value })}
              placeholder="Rua, número" required />
          </div>
          <div className="form-group">
            <label>Cidade</label>
            <input value={form.cidade} onChange={(e) => setForm({ ...form, cidade: e.target.value })}
              placeholder="Cidade" required />
          </div>
          <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 8 }}>
            <button type="button" className="btn btn-ghost" onClick={closeModal}>Cancelar</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Salvando...' : 'Salvar'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}

import { useState } from 'react'
import { usePessoas } from '../hooks/usePessoas'
import Table from '../components/Table/Table'
import Modal from '../components/Modal/Modal'
import type { Pessoa } from '../types'

type Form = { cpf: string; nome: string; idade: string }
const empty: Form = { cpf: '', nome: '', idade: '' }

export default function Pessoas() {
  const { pessoas, loading, error, create, update, remove } = usePessoas()
  const [modal, setModal] = useState<'create' | 'edit' | null>(null)
  const [selected, setSelected] = useState<Pessoa | null>(null)
  const [form, setForm] = useState<Form>(empty)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  const openCreate = () => { setForm(empty); setFormError(''); setModal('create') }
  const openEdit = (p: Pessoa) => {
    setSelected(p)
    setForm({ cpf: p.cpf, nome: p.nome, idade: String(p.idade) })
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
        await create({ cpf: form.cpf, nome: form.nome, idade: Number(form.idade) })
      } else if (selected) {
        await update(selected.id, { nome: form.nome, idade: Number(form.idade) })
      }
      closeModal()
    } catch (err) {
      setFormError((err as Error).message)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (p: Pessoa) => {
    if (!confirm(`Excluir ${p.nome}?`)) return
    try { await remove(p.id) } catch (err) { alert((err as Error).message) }
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700 }}>Clientes</h1>
          <p style={{ color: 'var(--text-muted)', marginTop: 4 }}>{pessoas.length} cliente(s) cadastrado(s)</p>
        </div>
        <button className="btn btn-primary" onClick={openCreate}>+ Novo Cliente</button>
      </div>

      {error && <div className="error-msg">{error}</div>}

      <Table
        data={pessoas}
        loading={loading}
        columns={[
          { key: 'cpf',   label: 'CPF' },
          { key: 'nome',  label: 'Nome' },
          { key: 'idade', label: 'Idade', render: (p) => `${p.idade} anos` },
          { key: 'createdAt', label: 'Cadastro', render: (p) => new Date(p.createdAt).toLocaleDateString('pt-BR') },
        ]}
        onEdit={openEdit}
        onDelete={handleDelete}
        emptyMessage="Nenhum cliente cadastrado."
      />

      <Modal isOpen={modal !== null} onClose={closeModal} title={modal === 'create' ? 'Novo Cliente' : 'Editar Cliente'}>
        <form onSubmit={handleSubmit}>
          {formError && <div className="error-msg">{formError}</div>}
          <div className="form-group">
            <label>CPF</label>
            <input value={form.cpf} onChange={(e) => setForm({ ...form, cpf: e.target.value })}
              placeholder="123.456.789-09" disabled={modal === 'edit'} required />
          </div>
          <div className="form-group">
            <label>Nome</label>
            <input value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })}
              placeholder="Nome completo" required />
          </div>
          <div className="form-group">
            <label>Idade</label>
            <input type="number" value={form.idade} onChange={(e) => setForm({ ...form, idade: e.target.value })}
              placeholder="0" min={0} max={150} required />
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

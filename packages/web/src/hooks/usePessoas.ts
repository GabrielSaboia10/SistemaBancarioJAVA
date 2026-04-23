import { useState, useEffect, useCallback } from 'react'
import { pessoasApi } from '../api/pessoas'
import type { Pessoa, CreatePessoaDTO, UpdatePessoaDTO } from '../types'

export function usePessoas() {
  const [pessoas, setPessoas] = useState<Pessoa[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchAll = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setPessoas(await pessoasApi.getAll())
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchAll() }, [fetchAll])

  const create = async (data: CreatePessoaDTO) => {
    const nova = await pessoasApi.create(data)
    setPessoas((prev) => [...prev, nova].sort((a, b) => a.nome.localeCompare(b.nome)))
    return nova
  }

  const update = async (id: number, data: UpdatePessoaDTO) => {
    const atualizada = await pessoasApi.update(id, data)
    setPessoas((prev) => prev.map((p) => (p.id === id ? atualizada : p)))
    return atualizada
  }

  const remove = async (id: number) => {
    await pessoasApi.remove(id)
    setPessoas((prev) => prev.filter((p) => p.id !== id))
  }

  return { pessoas, loading, error, fetchAll, create, update, remove }
}

import { useState, useEffect, useCallback } from 'react'
import { contasApi } from '../api/contas'
import type { ContaBancaria, CreateContaDTO } from '../types'

export function useContas() {
  const [contas, setContas] = useState<ContaBancaria[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchAll = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setContas(await contasApi.getAll())
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchAll() }, [fetchAll])

  const create = async (data: CreateContaDTO) => {
    const nova = await contasApi.create(data)
    setContas((prev) => [...prev, nova].sort((a, b) => a.numContaCorrente - b.numContaCorrente))
    return nova
  }

  const remove = async (id: number) => {
    await contasApi.remove(id)
    setContas((prev) => prev.filter((c) => c.id !== id))
  }

  const atualizar = (conta: ContaBancaria) => {
    setContas((prev) => prev.map((c) => (c.id === conta.id ? conta : c)))
  }

  return { contas, loading, error, fetchAll, create, remove, atualizar }
}

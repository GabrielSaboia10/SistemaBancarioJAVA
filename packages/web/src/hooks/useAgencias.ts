import { useState, useEffect, useCallback } from 'react'
import { agenciasApi } from '../api/agencias'
import type { AgenciaBancaria, CreateAgenciaDTO, UpdateAgenciaDTO } from '../types'

export function useAgencias() {
  const [agencias, setAgencias] = useState<AgenciaBancaria[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const fetchAll = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setAgencias(await agenciasApi.getAll())
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchAll() }, [fetchAll])

  const create = async (data: CreateAgenciaDTO) => {
    const nova = await agenciasApi.create(data)
    setAgencias((prev) => [...prev, nova].sort((a, b) => a.numero - b.numero))
    return nova
  }

  const update = async (id: number, data: UpdateAgenciaDTO) => {
    const atualizada = await agenciasApi.update(id, data)
    setAgencias((prev) => prev.map((a) => (a.id === id ? atualizada : a)))
    return atualizada
  }

  const remove = async (id: number) => {
    await agenciasApi.remove(id)
    setAgencias((prev) => prev.filter((a) => a.id !== id))
  }

  return { agencias, loading, error, fetchAll, create, update, remove }
}

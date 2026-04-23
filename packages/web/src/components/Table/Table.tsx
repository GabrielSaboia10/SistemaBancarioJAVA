interface Column<T> {
  key: keyof T | string
  label: string
  render?: (row: T) => React.ReactNode
}

interface TableProps<T> {
  data: T[]
  columns: Column<T>[]
  onEdit?: (row: T) => void
  onDelete?: (row: T) => void
  loading?: boolean
  emptyMessage?: string
}

export default function Table<T extends { id: number }>({
  data, columns, onEdit, onDelete, loading, emptyMessage = 'Nenhum registro encontrado.'
}: TableProps<T>) {
  if (loading) return (
    <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>Carregando...</div>
  )

  return (
    <div style={{ overflowX: 'auto', borderRadius: 'var(--radius)', border: '1px solid var(--border)', background: '#fff' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ background: '#f8fafc', borderBottom: '1px solid var(--border)' }}>
            {columns.map((col) => (
              <th key={String(col.key)} style={{
                padding: '12px 16px', textAlign: 'left', fontWeight: 600,
                fontSize: 12, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '.5px',
              }}>
                {col.label}
              </th>
            ))}
            {(onEdit || onDelete) && (
              <th style={{ padding: '12px 16px', width: 100 }} />
            )}
          </tr>
        </thead>
        <tbody>
          {data.length === 0 ? (
            <tr>
              <td colSpan={columns.length + 1} style={{ padding: 32, textAlign: 'center', color: 'var(--text-muted)' }}>
                {emptyMessage}
              </td>
            </tr>
          ) : data.map((row, i) => (
            <tr key={row.id} style={{
              borderBottom: i < data.length - 1 ? '1px solid var(--border)' : 'none',
              transition: 'background .1s',
            }}
              onMouseEnter={(e) => (e.currentTarget.style.background = '#f8fafc')}
              onMouseLeave={(e) => (e.currentTarget.style.background = '')}
            >
              {columns.map((col) => (
                <td key={String(col.key)} style={{ padding: '12px 16px', fontSize: 14, color: 'var(--text)' }}>
                  {col.render ? col.render(row) : String((row as Record<string, unknown>)[String(col.key)] ?? '')}
                </td>
              ))}
              {(onEdit || onDelete) && (
                <td style={{ padding: '8px 16px' }}>
                  <div style={{ display: 'flex', gap: 6 }}>
                    {onEdit && (
                      <button className="btn btn-ghost btn-sm" onClick={() => onEdit(row)}>Editar</button>
                    )}
                    {onDelete && (
                      <button className="btn btn-danger btn-sm" onClick={() => onDelete(row)}>Excluir</button>
                    )}
                  </div>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

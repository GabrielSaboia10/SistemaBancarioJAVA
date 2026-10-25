import { useEffect, useState } from 'react';

/** Devolve o valor só depois que ele para de mudar por `ms` (ex.: busca enquanto digita). */
export function useAtrasado<T>(valor: T, ms = 300): T {
  const [atrasado, setAtrasado] = useState(valor);
  useEffect(() => {
    const timer = setTimeout(() => setAtrasado(valor), ms);
    return () => clearTimeout(timer);
  }, [valor, ms]);
  return atrasado;
}

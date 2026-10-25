import { useCallback, useEffect, useRef, useState } from 'react';
import { mensagemDeErro } from '@/api/cliente';
import type { Page } from '@/api/tipos';

/**
 * Lista paginada da API com "carregar mais" e "puxar para atualizar".
 * Recarrega do zero sempre que `carregar` mudar (ex.: novo termo de busca).
 */
export function usePaginado<T>(carregar: (pagina: number) => Promise<Page<T>>) {
  const [itens, setItens] = useState<T[]>([]);
  const [pagina, setPagina] = useState(0);
  const [total, setTotal] = useState(0);
  const [temMais, setTemMais] = useState(false);
  const [carregando, setCarregando] = useState(true);
  const [atualizando, setAtualizando] = useState(false);
  const [erro, setErro] = useState('');
  const versao = useRef(0);

  const buscar = useCallback(
    async (p: number, modo: 'inicial' | 'mais' | 'atualizar') => {
      const minhaVersao = modo === 'mais' ? versao.current : ++versao.current;
      if (modo === 'atualizar') setAtualizando(true);
      else setCarregando(true);
      try {
        const res = await carregar(p);
        if (minhaVersao !== versao.current) return; // resposta de uma busca antiga
        setItens((atual) => (p === 0 ? res.content : [...atual, ...res.content]));
        setPagina(res.page);
        setTotal(res.totalElements);
        setTemMais(res.page + 1 < res.totalPages);
        setErro('');
      } catch (e) {
        if (minhaVersao === versao.current) setErro(mensagemDeErro(e));
      } finally {
        if (minhaVersao === versao.current) {
          setCarregando(false);
          setAtualizando(false);
        }
      }
    },
    [carregar],
  );

  useEffect(() => {
    buscar(0, 'inicial');
  }, [buscar]);

  return {
    itens,
    setItens,
    total,
    temMais,
    carregando,
    atualizando,
    erro,
    carregarMais: () => buscar(pagina + 1, 'mais'),
    atualizar: () => buscar(0, 'atualizar'),
  };
}

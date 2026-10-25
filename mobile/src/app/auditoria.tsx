import { useCallback, useState } from 'react';
import { View } from 'react-native';
import { auditoria } from '@/api/endpoints';
import type { EntidadeAuditada } from '@/api/tipos';
import { Alerta, Botao, Carregando, Cartao, Chips, Tela, Texto, Vazio } from '@/components/ui';
import { usePaginado } from '@/hooks/usePaginado';
import { useCores } from '@/theme';
import { dataHora } from '@/utils/formato';

const ACOES: Record<string, string> = {
  CRIAR: 'Cadastro',
  ATUALIZAR: 'Alteração',
  EXCLUIR: 'Exclusão',
  BLOQUEAR: 'Bloqueio',
  DESBLOQUEAR: 'Liberação',
  REDEFINIR_SENHA: 'Nova senha',
  DEPOSITO: 'Depósito',
  SAQUE: 'Saque',
  TRANSFERENCIA: 'Transferência',
};

type Filtro = EntidadeAuditada | 'TUDO';
const FILTROS: { valor: Filtro; rotulo: string }[] = [
  { valor: 'TUDO', rotulo: 'Tudo' },
  { valor: 'PESSOA', rotulo: 'Clientes' },
  { valor: 'AGENCIA', rotulo: 'Agências' },
  { valor: 'CONTA', rotulo: 'Contas' },
];

/** Quem fez cada ação administrativa, e quando. */
export default function Auditoria() {
  const c = useCores();
  const [filtro, setFiltro] = useState<Filtro>('TUDO');
  const carregar = useCallback(
    (pagina: number) => auditoria.listar(pagina, filtro === 'TUDO' ? '' : filtro),
    [filtro],
  );
  const lista = usePaginado(carregar);

  return (
    <Tela aoAtualizar={lista.atualizar} atualizando={lista.atualizando}>
      <Chips opcoes={FILTROS} valor={filtro} aoMudar={setFiltro} />
      {lista.erro ? <Alerta tipo="erro">{lista.erro}</Alerta> : null}

      <Cartao>
        {lista.carregando && lista.itens.length === 0 ? (
          <Carregando />
        ) : lista.itens.length === 0 ? (
          <Vazio>Nenhuma ação registrada.</Vazio>
        ) : (
          lista.itens.map((r) => (
            <View key={r.id} style={{ paddingVertical: 10, gap: 2, borderBottomWidth: 1, borderColor: c.borda }}>
              <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
                <Texto forte>{ACOES[r.acao] ?? r.acao}</Texto>
                <Texto suave tamanho={12}>{dataHora(r.dataHora)}</Texto>
              </View>
              <Texto>{r.descricao}</Texto>
              <Texto suave tamanho={12}>Por {r.usuarioCpf ?? 'Sistema'}</Texto>
            </View>
          ))
        )}
        {lista.temMais ? <Botao titulo="Carregar mais" variante="secundario" aoPressionar={lista.carregarMais} /> : null}
      </Cartao>
    </Tela>
  );
}

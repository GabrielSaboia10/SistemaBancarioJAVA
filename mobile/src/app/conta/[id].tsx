import { router, Stack, useFocusEffect, useLocalSearchParams } from 'expo-router';
import { useCallback, useState } from 'react';
import { View } from 'react-native';
import { mensagemDeErro } from '@/api/cliente';
import { agencias as apiAgencias, contas } from '@/api/endpoints';
import type { Agencia, Conta } from '@/api/tipos';
import { useSessao } from '@/auth/AuthContext';
import { BuscaSelect } from '@/components/BuscaSelect';
import { avisar, confirmar } from '@/components/dialogos';
import { Alerta, Botao, Campo, Carregando, Cartao, Chips, Folha, ItemLista, Rotulo, Tela, Texto, Vazio } from '@/components/ui';
import { usePaginado } from '@/hooks/usePaginado';
import { useCores } from '@/theme';
import { compartilharTexto } from '@/utils/compartilhar';
import { agencia, brl, dataHora, diasAtras, ehEntrada, lerValor, ROTULO_MOVIMENTACAO } from '@/utils/formato';

type Periodo = '7' | '30' | '90' | 'tudo';
const PERIODOS: { valor: Periodo; rotulo: string }[] = [
  { valor: '7', rotulo: '7 dias' },
  { valor: '30', rotulo: '30 dias' },
  { valor: '90', rotulo: '90 dias' },
  { valor: 'tudo', rotulo: 'Tudo' },
];

const buscarAgencias = (termo: string) => apiAgencias.listar(0, termo).then((p) => p.content);
const descreverAgencia = (a: Pick<Agencia, 'numero' | 'cidade'>) => ({ titulo: `${agencia(a.numero)} — ${a.cidade}` });

export default function DetalheConta() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const contaId = Number(id);
  const { isAdmin } = useSessao();
  const c = useCores();

  const [conta, setConta] = useState<Conta | null>(null);
  const [erro, setErro] = useState('');
  const [periodo, setPeriodo] = useState<Periodo>('30');
  const [exportando, setExportando] = useState(false);
  const de = periodo === 'tudo' ? undefined : diasAtras(Number(periodo));

  const carregarExtrato = useCallback((pagina: number) => contas.extrato(contaId, pagina, de), [contaId, de]);
  const extrato = usePaginado(carregarExtrato);

  const carregarConta = useCallback(() => {
    contas.buscar(contaId).then(setConta).catch((e) => setErro(mensagemDeErro(e)));
  }, [contaId]);

  // Volta de uma operação: atualiza saldo e extrato
  useFocusEffect(
    useCallback(() => {
      carregarConta();
      extrato.atualizar();
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [carregarConta]),
  );

  async function exportar() {
    if (!conta) return;
    setExportando(true);
    try {
      const csv = await contas.extratoCsv(contaId, de);
      await compartilharTexto(`extrato-conta-${conta.numero}.csv`, csv);
    } catch (e) {
      avisar('Não foi possível exportar', mensagemDeErro(e));
    } finally {
      setExportando(false);
    }
  }

  if (erro) return <Tela><Alerta tipo="erro">{erro}</Alerta></Tela>;
  if (!conta) return <Carregando />;

  return (
    <Tela aoAtualizar={() => { carregarConta(); extrato.atualizar(); }} atualizando={extrato.atualizando}>
      <Stack.Screen options={{ title: `Conta ${conta.numero}` }} />

      <Cartao>
        <Texto suave>
          {conta.correntista.nome} · {agencia(conta.agencia.numero)} — {conta.agencia.cidade}
        </Texto>
        <View style={{ gap: 2 }}>
          <Rotulo>Saldo</Rotulo>
          <Texto forte tamanho={28} cor={conta.saldo < 0 ? c.perigo : c.texto}>{brl(conta.saldo)}</Texto>
        </View>
        <View style={{ flexDirection: 'row', gap: 24 }}>
          <View><Rotulo>Cheque especial</Rotulo><Texto>{brl(conta.limiteChequeEspecial)}</Texto></View>
          <View><Rotulo>Disponível</Rotulo><Texto>{brl(conta.saldoDisponivel)}</Texto></View>
        </View>
        <Botao
          titulo="Movimentar"
          icone="swap-horizontal-outline"
          aoPressionar={() => router.push({ pathname: '/operacoes', params: { conta: String(conta.id) } })}
        />
      </Cartao>

      {isAdmin ? <AcoesAdmin conta={conta} aoMudar={setConta} /> : null}

      <Cartao>
        <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
          <Texto forte>Extrato</Texto>
          <Botao pequeno variante="secundario" icone="share-outline" titulo="Exportar" aoPressionar={exportar} carregando={exportando} />
        </View>
        <Chips opcoes={PERIODOS} valor={periodo} aoMudar={setPeriodo} />

        {extrato.carregando && extrato.itens.length === 0 ? (
          <Carregando />
        ) : extrato.itens.length === 0 ? (
          <Vazio>{periodo === 'tudo' ? 'Nenhuma movimentação ainda.' : 'Nenhuma movimentação no período.'}</Vazio>
        ) : (
          extrato.itens.map((m) => {
            const entrada = ehEntrada(m.tipo);
            const contraparte = m.numeroContaContraparte
              ? ` · ${m.tipo === 'TRANSFERENCIA_ENVIADA' ? 'para' : 'de'} conta ${m.numeroContaContraparte}`
              : '';
            return (
              <ItemLista
                key={m.id}
                titulo={ROTULO_MOVIMENTACAO[m.tipo]}
                subtitulo={`${dataHora(m.dataHora)}${contraparte}`}
                direita={
                  <View style={{ alignItems: 'flex-end' }}>
                    <Texto forte cor={entrada ? c.sucesso : c.perigo}>{`${entrada ? '+' : '−'} ${brl(m.valor)}`}</Texto>
                    <Texto suave tamanho={12}>Saldo {brl(m.saldoApos)}</Texto>
                  </View>
                }
              />
            );
          })
        )}
        {extrato.temMais ? <Botao titulo="Carregar mais" variante="secundario" aoPressionar={extrato.carregarMais} carregando={extrato.carregando} /> : null}
      </Cartao>
    </Tela>
  );
}

/** Admin: alterar limite/agência e encerrar a conta. */
function AcoesAdmin({ conta, aoMudar }: { conta: Conta; aoMudar: (c: Conta) => void }) {
  const [editando, setEditando] = useState(false);
  const [limite, setLimite] = useState('');
  const [agenciaEscolhida, setAgenciaEscolhida] = useState<Pick<Agencia, 'id' | 'numero' | 'cidade'> | null>(null);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState('');

  function abrir() {
    setLimite(String(conta.limiteChequeEspecial).replace('.', ','));
    setAgenciaEscolhida(conta.agencia);
    setErro('');
    setEditando(true);
  }

  async function salvar() {
    const valor = lerValor(limite);
    if (!(valor >= 0) || valor > 30000 || !agenciaEscolhida) {
      setErro('O limite deve ser de R$ 0 a R$ 30.000');
      return;
    }
    setSalvando(true);
    try {
      aoMudar(await contas.atualizar(conta.id, { limiteChequeEspecial: valor, agenciaId: agenciaEscolhida.id }));
      setEditando(false);
    } catch (e) {
      setErro(mensagemDeErro(e));
    } finally {
      setSalvando(false);
    }
  }

  async function encerrar() {
    if (!(await confirmar('Encerrar conta', `Encerrar a conta ${conta.numero}? Só é possível com saldo zero.`, 'Encerrar'))) return;
    try {
      await contas.encerrar(conta.id);
      router.back();
    } catch (e) {
      avisar('Não foi possível encerrar', mensagemDeErro(e));
    }
  }

  return (
    <View style={{ flexDirection: 'row', gap: 8 }}>
      <View style={{ flex: 1 }}><Botao titulo="Editar" variante="secundario" icone="create-outline" aoPressionar={abrir} /></View>
      <View style={{ flex: 1 }}><Botao titulo="Encerrar" variante="perigo" icone="close-circle-outline" aoPressionar={encerrar} /></View>

      <Folha visivel={editando} titulo={`Editar conta ${conta.numero}`} aoFechar={() => setEditando(false)}>
        <Campo rotulo="Limite do cheque especial (R$)" value={limite} onChangeText={setLimite} keyboardType="decimal-pad" />
        <BuscaSelect
          rotulo="Agência"
          placeholder="Número ou cidade da agência"
          valor={agenciaEscolhida}
          buscar={buscarAgencias}
          descrever={descreverAgencia}
          aoEscolher={setAgenciaEscolhida}
        />
        {erro ? <Alerta tipo="erro">{erro}</Alerta> : null}
        <Botao titulo="Salvar" aoPressionar={salvar} carregando={salvando} />
      </Folha>
    </View>
  );
}

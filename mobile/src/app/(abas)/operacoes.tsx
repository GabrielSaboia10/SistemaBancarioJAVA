import { Ionicons } from '@expo/vector-icons';
import { router, useLocalSearchParams } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { Pressable, View } from 'react-native';
import { mensagemDeErro } from '@/api/cliente';
import { contas } from '@/api/endpoints';
import type { Conta } from '@/api/tipos';
import { useSessao } from '@/auth/AuthContext';
import { BuscaSelect } from '@/components/BuscaSelect';
import { Abas, Alerta, Botao, Campo, Carregando, Cartao, Rotulo, Tela, Texto, Vazio } from '@/components/ui';
import { useCores } from '@/theme';
import { agencia, brl, lerValor } from '@/utils/formato';

type Operacao = 'depositar' | 'sacar' | 'transferir';

const OPERACOES: { valor: Operacao; rotulo: string }[] = [
  { valor: 'depositar', rotulo: 'Depósito' },
  { valor: 'sacar', rotulo: 'Saque' },
  { valor: 'transferir', rotulo: 'Transferência' },
];

const buscarContas = (termo: string) => contas.listar(0, termo, 10).then((p) => p.content);
const descreverConta = (c: Conta) => ({
  titulo: `${c.numero} — ${c.correntista.nome}`,
  subtitulo: `${agencia(c.agencia.numero)} · saldo ${brl(c.saldo)}`,
});

export default function Operacoes() {
  const { isAdmin } = useSessao();
  const c = useCores();
  const { conta: contaDaRota } = useLocalSearchParams<{ conta?: string }>();

  const [operacao, setOperacao] = useState<Operacao>('depositar');
  const [minhasContas, setMinhasContas] = useState<Conta[]>([]);
  const [carregando, setCarregando] = useState(!isAdmin);
  const [origem, setOrigem] = useState<Conta | null>(null);
  const [destino, setDestino] = useState<Conta | null>(null);
  const [numeroDestino, setNumeroDestino] = useState('');
  const [valor, setValor] = useState('');
  const [tentou, setTentou] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [resultado, setResultado] = useState<{ tipo: 'sucesso' | 'erro'; texto: string } | null>(null);

  // Cliente: escolhe entre as próprias contas (poucas). Admin: busca qualquer conta.
  useEffect(() => {
    if (isAdmin) return;
    contas
      .listar(0, '', 50)
      .then((p) => {
        setMinhasContas(p.content);
        if (p.content.length === 1) setOrigem(p.content[0]);
      })
      .catch((e) => setResultado({ tipo: 'erro', texto: mensagemDeErro(e) }))
      .finally(() => setCarregando(false));
  }, [isAdmin]);

  // Aberta a partir da tela da conta: já vem com a conta escolhida
  useEffect(() => {
    if (!contaDaRota) return;
    contas.buscar(Number(contaDaRota)).then(setOrigem).catch(() => {});
  }, [contaDaRota]);

  const numeroDoDestino = isAdmin ? destino?.numero : Number(numeroDestino);
  const valorNumerico = lerValor(valor);
  const valorInvalido = !(valorNumerico > 0);
  const destinoInvalido = operacao === 'transferir' && !numeroDoDestino;

  const confirmar = useCallback(async () => {
    setTentou(true);
    if (!origem || valorInvalido || destinoInvalido) return;
    setEnviando(true);
    setResultado(null);
    try {
      const atualizada =
        operacao === 'depositar'
          ? await contas.depositar(origem.id, valorNumerico)
          : operacao === 'sacar'
            ? await contas.sacar(origem.id, valorNumerico)
            : await contas.transferir(origem.id, numeroDoDestino!, valorNumerico);
      setOrigem(atualizada);
      setMinhasContas((l) => l.map((x) => (x.id === atualizada.id ? atualizada : x)));
      const nome = OPERACOES.find((o) => o.valor === operacao)!.rotulo;
      const realizada = operacao === 'transferir' ? 'realizada' : 'realizado';
      setResultado({ tipo: 'sucesso', texto: `${nome} de ${brl(valorNumerico)} ${realizada}. Novo saldo: ${brl(atualizada.saldo)}` });
      setValor('');
      setNumeroDestino('');
      setDestino(null);
      setTentou(false);
    } catch (e) {
      setResultado({ tipo: 'erro', texto: mensagemDeErro(e) });
    } finally {
      setEnviando(false);
    }
  }, [origem, valorInvalido, destinoInvalido, operacao, valorNumerico, numeroDoDestino]);

  if (carregando) return <Carregando />;

  if (!isAdmin && minhasContas.length === 0) {
    return (
      <Tela>
        <Vazio>Nenhuma conta vinculada ao seu usuário.</Vazio>
      </Tela>
    );
  }

  return (
    <Tela>
      <Cartao estilo={{ gap: 16 }}>
        <Abas opcoes={OPERACOES} valor={operacao} aoMudar={(o) => { setOperacao(o); setResultado(null); }} />

        {resultado ? <Alerta tipo={resultado.tipo}>{resultado.texto}</Alerta> : null}

        {isAdmin ? (
          <BuscaSelect
            rotulo={operacao === 'transferir' ? 'Conta de origem' : 'Conta'}
            placeholder="Número da conta ou nome do titular"
            valor={origem}
            buscar={buscarContas}
            descrever={descreverConta}
            aoEscolher={(conta) => { setOrigem(conta); setResultado(null); }}
          />
        ) : (
          <View style={{ gap: 6 }}>
            <Texto forte tamanho={13}>{operacao === 'transferir' ? 'Conta de origem' : 'Conta'}</Texto>
            {minhasContas.map((conta) => {
              const escolhida = origem?.id === conta.id;
              return (
                <Pressable
                  key={conta.id}
                  accessibilityRole="radio"
                  accessibilityState={{ selected: escolhida }}
                  onPress={() => { setOrigem(conta); setResultado(null); }}
                  style={{
                    flexDirection: 'row', alignItems: 'center', gap: 10, padding: 12, borderRadius: 6, borderWidth: 1,
                    borderColor: escolhida ? c.primaria : c.borda,
                  }}
                >
                  <Ionicons name={escolhida ? 'radio-button-on' : 'radio-button-off'} size={20} color={escolhida ? c.primaria : c.suave} />
                  <Texto estilo={{ flex: 1 }}>Conta {conta.numero}</Texto>
                  <Texto forte>{brl(conta.saldo)}</Texto>
                </Pressable>
              );
            })}
          </View>
        )}
        {tentou && !origem ? <Texto cor={c.perigo} tamanho={12}>Escolha a conta</Texto> : null}

        {origem ? (
          <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 16, backgroundColor: c.superficie2, padding: 12, borderRadius: 6 }}>
            <View><Rotulo>Saldo</Rotulo><Texto forte cor={origem.saldo < 0 ? c.perigo : c.sucesso}>{brl(origem.saldo)}</Texto></View>
            <View><Rotulo>Limite</Rotulo><Texto>{brl(origem.limiteChequeEspecial)}</Texto></View>
            <View><Rotulo>Disponível</Rotulo><Texto>{brl(origem.saldoDisponivel)}</Texto></View>
          </View>
        ) : null}

        {operacao === 'transferir' ? (
          isAdmin ? (
            <BuscaSelect
              rotulo="Conta de destino"
              placeholder="Número da conta ou nome do titular"
              valor={destino}
              buscar={buscarContas}
              descrever={descreverConta}
              aoEscolher={setDestino}
            />
          ) : (
            <Campo
              rotulo="Conta de destino"
              value={numeroDestino}
              onChangeText={(t) => setNumeroDestino(t.replace(/\D/g, ''))}
              keyboardType="number-pad"
              placeholder="Número da conta"
              erro={tentou && destinoInvalido ? 'Informe a conta de destino' : undefined}
            />
          )
        ) : null}
        {isAdmin && tentou && destinoInvalido ? <Texto cor={c.perigo} tamanho={12}>Escolha a conta de destino</Texto> : null}

        <Campo
          rotulo="Valor (R$)"
          value={valor}
          onChangeText={(t) => setValor(t.replace(/[^\d,.]/g, ''))}
          keyboardType="decimal-pad"
          placeholder="0,00"
          erro={tentou && valorInvalido ? 'Informe um valor maior que zero' : undefined}
        />

        <Botao
          titulo={`Confirmar ${OPERACOES.find((o) => o.valor === operacao)!.rotulo.toLowerCase()}`}
          aoPressionar={confirmar}
          carregando={enviando}
        />
        {origem ? <Botao titulo="Ver extrato" variante="secundario" aoPressionar={() => router.push(`/conta/${origem.id}`)} /> : null}
      </Cartao>
    </Tela>
  );
}

import { router, useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import { Pressable, StyleSheet, View } from 'react-native';
import { mensagemDeErro } from '@/api/cliente';
import { contas } from '@/api/endpoints';
import type { Conta, Stats } from '@/api/tipos';
import { useSessao } from '@/auth/AuthContext';
import { Alerta, Botao, Carregando, Cartao, ItemLista, Rotulo, Tela, Texto, Vazio } from '@/components/ui';
import { useCores } from '@/theme';
import { agencia, brl } from '@/utils/formato';

export default function Inicio() {
  const { isAdmin } = useSessao();
  const c = useCores();
  const [stats, setStats] = useState<Stats | null>(null);
  const [lista, setLista] = useState<Conta[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [atualizando, setAtualizando] = useState(false);
  const [erro, setErro] = useState('');

  const carregar = useCallback(async () => {
    try {
      const [s, p] = await Promise.all([contas.stats(), contas.listar(0, '', isAdmin ? 8 : 50)]);
      setStats(s);
      setLista(p.content);
      setErro('');
    } catch (e) {
      setErro(mensagemDeErro(e));
    } finally {
      setCarregando(false);
      setAtualizando(false);
    }
  }, [isAdmin]);

  // Recarrega ao voltar para a aba (ex.: depois de uma operação)
  useFocusEffect(
    useCallback(() => {
      carregar();
    }, [carregar]),
  );

  if (carregando) return <Carregando />;

  return (
    <Tela aoAtualizar={() => { setAtualizando(true); carregar(); }} atualizando={atualizando}>
      {erro ? <Alerta tipo="erro">{erro}</Alerta> : null}

      {isAdmin ? (
        <>
          <Texto suave>Visão geral do sistema bancário</Texto>
          <Cartao estilo={{ padding: 0, gap: 0 }}>
            <View style={estilos.linhaIndicadores}>
              <Indicador rotulo="Clientes" valor={stats?.totalClientes} aoPressionar={() => router.push('/clientes')} />
              <Indicador rotulo="Agências" valor={stats?.totalAgencias} aoPressionar={() => router.push('/agencias')} />
            </View>
            <View style={[estilos.linhaIndicadores, { borderTopWidth: 1, borderColor: c.borda }]}>
              <Indicador rotulo="Contas" valor={stats?.totalContas} aoPressionar={() => router.push('/contas')} />
              <Indicador rotulo="Saldo em custódia" valor={stats ? brl(stats.saldoTotal) : undefined} />
            </View>
          </Cartao>

          <Cartao>
            <Texto forte>Acesso rápido</Texto>
            <View style={estilos.atalhos}>
              <Botao pequeno variante="secundario" icone="business-outline" titulo="Agências" aoPressionar={() => router.push('/agencias')} />
              <Botao pequeno variante="secundario" icone="document-text-outline" titulo="Auditoria" aoPressionar={() => router.push('/auditoria')} />
              <Botao pequeno icone="swap-horizontal-outline" titulo="Operações" aoPressionar={() => router.push('/operacoes')} />
            </View>
          </Cartao>
        </>
      ) : (
        <View style={{ gap: 4 }}>
          <Rotulo>Saldo total</Rotulo>
          <Texto forte tamanho={28} cor={(stats?.saldoTotal ?? 0) < 0 ? c.perigo : c.texto}>
            {stats ? brl(stats.saldoTotal) : '—'}
          </Texto>
        </View>
      )}

      <Cartao>
        <Texto forte>{isAdmin ? 'Contas recentes' : 'Minhas contas'}</Texto>
        {lista.length === 0 ? (
          <Vazio>{isAdmin ? 'Nenhuma conta aberta.' : 'Você ainda não tem contas. Procure uma agência.'}</Vazio>
        ) : (
          lista.map((conta) => (
            <ItemLista
              key={conta.id}
              titulo={isAdmin ? `${conta.numero} — ${conta.correntista.nome}` : `Conta corrente ${conta.numero}`}
              subtitulo={`${agencia(conta.agencia.numero)} — ${conta.agencia.cidade}`}
              direita={
                <View style={{ alignItems: 'flex-end' }}>
                  <Texto forte cor={conta.saldo < 0 ? c.perigo : c.texto}>{brl(conta.saldo)}</Texto>
                  <Texto suave tamanho={12}>Disp. {brl(conta.saldoDisponivel)}</Texto>
                </View>
              }
              aoPressionar={() => router.push(`/conta/${conta.id}`)}
            />
          ))
        )}
      </Cartao>

      {!isAdmin && lista.length > 0 ? (
        <Botao titulo="Realizar operações" icone="swap-horizontal-outline" aoPressionar={() => router.push('/operacoes')} />
      ) : null}
    </Tela>
  );
}

function Indicador({ rotulo, valor, aoPressionar }: { rotulo: string; valor?: number | string | null; aoPressionar?: () => void }) {
  return (
    <Pressable
      style={estilos.indicador}
      onPress={aoPressionar}
      disabled={!aoPressionar}
      accessibilityRole={aoPressionar ? 'button' : undefined}
    >
      <Rotulo>{rotulo}</Rotulo>
      <Texto forte tamanho={20}>{valor ?? '—'}</Texto>
    </Pressable>
  );
}

const estilos = StyleSheet.create({
  linhaIndicadores: { flexDirection: 'row' },
  indicador: { flex: 1, padding: 16, gap: 4 },
  atalhos: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
});

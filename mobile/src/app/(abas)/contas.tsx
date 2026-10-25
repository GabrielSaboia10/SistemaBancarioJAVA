import { router } from 'expo-router';
import { useCallback, useState } from 'react';
import { View } from 'react-native';
import { mensagemDeErro } from '@/api/cliente';
import { agencias, contas, pessoas } from '@/api/endpoints';
import type { Agencia, Pessoa } from '@/api/tipos';
import { BuscaSelect } from '@/components/BuscaSelect';
import { Alerta, Botao, Campo, Carregando, Cartao, Folha, ItemLista, Tela, Texto, Vazio } from '@/components/ui';
import { useAtrasado } from '@/hooks/useAtrasado';
import { usePaginado } from '@/hooks/usePaginado';
import { useCores } from '@/theme';
import { agencia, brl, contagem, lerValor } from '@/utils/formato';

const buscarPessoas = (termo: string) => pessoas.listar(0, termo).then((p) => p.content);
const buscarAgencias = (termo: string) => agencias.listar(0, termo).then((p) => p.content);
const descreverPessoa = (p: Pessoa) => ({ titulo: p.nome, subtitulo: p.cpf });
const descreverAgencia = (a: Agencia) => ({ titulo: `${agencia(a.numero)} — ${a.cidade}`, subtitulo: a.endereco });

/** Gerenciamento de contas (ADMIN). Tocar numa conta abre o detalhe, com editar e encerrar. */
export default function Contas() {
  const c = useCores();
  const [busca, setBusca] = useState('');
  const termo = useAtrasado(busca.trim());
  const carregar = useCallback((pagina: number) => contas.listar(pagina, termo), [termo]);
  const lista = usePaginado(carregar);
  const [abrindo, setAbrindo] = useState(false);

  return (
    <Tela aoAtualizar={lista.atualizar} atualizando={lista.atualizando}>
      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
        <Texto suave>{contagem(lista.total, 'conta', busca ? 'encontrada' : 'aberta')}</Texto>
        <Botao pequeno titulo="Abrir" icone="add" aoPressionar={() => setAbrindo(true)} />
      </View>

      <Campo rotulo="Buscar" value={busca} onChangeText={setBusca} placeholder="Número da conta ou nome do titular" autoCorrect={false} />
      {lista.erro ? <Alerta tipo="erro">{lista.erro}</Alerta> : null}

      <Cartao>
        {lista.carregando && lista.itens.length === 0 ? (
          <Carregando />
        ) : lista.itens.length === 0 ? (
          <Vazio>Nenhuma conta encontrada.</Vazio>
        ) : (
          lista.itens.map((conta) => (
            <ItemLista
              key={conta.id}
              titulo={`${conta.numero} — ${conta.correntista.nome}`}
              subtitulo={`${agencia(conta.agencia.numero)} — ${conta.agencia.cidade}`}
              direita={<Texto forte cor={conta.saldo < 0 ? c.perigo : c.sucesso}>{brl(conta.saldo)}</Texto>}
              aoPressionar={() => router.push(`/conta/${conta.id}`)}
            />
          ))
        )}
        {lista.temMais ? <Botao titulo="Carregar mais" variante="secundario" aoPressionar={lista.carregarMais} /> : null}
      </Cartao>

      {abrindo ? <AbrirConta aoFechar={() => setAbrindo(false)} aoSalvar={() => { setAbrindo(false); lista.atualizar(); }} /> : null}
    </Tela>
  );
}

function AbrirConta({ aoFechar, aoSalvar }: { aoFechar: () => void; aoSalvar: () => void }) {
  const c = useCores();
  const [pessoa, setPessoa] = useState<Pessoa | null>(null);
  const [agenciaEscolhida, setAgenciaEscolhida] = useState<Agencia | null>(null);
  const [numero, setNumero] = useState('');
  const [limite, setLimite] = useState('0');
  const [deposito, setDeposito] = useState('0');
  const [tentou, setTentou] = useState(false);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState('');

  const numeroNum = Number(numero);
  const numeroInvalido = !(numeroNum >= 1000 && numeroNum <= 99999);
  const limiteNum = lerValor(limite || '0');
  const limiteInvalido = !(limiteNum >= 0 && limiteNum <= 30000);
  const depositoNum = lerValor(deposito || '0');

  async function salvar() {
    setTentou(true);
    if (!pessoa || !agenciaEscolhida || numeroInvalido || limiteInvalido || !(depositoNum >= 0)) return;
    setSalvando(true);
    setErro('');
    try {
      await contas.criar({
        numero: numeroNum,
        limiteChequeEspecial: limiteNum,
        saldoInicial: depositoNum,
        pessoaId: pessoa.id,
        agenciaId: agenciaEscolhida.id,
      });
      aoSalvar();
    } catch (e) {
      setErro(mensagemDeErro(e));
      setSalvando(false);
    }
  }

  return (
    <Folha visivel titulo="Abrir conta" aoFechar={aoFechar}>
      <BuscaSelect rotulo="Correntista" placeholder="Nome ou CPF do cliente" valor={pessoa}
                   buscar={buscarPessoas} descrever={descreverPessoa} aoEscolher={setPessoa} />
      {tentou && !pessoa ? <Texto tamanho={12} cor={c.perigo}>Escolha o correntista</Texto> : null}
      <BuscaSelect rotulo="Agência" placeholder="Número ou cidade da agência" valor={agenciaEscolhida}
                   buscar={buscarAgencias} descrever={descreverAgencia} aoEscolher={setAgenciaEscolhida} />
      {tentou && !agenciaEscolhida ? <Texto tamanho={12} cor={c.perigo}>Escolha a agência</Texto> : null}
      <Campo rotulo="Número da conta" value={numero} onChangeText={(t) => setNumero(t.replace(/\D/g, ''))}
             keyboardType="number-pad" placeholder="1000 a 99999" erro={tentou && numeroInvalido ? 'Entre 1000 e 99999' : undefined} />
      <Campo rotulo="Limite do cheque especial (R$)" value={limite} onChangeText={setLimite} keyboardType="decimal-pad"
             erro={tentou && limiteInvalido ? 'De R$ 0 a R$ 30.000' : undefined} />
      <Campo rotulo="Depósito inicial (R$)" value={deposito} onChangeText={setDeposito} keyboardType="decimal-pad" />
      {erro ? <Alerta tipo="erro">{erro}</Alerta> : null}
      <Botao titulo="Abrir conta" aoPressionar={salvar} carregando={salvando} />
    </Folha>
  );
}

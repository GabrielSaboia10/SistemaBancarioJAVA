import { useCallback, useState } from 'react';
import { View } from 'react-native';
import { mensagemDeErro } from '@/api/cliente';
import { agencias } from '@/api/endpoints';
import type { Agencia } from '@/api/tipos';
import { avisar, confirmar } from '@/components/dialogos';
import { Alerta, Botao, Campo, Carregando, Cartao, Folha, ItemLista, Tela, Texto, Vazio } from '@/components/ui';
import { useAtrasado } from '@/hooks/useAtrasado';
import { usePaginado } from '@/hooks/usePaginado';
import { agencia, contagem } from '@/utils/formato';

export default function Agencias() {
  const [busca, setBusca] = useState('');
  const termo = useAtrasado(busca.trim());
  const carregar = useCallback((pagina: number) => agencias.listar(pagina, termo), [termo]);
  const lista = usePaginado(carregar);
  const [formulario, setFormulario] = useState<{ agencia: Agencia | null } | null>(null);

  async function excluir(a: Agencia) {
    if (!(await confirmar('Excluir agência', `Excluir a agência ${a.numero}?`, 'Excluir'))) return;
    try {
      await agencias.excluir(a.id);
      setFormulario(null);
      lista.atualizar();
    } catch (e) {
      avisar('Não foi possível excluir', mensagemDeErro(e));
    }
  }

  return (
    <Tela aoAtualizar={lista.atualizar} atualizando={lista.atualizando}>
      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
        <Texto suave>{contagem(lista.total, 'agência', termo ? 'encontrada' : 'cadastrada')}</Texto>
        <Botao pequeno titulo="Nova" icone="add" aoPressionar={() => setFormulario({ agencia: null })} />
      </View>
      <Campo rotulo="Buscar" value={busca} onChangeText={setBusca} placeholder="Número ou cidade" autoCorrect={false} />
      {lista.erro ? <Alerta tipo="erro">{lista.erro}</Alerta> : null}

      <Cartao>
        {lista.carregando && lista.itens.length === 0 ? (
          <Carregando />
        ) : lista.itens.length === 0 ? (
          <Vazio>Nenhuma agência encontrada.</Vazio>
        ) : (
          lista.itens.map((a) => (
            <ItemLista
              key={a.id}
              titulo={`${agencia(a.numero)} — ${a.cidade}`}
              subtitulo={a.endereco}
              aoPressionar={() => setFormulario({ agencia: a })}
            />
          ))
        )}
        {lista.temMais ? <Botao titulo="Carregar mais" variante="secundario" aoPressionar={lista.carregarMais} /> : null}
      </Cartao>

      {formulario ? (
        <FormularioAgencia
          agencia={formulario.agencia}
          aoFechar={() => setFormulario(null)}
          aoSalvar={() => { setFormulario(null); lista.atualizar(); }}
          aoExcluir={excluir}
        />
      ) : null}
    </Tela>
  );
}

function FormularioAgencia({
  agencia: atual,
  aoFechar,
  aoSalvar,
  aoExcluir,
}: {
  agencia: Agencia | null;
  aoFechar: () => void;
  aoSalvar: () => void;
  aoExcluir: (a: Agencia) => void;
}) {
  const [numero, setNumero] = useState(atual ? String(atual.numero) : '');
  const [endereco, setEndereco] = useState(atual?.endereco ?? '');
  const [cidade, setCidade] = useState(atual?.cidade ?? '');
  const [tentou, setTentou] = useState(false);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState('');

  const numeroNum = Number(numero);
  const numeroInvalido = !(numeroNum >= 1 && numeroNum <= 9999);

  async function salvar() {
    setTentou(true);
    if (numeroInvalido || !endereco.trim() || !cidade.trim()) return;
    setSalvando(true);
    setErro('');
    try {
      await agencias.salvar({ numero: numeroNum, endereco: endereco.trim(), cidade: cidade.trim() }, atual?.id);
      aoSalvar();
    } catch (e) {
      setErro(mensagemDeErro(e));
      setSalvando(false);
    }
  }

  return (
    <Folha visivel titulo={atual ? `Agência ${atual.numero}` : 'Nova agência'} aoFechar={aoFechar}>
      <Campo rotulo="Número" value={numero} onChangeText={(t) => setNumero(t.replace(/\D/g, ''))} keyboardType="number-pad"
             placeholder="1 a 9999" erro={tentou && numeroInvalido ? 'Número entre 1 e 9999' : undefined} />
      <Campo rotulo="Endereço" value={endereco} onChangeText={setEndereco} maxLength={100} placeholder="Rua, número - bairro"
             erro={tentou && !endereco.trim() ? 'Informe o endereço' : undefined} />
      <Campo rotulo="Cidade" value={cidade} onChangeText={setCidade} maxLength={25}
             erro={tentou && !cidade.trim() ? 'Informe a cidade' : undefined} />
      {erro ? <Alerta tipo="erro">{erro}</Alerta> : null}
      <Botao titulo="Salvar" aoPressionar={salvar} carregando={salvando} />
      {atual ? <Botao titulo="Excluir agência" variante="perigo" icone="trash-outline" aoPressionar={() => aoExcluir(atual)} /> : null}
    </Folha>
  );
}

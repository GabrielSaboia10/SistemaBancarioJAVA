import { useCallback, useState } from 'react';
import { View } from 'react-native';
import { mensagemDeErro } from '@/api/cliente';
import { pessoas } from '@/api/endpoints';
import type { Pessoa } from '@/api/tipos';
import { avisar, confirmar } from '@/components/dialogos';
import { Alerta, Botao, Campo, Carregando, Cartao, Folha, ItemLista, Tela, Texto, Vazio } from '@/components/ui';
import { useAtrasado } from '@/hooks/useAtrasado';
import { usePaginado } from '@/hooks/usePaginado';
import { useCores } from '@/theme';
import { contagem, cpfCompleto, data, mascararCpf } from '@/utils/formato';

export default function Clientes() {
  const c = useCores();
  const [busca, setBusca] = useState('');
  const termo = useAtrasado(busca.trim());
  const carregar = useCallback((pagina: number) => pessoas.listar(pagina, termo), [termo]);
  const lista = usePaginado(carregar);

  const [selecionado, setSelecionado] = useState<Pessoa | null>(null);
  const [formulario, setFormulario] = useState<{ pessoa: Pessoa | null } | null>(null);

  function atualizarNaLista(p: Pessoa) {
    lista.setItens((itens) => itens.map((x) => (x.id === p.id ? p : x)));
    setSelecionado((s) => (s?.id === p.id ? p : s));
  }

  return (
    <Tela aoAtualizar={lista.atualizar} atualizando={lista.atualizando}>
      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
        <Texto suave>{contagem(lista.total, 'cliente', busca ? 'encontrado' : 'cadastrado')}</Texto>
        <Botao pequeno titulo="Novo" icone="add" aoPressionar={() => setFormulario({ pessoa: null })} />
      </View>

      <Campo rotulo="Buscar" value={busca} onChangeText={setBusca} placeholder="Nome ou CPF" autoCorrect={false} />
      {lista.erro ? <Alerta tipo="erro">{lista.erro}</Alerta> : null}

      <Cartao>
        {lista.carregando && lista.itens.length === 0 ? (
          <Carregando />
        ) : lista.itens.length === 0 ? (
          <Vazio>{busca ? 'Nenhum cliente encontrado.' : 'Nenhum cliente cadastrado.'}</Vazio>
        ) : (
          lista.itens.map((p) => (
            <ItemLista
              key={p.id}
              titulo={p.nome}
              subtitulo={`${p.cpf} · ${p.idade} anos`}
              direita={<StatusAcesso pessoa={p} />}
              aoPressionar={() => setSelecionado(p)}
            />
          ))
        )}
        {lista.temMais ? <Botao titulo="Carregar mais" variante="secundario" aoPressionar={lista.carregarMais} /> : null}
      </Cartao>

      {selecionado ? (
        <AcoesCliente
          pessoa={selecionado}
          aoFechar={() => setSelecionado(null)}
          aoEditar={() => { setFormulario({ pessoa: selecionado }); setSelecionado(null); }}
          aoMudar={atualizarNaLista}
          aoExcluir={() => { setSelecionado(null); lista.atualizar(); }}
        />
      ) : null}

      {formulario ? (
        <FormularioCliente
          pessoa={formulario.pessoa}
          aoFechar={() => setFormulario(null)}
          aoSalvar={() => { setFormulario(null); lista.atualizar(); }}
        />
      ) : null}
    </Tela>
  );
}

function StatusAcesso({ pessoa }: { pessoa: Pessoa }) {
  const c = useCores();
  if (pessoa.ativo === false) return <Texto tamanho={12} cor={c.perigo}>Bloqueado</Texto>;
  if (pessoa.senhaTemporaria) return <Texto tamanho={12} suave>Senha temporária</Texto>;
  if (pessoa.ativo) return <Texto tamanho={12} cor={c.sucesso}>Ativo</Texto>;
  return null;
}

function AcoesCliente({
  pessoa,
  aoFechar,
  aoEditar,
  aoMudar,
  aoExcluir,
}: {
  pessoa: Pessoa;
  aoFechar: () => void;
  aoEditar: () => void;
  aoMudar: (p: Pessoa) => void;
  aoExcluir: () => void;
}) {
  const [ocupado, setOcupado] = useState(false);

  async function executar(acao: () => Promise<void>) {
    setOcupado(true);
    try {
      await acao();
    } catch (e) {
      avisar('Não foi possível concluir', mensagemDeErro(e));
    } finally {
      setOcupado(false);
    }
  }

  const alternarBloqueio = () =>
    executar(async () => {
      const bloquear = pessoa.ativo === true;
      const ok = await confirmar(
        bloquear ? 'Bloquear acesso' : 'Liberar acesso',
        bloquear ? `${pessoa.nome} será desconectado na hora.` : `Liberar o acesso de ${pessoa.nome}?`,
        bloquear ? 'Bloquear' : 'Liberar',
      );
      if (ok) aoMudar(await pessoas.alterarBloqueio(pessoa.id, !bloquear));
    });

  const novaSenha = () =>
    executar(async () => {
      if (!(await confirmar('Nova senha', `Gerar uma senha temporária para ${pessoa.nome}? A atual deixa de funcionar.`, 'Gerar'))) return;
      const res = await pessoas.redefinirSenha(pessoa.id);
      aoMudar({ ...pessoa, senhaTemporaria: true });
      avisar('Nova senha gerada', `Login: ${res.cpf}\nSenha temporária: ${res.senhaTemporaria}\n\nAnote e repasse ao cliente: ela não será exibida novamente.`);
    });

  const excluir = () =>
    executar(async () => {
      if (!(await confirmar('Excluir cliente', `Excluir ${pessoa.nome}? O acesso ao sistema também será removido.`, 'Excluir'))) return;
      await pessoas.excluir(pessoa.id);
      aoExcluir();
    });

  return (
    <Folha visivel titulo={pessoa.nome} aoFechar={aoFechar}>
      <Texto suave>{`${pessoa.cpf} · ${pessoa.idade} anos · desde ${data(pessoa.criadoEm)}`}</Texto>
      <StatusAcesso pessoa={pessoa} />
      <Botao titulo="Editar dados" variante="secundario" icone="create-outline" aoPressionar={aoEditar} desabilitado={ocupado} />
      {pessoa.ativo !== null ? (
        <>
          <Botao titulo="Gerar nova senha" variante="secundario" icone="key-outline" aoPressionar={novaSenha} desabilitado={ocupado} />
          <Botao
            titulo={pessoa.ativo ? 'Bloquear acesso' : 'Liberar acesso'}
            variante="secundario"
            icone={pessoa.ativo ? 'lock-closed-outline' : 'lock-open-outline'}
            aoPressionar={alternarBloqueio}
            desabilitado={ocupado}
          />
        </>
      ) : null}
      <Botao titulo="Excluir" variante="perigo" icone="trash-outline" aoPressionar={excluir} desabilitado={ocupado} />
    </Folha>
  );
}

function FormularioCliente({ pessoa, aoFechar, aoSalvar }: { pessoa: Pessoa | null; aoFechar: () => void; aoSalvar: () => void }) {
  const [cpf, setCpf] = useState(pessoa?.cpf ?? '');
  const [nome, setNome] = useState(pessoa?.nome ?? '');
  const [idade, setIdade] = useState(pessoa ? String(pessoa.idade) : '');
  const [tentou, setTentou] = useState(false);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState('');

  const idadeNum = Number(idade);
  const idadeInvalida = !idade || idadeNum < 0 || idadeNum > 150;

  async function salvar() {
    setTentou(true);
    if ((!pessoa && !cpfCompleto(cpf)) || !nome.trim() || idadeInvalida) return;
    setSalvando(true);
    setErro('');
    try {
      if (pessoa) {
        await pessoas.atualizar(pessoa.id, { nome: nome.trim(), idade: idadeNum });
      } else {
        const res = await pessoas.criar({ cpf, nome: nome.trim(), idade: idadeNum });
        avisar(
          'Cadastro concluído',
          `Login: ${res.pessoa.cpf}\nSenha temporária: ${res.senhaTemporaria}\n\nAnote e repasse ao cliente: ela não será exibida novamente. No primeiro acesso, ele vai criar uma senha pessoal.`,
        );
      }
      aoSalvar();
    } catch (e) {
      setErro(mensagemDeErro(e));
      setSalvando(false);
    }
  }

  return (
    <Folha visivel titulo={pessoa ? 'Editar cliente' : 'Novo cliente'} aoFechar={aoFechar}>
      <Campo
        rotulo="CPF"
        value={cpf}
        onChangeText={(t) => setCpf(mascararCpf(t))}
        keyboardType="number-pad"
        placeholder="000.000.000-00"
        editable={!pessoa}
        dica={pessoa ? 'O CPF é o login do cliente e não pode ser alterado.' : undefined}
        erro={tentou && !pessoa && !cpfCompleto(cpf) ? 'Informe um CPF com 11 dígitos' : undefined}
      />
      <Campo rotulo="Nome" value={nome} onChangeText={setNome} maxLength={40} placeholder="Nome completo"
             erro={tentou && !nome.trim() ? 'Informe o nome' : undefined} />
      <Campo rotulo="Idade" value={idade} onChangeText={(t) => setIdade(t.replace(/\D/g, ''))} keyboardType="number-pad"
             erro={tentou && idadeInvalida ? 'Idade entre 0 e 150' : undefined} />
      {!pessoa ? <Alerta tipo="aviso">O cliente recebe acesso com uma senha temporária, exibida ao concluir o cadastro.</Alerta> : null}
      {erro ? <Alerta tipo="erro">{erro}</Alerta> : null}
      <Botao titulo="Salvar" aoPressionar={salvar} carregando={salvando} />
    </Folha>
  );
}

import { useState } from 'react';
import { Alert, Platform } from 'react-native';
import { mensagemDeErro } from '@/api/cliente';
import { useSessao } from '@/auth/AuthContext';
import { Alerta, Botao, Campo } from './ui';

/** Usado na troca obrigatória e no Perfil. Depois de trocar, a API encerra a sessão e o app volta ao login. */
export function FormTrocaSenha({ rotuloSenhaAtual, tituloBotao }: { rotuloSenhaAtual: string; tituloBotao: string }) {
  const { trocarSenha } = useSessao();
  const [atual, setAtual] = useState('');
  const [nova, setNova] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [tentou, setTentou] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState('');

  const novaCurta = nova.length < 6;
  const diferentes = nova !== confirmacao;

  async function salvar() {
    setTentou(true);
    if (!atual || novaCurta || diferentes) return;
    setEnviando(true);
    setErro('');
    try {
      await trocarSenha(atual, nova);
      const aviso = 'Senha definida. Entre com a nova senha.';
      if (Platform.OS === 'web') globalThis.alert?.(aviso);
      else Alert.alert('Pronto', aviso);
    } catch (e) {
      setErro(mensagemDeErro(e));
      setEnviando(false);
    }
  }

  return (
    <>
      <Campo rotulo={rotuloSenhaAtual} value={atual} onChangeText={setAtual} secureTextEntry autoComplete="current-password"
             erro={tentou && !atual ? `Informe a ${rotuloSenhaAtual.toLowerCase()}` : undefined} />
      <Campo rotulo="Nova senha" value={nova} onChangeText={setNova} secureTextEntry autoComplete="new-password"
             erro={tentou && novaCurta ? 'Mínimo de 6 caracteres' : undefined} />
      <Campo rotulo="Confirme a nova senha" value={confirmacao} onChangeText={setConfirmacao} secureTextEntry
             autoComplete="new-password" erro={tentou && diferentes ? 'As senhas não conferem' : undefined} />
      {erro ? <Alerta tipo="erro">{erro}</Alerta> : null}
      <Botao titulo={tituloBotao} aoPressionar={salvar} carregando={enviando} />
    </>
  );
}

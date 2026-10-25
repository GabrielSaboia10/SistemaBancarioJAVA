import { useState } from 'react';
import { KeyboardAvoidingView, Platform, StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { mensagemDeErro } from '@/api/cliente';
import { useSessao } from '@/auth/AuthContext';
import { Alerta, Botao, Campo, Cartao, Texto } from '@/components/ui';
import { useCores } from '@/theme';
import { cpfCompleto, mascararCpf } from '@/utils/formato';

export default function Login() {
  const { login } = useSessao();
  const c = useCores();
  const [cpf, setCpf] = useState('');
  const [senha, setSenha] = useState('');
  const [tentou, setTentou] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState('');

  const cpfValido = cpfCompleto(cpf);

  async function entrar() {
    setTentou(true);
    if (!cpfValido || !senha) return;
    setEnviando(true);
    setErro('');
    try {
      // Depois do login, as rotas protegidas levam para as abas ou para a troca de senha
      await login(cpf, senha);
    } catch (e) {
      setErro(mensagemDeErro(e));
      setEnviando(false);
    }
  }

  return (
    <SafeAreaView style={[estilos.tela, { backgroundColor: c.fundo }]}>
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={estilos.centro}>
        <Texto forte tamanho={20} cor={c.primaria} estilo={{ textAlign: 'center', marginBottom: 16 }}>
          Sistema Bancário
        </Texto>
        <Cartao estilo={{ gap: 14, padding: 20 }}>
          <Texto forte tamanho={18}>Acessar conta</Texto>
          <Campo
            rotulo="CPF"
            value={cpf}
            onChangeText={(t) => setCpf(mascararCpf(t))}
            placeholder="000.000.000-00"
            keyboardType="number-pad"
            autoComplete="username"
            textContentType="username"
            erro={tentou && !cpfValido ? 'Informe um CPF com 11 dígitos' : undefined}
          />
          <Campo
            rotulo="Senha"
            value={senha}
            onChangeText={setSenha}
            secureTextEntry
            autoComplete="current-password"
            textContentType="password"
            onSubmitEditing={entrar}
            returnKeyType="go"
            erro={tentou && !senha ? 'Informe a senha' : undefined}
          />
          {erro ? <Alerta tipo="erro">{erro}</Alerta> : null}
          <Botao titulo="Entrar" aoPressionar={entrar} carregando={enviando} />
        </Cartao>
        <View style={{ marginTop: 16 }}>
          <Texto suave tamanho={13} estilo={{ textAlign: 'center' }}>
            Esqueceu a senha? Procure sua agência.
          </Texto>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const estilos = StyleSheet.create({
  tela: { flex: 1 },
  centro: { flex: 1, justifyContent: 'center', padding: 20, maxWidth: 440, width: '100%', alignSelf: 'center' },
});

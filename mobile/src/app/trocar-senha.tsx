import { StyleSheet, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useSessao } from '@/auth/AuthContext';
import { FormTrocaSenha } from '@/components/FormTrocaSenha';
import { Botao, Cartao, Texto } from '@/components/ui';
import { useCores } from '@/theme';

/** Primeiro acesso (ou senha redefinida pelo admin): troca obrigatória antes de usar o app. */
export default function TrocarSenha() {
  const { usuario, logout } = useSessao();
  const c = useCores();

  return (
    <SafeAreaView style={[estilos.tela, { backgroundColor: c.fundo }]}>
      <View style={estilos.centro}>
        <Cartao estilo={{ gap: 14, padding: 20 }}>
          <Texto forte tamanho={18}>Crie sua senha</Texto>
          <Texto suave tamanho={14}>
            Olá, {usuario?.nome}. Você entrou com uma senha temporária. Para continuar, defina uma senha pessoal.
          </Texto>
          <FormTrocaSenha rotuloSenhaAtual="Senha temporária" tituloBotao="Definir senha" />
        </Cartao>
        <View style={{ marginTop: 12 }}>
          <Botao titulo="Sair" variante="secundario" aoPressionar={logout} />
        </View>
      </View>
    </SafeAreaView>
  );
}

const estilos = StyleSheet.create({
  tela: { flex: 1 },
  centro: { flex: 1, justifyContent: 'center', padding: 20, maxWidth: 440, width: '100%', alignSelf: 'center' },
});

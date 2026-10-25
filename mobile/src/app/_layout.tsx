import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { View } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { SessaoProvider, useSessao } from '@/auth/AuthContext';
import { Carregando } from '@/components/ui';
import { useCores } from '@/theme';

/**
 * Quem pode ver cada tela (o Expo Router redireciona sozinho quando a regra muda):
 * - sem sessão: só o login;
 * - com senha temporária: só a troca de senha;
 * - logado: as abas e as telas internas; agências e auditoria só para ADMIN.
 * As permissões de verdade continuam na API.
 */
function Rotas() {
  const { usuario, carregando, isAdmin } = useSessao();
  const c = useCores();

  if (carregando) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', backgroundColor: c.fundo }}>
        <Carregando />
      </View>
    );
  }

  const logado = usuario !== null;
  const senhaTemporaria = usuario?.trocarSenha === true;

  return (
    <Stack
      screenOptions={{
        headerStyle: { backgroundColor: c.cabecalho },
        headerTintColor: '#fff',
        headerTitleStyle: { fontWeight: '600' },
        contentStyle: { backgroundColor: c.fundo },
        headerBackButtonDisplayMode: 'minimal',
      }}
    >
      <Stack.Protected guard={!logado}>
        <Stack.Screen name="login" options={{ headerShown: false }} />
      </Stack.Protected>

      <Stack.Protected guard={logado && senhaTemporaria}>
        <Stack.Screen name="trocar-senha" options={{ headerShown: false }} />
      </Stack.Protected>

      <Stack.Protected guard={logado && !senhaTemporaria}>
        <Stack.Screen name="(abas)" options={{ headerShown: false }} />
        <Stack.Screen name="conta/[id]" options={{ title: 'Conta' }} />
        <Stack.Protected guard={isAdmin}>
          <Stack.Screen name="agencias" options={{ title: 'Agências' }} />
          <Stack.Screen name="auditoria" options={{ title: 'Auditoria' }} />
        </Stack.Protected>
      </Stack.Protected>
    </Stack>
  );
}

export default function Layout() {
  return (
    <SafeAreaProvider>
      <SessaoProvider>
        <StatusBar style="light" />
        <Rotas />
      </SessaoProvider>
    </SafeAreaProvider>
  );
}

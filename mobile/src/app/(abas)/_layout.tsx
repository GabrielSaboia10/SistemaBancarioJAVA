import { Ionicons } from '@expo/vector-icons';
import { Tabs } from 'expo-router';
import type { ColorValue } from 'react-native';
import { useSessao } from '@/auth/AuthContext';
import { useCores } from '@/theme';

type NomeIcone = keyof typeof Ionicons.glyphMap;

const icone =
  (nome: NomeIcone) =>
  ({ color, size }: { color: ColorValue; size: number }) => <Ionicons name={nome} size={size} color={color as string} />;

/** Cliente: Minhas contas, Operações, Perfil. Admin: Painel, Clientes, Contas, Operações, Perfil. */
export default function Abas() {
  const { isAdmin } = useSessao();
  const c = useCores();

  return (
    <Tabs
      screenOptions={{
        headerStyle: { backgroundColor: c.cabecalho },
        headerTintColor: '#fff',
        headerTitleStyle: { fontWeight: '600' },
        tabBarActiveTintColor: c.primaria,
        tabBarInactiveTintColor: c.suave,
        tabBarStyle: { backgroundColor: c.superficie, borderTopColor: c.borda },
        sceneStyle: { backgroundColor: c.fundo },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: isAdmin ? 'Painel' : 'Minhas contas',
          tabBarIcon: icone(isAdmin ? 'grid-outline' : 'wallet-outline'),
        }}
      />
      <Tabs.Protected guard={isAdmin}>
        <Tabs.Screen name="clientes" options={{ title: 'Clientes', tabBarIcon: icone('people-outline') }} />
        <Tabs.Screen name="contas" options={{ title: 'Contas', tabBarIcon: icone('card-outline') }} />
      </Tabs.Protected>
      <Tabs.Screen name="operacoes" options={{ title: 'Operações', tabBarIcon: icone('swap-horizontal-outline') }} />
      <Tabs.Screen name="perfil" options={{ title: 'Perfil', tabBarIcon: icone('person-outline') }} />
    </Tabs>
  );
}

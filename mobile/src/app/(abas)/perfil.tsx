import { View } from 'react-native';
import { useSessao } from '@/auth/AuthContext';
import { confirmar } from '@/components/dialogos';
import { FormTrocaSenha } from '@/components/FormTrocaSenha';
import { Botao, Cartao, Rotulo, Tela, Texto } from '@/components/ui';

export default function Perfil() {
  const { usuario, isAdmin, logout } = useSessao();

  async function sair() {
    if (await confirmar('Sair', 'Deseja sair do aplicativo?', 'Sair')) await logout();
  }

  return (
    <Tela>
      <Cartao>
        <Dado rotulo="Nome" valor={usuario?.nome ?? ''} />
        <Dado rotulo="CPF" valor={usuario?.cpf ?? ''} />
        <Dado rotulo="Perfil" valor={isAdmin ? 'Administrador' : 'Cliente'} />
      </Cartao>

      <Cartao>
        <Texto forte>Trocar senha</Texto>
        <Texto suave tamanho={13}>Depois da troca, você entra de novo com a nova senha.</Texto>
        <FormTrocaSenha rotuloSenhaAtual="Senha atual" tituloBotao="Trocar senha" />
      </Cartao>

      <Botao titulo="Sair" variante="perigo" icone="log-out-outline" aoPressionar={sair} />
    </Tela>
  );
}

function Dado({ rotulo, valor }: { rotulo: string; valor: string }) {
  return (
    <View style={{ gap: 2 }}>
      <Rotulo>{rotulo}</Rotulo>
      <Texto>{valor}</Texto>
    </View>
  );
}

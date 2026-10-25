import { Alert, Platform } from 'react-native';

/** Pergunta de confirmação (no navegador, o Alert do React Native não tem botões). */
export function confirmar(titulo: string, mensagem: string, textoConfirmar = 'Confirmar'): Promise<boolean> {
  if (Platform.OS === 'web') return Promise.resolve(globalThis.confirm?.(`${titulo}\n\n${mensagem}`) ?? false);
  return new Promise((resolve) =>
    Alert.alert(titulo, mensagem, [
      { text: 'Cancelar', style: 'cancel', onPress: () => resolve(false) },
      { text: textoConfirmar, style: 'destructive', onPress: () => resolve(true) },
    ]),
  );
}

export function avisar(titulo: string, mensagem: string): void {
  if (Platform.OS === 'web') globalThis.alert?.(`${titulo}\n\n${mensagem}`);
  else Alert.alert(titulo, mensagem);
}

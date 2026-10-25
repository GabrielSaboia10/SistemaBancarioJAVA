import { File, Paths } from 'expo-file-system';
import * as Sharing from 'expo-sharing';
import { Platform } from 'react-native';

/**
 * Entrega um arquivo de texto ao usuário: no celular abre o menu de compartilhar
 * (WhatsApp, e-mail, Drive...); no navegador, baixa o arquivo.
 */
export async function compartilharTexto(nome: string, conteudo: string, tipo = 'text/csv'): Promise<void> {
  if (Platform.OS === 'web') {
    const url = URL.createObjectURL(new Blob([conteudo], { type: tipo }));
    const link = document.createElement('a');
    link.href = url;
    link.download = nome;
    link.click();
    URL.revokeObjectURL(url);
    return;
  }

  const arquivo = new File(Paths.cache, nome);
  if (arquivo.exists) arquivo.delete();
  arquivo.create();
  arquivo.write(conteudo);
  await Sharing.shareAsync(arquivo.uri, { mimeType: tipo, dialogTitle: nome });
}

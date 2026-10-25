import Constants from 'expo-constants';
import { Platform } from 'react-native';

/**
 * Endereço da API Spring.
 *
 * - Se EXPO_PUBLIC_API_URL estiver definida, usa ela.
 * - Em desenvolvimento com o Expo Go, o celular carrega o app a partir do PC
 *   (ex.: 192.168.0.10:8081). A API roda nesse mesmo PC, na porta 8080, então
 *   o endereço é descoberto sozinho a partir do IP do Metro.
 * - No navegador (expo start --web), usa localhost.
 */
function descobrirApiUrl(): string {
  const definida = process.env.EXPO_PUBLIC_API_URL;
  if (definida) return definida.replace(/\/$/, '');

  if (Platform.OS === 'web') return 'http://localhost:8080';

  const hostDoMetro = Constants.expoConfig?.hostUri?.split(':')[0];
  return `http://${hostDoMetro ?? 'localhost'}:8080`;
}

export const API_URL = descobrirApiUrl();

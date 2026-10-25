import { useColorScheme } from 'react-native';

/** Mesma paleta do site (frontend/src/styles.scss), em claro e escuro. */
const claro = {
  fundo: '#f3f4f6',
  superficie: '#ffffff',
  superficie2: '#f7f8fa',
  texto: '#1c2430',
  suave: '#667085',
  borda: '#e1e4e8',
  primaria: '#1f3a5f',
  sobrePrimaria: '#ffffff',
  sucesso: '#1e7b47',
  sucessoFundo: '#e8f3ec',
  perigo: '#b3261e',
  perigoFundo: '#fbeceb',
  cabecalho: '#1f3a5f',
};

const escuro: typeof claro = {
  fundo: '#121417',
  superficie: '#1a1d21',
  superficie2: '#202328',
  texto: '#e6e8eb',
  suave: '#9aa1ab',
  borda: '#2c3036',
  primaria: '#8fb3de',
  sobrePrimaria: '#10151c',
  sucesso: '#6cc08f',
  sucessoFundo: '#1b2a21',
  perigo: '#ef8a82',
  perigoFundo: '#34201f',
  cabecalho: '#0d0f12',
};

export type Cores = typeof claro;

export function useCores(): Cores {
  return useColorScheme() === 'dark' ? escuro : claro;
}

export const raio = 8;
export const espaco = 16;

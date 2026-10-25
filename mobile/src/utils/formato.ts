import type { TipoMovimentacao } from '@/api/tipos';

/** 1234.5 → "R$ 1.234,50" (monta à mão: o Intl do Hermes varia entre aparelhos). */
export function brl(valor: number): string {
  const negativo = valor < 0;
  const [inteiro, centavos] = Math.abs(valor).toFixed(2).split('.');
  const milhares = inteiro.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `${negativo ? '-' : ''}R$ ${milhares},${centavos}`;
}

/** Aplica a máscara 000.000.000-00 enquanto o usuário digita. */
export function mascararCpf(valor: string): string {
  const d = valor.replace(/\D/g, '').slice(0, 11);
  if (d.length <= 3) return d;
  if (d.length <= 6) return `${d.slice(0, 3)}.${d.slice(3)}`;
  if (d.length <= 9) return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6)}`;
  return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`;
}

export function cpfCompleto(valor: string): boolean {
  return valor.replace(/\D/g, '').length === 11;
}

/** "1234,56" ou "1234.56" → 1234.56 (o teclado numérico brasileiro usa vírgula). */
export function lerValor(texto: string): number {
  const normalizado = texto.replace(/\./g, '').replace(',', '.');
  return Number(normalizado);
}

/** 1 → "AG 0001" */
export function agencia(numero: number): string {
  return `AG ${String(numero).padStart(4, '0')}`;
}

/** ISO → "25/09/2026 14:30" no fuso do aparelho. */
export function dataHora(iso: string): string {
  const d = new Date(iso);
  const dois = (n: number) => String(n).padStart(2, '0');
  return `${dois(d.getDate())}/${dois(d.getMonth() + 1)}/${d.getFullYear()} ${dois(d.getHours())}:${dois(d.getMinutes())}`;
}

export function data(iso: string): string {
  return dataHora(iso).slice(0, 10);
}

/** Data de N dias atrás no formato yyyy-MM-dd (o que a API espera). */
export function diasAtras(dias: number, hoje = new Date()): string {
  const d = new Date(hoje);
  d.setDate(d.getDate() - dias);
  const dois = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${dois(d.getMonth() + 1)}-${dois(d.getDate())}`;
}

export const ROTULO_MOVIMENTACAO: Record<TipoMovimentacao, string> = {
  DEPOSITO: 'Depósito',
  SAQUE: 'Saque',
  TRANSFERENCIA_ENVIADA: 'Transferência enviada',
  TRANSFERENCIA_RECEBIDA: 'Transferência recebida',
};

export function ehEntrada(tipo: TipoMovimentacao): boolean {
  return tipo === 'DEPOSITO' || tipo === 'TRANSFERENCIA_RECEBIDA';
}

/** (3, 'cliente', 'cadastrado') → "3 clientes cadastrados" */
export function contagem(total: number, singular: string, sufixo = ''): string {
  const plural = total === 1 ? '' : 's';
  return `${total} ${singular}${plural}${sufixo ? ` ${sufixo}${plural}` : ''}`;
}

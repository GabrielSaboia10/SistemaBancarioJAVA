import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from './models';

/** Transforma qualquer erro de requisição em uma mensagem legível para o usuário. */
export function mensagemDeErro(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return 'Sem conexão com o servidor. Verifique sua internet.';
    const corpo = err.error as Partial<ApiError> | null;
    if (corpo?.campos) return Object.values(corpo.campos).join(' · ');
    if (corpo?.mensagem) return corpo.mensagem;
  }
  return 'Algo deu errado. Tente novamente.';
}

/** Aplica a máscara 000.000.000-00 enquanto o usuário digita. */
export function mascararCpf(valor: string): string {
  const d = valor.replace(/\D/g, '').slice(0, 11);
  if (d.length <= 3) return d;
  if (d.length <= 6) return `${d.slice(0, 3)}.${d.slice(3)}`;
  if (d.length <= 9) return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6)}`;
  return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`;
}

export const ROTULO_MOVIMENTACAO: Record<string, string> = {
  DEPOSITO: 'Depósito',
  SAQUE: 'Saque',
  TRANSFERENCIA_ENVIADA: 'Transferência enviada',
  TRANSFERENCIA_RECEBIDA: 'Transferência recebida',
};

export function ehEntrada(tipo: string): boolean {
  return tipo === 'DEPOSITO' || tipo === 'TRANSFERENCIA_RECEBIDA';
}

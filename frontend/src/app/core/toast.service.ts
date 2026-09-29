import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  texto: string;
  tipo: 'sucesso' | 'erro';
}

/** Notificações rápidas no canto da tela (ou no topo, no celular). */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private seq = 0;
  readonly toasts = signal<Toast[]>([]);

  sucesso(texto: string) {
    this.mostrar(texto, 'sucesso');
  }

  erro(texto: string) {
    this.mostrar(texto, 'erro');
  }

  fechar(id: number) {
    this.toasts.update((lista) => lista.filter((t) => t.id !== id));
  }

  private mostrar(texto: string, tipo: Toast['tipo']) {
    const id = ++this.seq;
    this.toasts.update((lista) => [...lista, { id, texto, tipo }]);
    setTimeout(() => this.fechar(id), 4500);
  }
}

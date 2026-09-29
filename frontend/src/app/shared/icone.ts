import { Component, computed, input } from '@angular/core';

const PATHS = {
  inicio: ['M3 10.5 12 3l9 7.5', 'M5 9v11h5v-6h4v6h5V9'],
  pessoas: [
    'M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z',
    'M2 21c0-3.9 3.1-7 7-7s7 3.1 7 7',
    'M16 3.5a4 4 0 0 1 0 7.5',
    'M22 21c0-2.8-1.6-5.2-4-6.3',
  ],
  agencias: ['M3 9 12 4l9 5', 'M5 10v8', 'M9.7 10v8', 'M14.3 10v8', 'M19 10v8', 'M3 21h18'],
  contas: ['M3 6h18v12H3z', 'M3 10h18', 'M7 15h3'],
  auditoria: ['M6 3h9l3 3v15H6z', 'M9 10h6', 'M9 14h6', 'M9 18h4'],
  operacoes: ['M4 8h14', 'M14 4l4 4-4 4', 'M20 16H6', 'M10 12l-4 4 4 4'],
  perfil: ['M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z', 'M4 21c0-4.4 3.6-8 8-8s8 3.6 8 8'],
  sair: ['M9 21H4V3h5', 'M16 17l5-5-5-5', 'M21 12H9'],
  voltar: ['M15 18l-6-6 6-6'],
} as const;

export type NomeIcone = keyof typeof PATHS;

@Component({
  selector: 'app-icone',
  template: `
    <svg viewBox="0 0 24 24" [attr.width]="tamanho()" [attr.height]="tamanho()" fill="none"
         stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"
         aria-hidden="true">
      @for (d of paths(); track $index) {
        <path [attr.d]="d" />
      }
    </svg>
  `,
  styles: `
    :host {
      display: inline-flex;
    }
  `,
})
export class Icone {
  readonly nome = input.required<NomeIcone>();
  readonly tamanho = input(20);
  protected readonly paths = computed(() => PATHS[this.nome()]);
}

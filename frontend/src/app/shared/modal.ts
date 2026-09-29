import { Component, input, output } from '@angular/core';

/** Modal simples: no computador fica centralizado, no celular sobe de baixo (bottom sheet). */
@Component({
  selector: 'app-modal',
  host: { '(document:keydown.escape)': 'fechar.emit()' },
  template: `
    <div class="fundo" (click)="fechar.emit()"></div>
    <section class="janela" role="dialog" aria-modal="true" [attr.aria-label]="titulo()">
      <header>
        <h2>{{ titulo() }}</h2>
        <button type="button" class="fechar" aria-label="Fechar" (click)="fechar.emit()">✕</button>
      </header>
      <ng-content />
    </section>
  `,
  styles: `
    :host {
      position: fixed;
      inset: 0;
      z-index: 50;
      display: grid;
      place-items: center;
      padding: 16px;
    }
    .fundo {
      position: absolute;
      inset: 0;
      background: rgb(0 0 0 / 45%);
    }
    .janela {
      position: relative;
      width: min(480px, 100%);
      max-height: calc(100dvh - 32px);
      overflow: auto;
      background: var(--surface);
      border-radius: var(--radius);
      padding: 20px;
    }
    header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }
    .fechar {
      border: none;
      background: none;
      color: var(--muted);
      font-size: 1.1rem;
      cursor: pointer;
      width: 40px;
      height: 40px;
    }
    @media (max-width: 720px) {
      :host {
        place-items: end stretch;
        padding: 0;
      }
      .janela {
        width: 100%;
        border-radius: var(--radius) var(--radius) 0 0;
        padding-bottom: calc(20px + env(safe-area-inset-bottom));
      }
    }
  `,
})
export class Modal {
  readonly titulo = input.required<string>();
  readonly fechar = output<void>();
}

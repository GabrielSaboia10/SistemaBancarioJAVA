import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-paginacao',
  template: `
    @if (totalPaginas() > 1) {
      <nav class="paginacao" aria-label="Paginação">
        <button type="button" class="btn secundario pequeno" [disabled]="pagina() === 0"
                (click)="mudar.emit(pagina() - 1)">Anterior</button>
        <span class="muted">Página {{ pagina() + 1 }} de {{ totalPaginas() }}</span>
        <button type="button" class="btn secundario pequeno" [disabled]="pagina() + 1 >= totalPaginas()"
                (click)="mudar.emit(pagina() + 1)">Próxima</button>
      </nav>
    }
  `,
  styles: `
    .paginacao {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;
      margin-top: 12px;
      font-size: 0.875rem;
    }
  `,
})
export class Paginacao {
  readonly pagina = input.required<number>();
  readonly totalPaginas = input.required<number>();
  readonly mudar = output<number>();
}

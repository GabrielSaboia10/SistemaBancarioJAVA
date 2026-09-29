import { Component, DestroyRef, ElementRef, effect, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, Subject, catchError, debounceTime, of, switchMap } from 'rxjs';

let sequencia = 0;

/**
 * Campo de busca com sugestões: consulta a API conforme o usuário digita, em vez de carregar
 * todos os registros num <select>. Funciona com qualquer lista (clientes, agências, contas).
 */
@Component({
  selector: 'app-busca-select',
  host: { '(document:click)': 'fecharAoClicarFora($event)' },
  template: `
    <div class="busca" [class.aberta]="aberta()">
      <input
        type="search"
        [id]="idCampo()"
        [value]="texto()"
        [placeholder]="placeholder()"
        [disabled]="desabilitado()"
        autocomplete="off"
        role="combobox"
        [attr.aria-expanded]="aberta()"
        [attr.aria-controls]="idLista"
        (input)="digitar($event)"
        (focus)="abrir()"
        (keydown.escape)="aberta.set(false)"
      />
      @if (aberta()) {
        <ul class="sugestoes" role="listbox" [id]="idLista">
          @if (carregando()) {
            <li class="info">Buscando…</li>
          } @else if (resultados().length === 0) {
            <li class="info">Nada encontrado</li>
          } @else {
            @for (item of resultados(); track $index) {
              <li role="option" tabindex="-1" (mousedown)="escolher(item)">{{ rotulo()(item) }}</li>
            }
          }
        </ul>
      }
    </div>
  `,
  styles: `
    .busca {
      position: relative;
    }
    input {
      width: 100%;
    }
    .sugestoes {
      position: absolute;
      z-index: 30;
      left: 0;
      right: 0;
      top: calc(100% + 4px);
      max-height: 240px;
      overflow: auto;
      margin: 0;
      padding: 4px 0;
      list-style: none;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius-sm);
      box-shadow: 0 6px 16px rgb(0 0 0 / 12%);
    }
    li {
      padding: 9px 12px;
      cursor: pointer;
    }
    li:not(.info):hover {
      background: var(--surface-2);
    }
    .info {
      color: var(--muted);
      cursor: default;
    }
  `,
})
export class BuscaSelect<T> {
  /** Recebe o termo digitado e devolve as opções. */
  readonly buscar = input.required<(termo: string) => Observable<T[]>>();
  readonly rotulo = input.required<(item: T) => string>();
  /** Item já escolhido (ex.: na edição), para mostrar o texto no campo. */
  readonly valor = input<T | null>(null);
  readonly placeholder = input('Digite para buscar…');
  readonly desabilitado = input(false);
  readonly idCampo = input<string>();
  readonly selecionado = output<T>();

  protected readonly idLista = `busca-select-${++sequencia}`;
  protected readonly texto = signal('');
  protected readonly aberta = signal(false);
  protected readonly carregando = signal(false);
  protected readonly resultados = signal<T[]>([]);

  private readonly termos = new Subject<string>();
  private readonly host = inject(DestroyRef);
  private readonly elemento = inject<ElementRef<HTMLElement>>(ElementRef);

  constructor() {
    this.termos
      .pipe(
        debounceTime(250),
        switchMap((termo) => {
          this.carregando.set(true);
          return this.buscar()(termo).pipe(catchError(() => of([] as T[])));
        }),
        takeUntilDestroyed(this.host),
      )
      .subscribe((itens) => {
        this.resultados.set(itens);
        this.carregando.set(false);
      });

    effect(() => {
      const atual = this.valor();
      this.texto.set(atual ? this.rotulo()(atual) : '');
    });
  }

  protected abrir() {
    this.aberta.set(true);
    this.termos.next('');
  }

  protected digitar(evento: Event) {
    const termo = (evento.target as HTMLInputElement).value;
    this.texto.set(termo);
    this.aberta.set(true);
    this.termos.next(termo);
  }

  protected escolher(item: T) {
    this.texto.set(this.rotulo()(item));
    this.aberta.set(false);
    this.selecionado.emit(item);
  }

  protected fecharAoClicarFora(evento: Event) {
    if (!this.elemento.nativeElement.contains(evento.target as Node)) this.aberta.set(false);
  }
}

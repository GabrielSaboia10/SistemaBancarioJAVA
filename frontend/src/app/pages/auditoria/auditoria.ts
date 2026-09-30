import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { EntidadeAuditada, RegistroAuditoria } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { mensagemDeErro } from '../../core/util';
import { Paginacao } from '../../shared/paginacao';

const ROTULOS_ACAO: Record<string, string> = {
  CRIAR: 'Cadastro',
  ATUALIZAR: 'Alteração',
  EXCLUIR: 'Exclusão',
  BLOQUEAR: 'Bloqueio',
  DESBLOQUEAR: 'Liberação',
  REDEFINIR_SENHA: 'Nova senha',
  DEPOSITO: 'Depósito',
  SAQUE: 'Saque',
  TRANSFERENCIA: 'Transferência',
};

const ROTULOS_ENTIDADE: Record<EntidadeAuditada, string> = {
  PESSOA: 'Cliente',
  AGENCIA: 'Agência',
  CONTA: 'Conta',
};

/** Histórico das ações administrativas: quem fez, o quê e quando. */
@Component({
  selector: 'app-auditoria',
  imports: [DatePipe, Paginacao],
  template: `
    <section class="page">
      <div class="page-header">
        <div>
          <h1>Auditoria</h1>
          <p class="muted subtitulo">Registro das ações administrativas</p>
        </div>
        <div class="campo filtro">
          <label for="entidade">Filtrar por</label>
          <select id="entidade" (change)="filtrar($event)">
            <option value="">Tudo</option>
            <option value="PESSOA">Clientes</option>
            <option value="AGENCIA">Agências</option>
            <option value="CONTA">Contas</option>
          </select>
        </div>
      </div>

      <div class="card">
        @if (carregando()) {
          <p class="vazio">Carregando…</p>
        } @else if (registros().length === 0) {
          <p class="vazio">Nenhuma ação registrada.</p>
        } @else {
          <table class="tabela">
            <thead>
              <tr><th>Data</th><th>Ação</th><th>Área</th><th>Descrição</th><th>Responsável</th></tr>
            </thead>
            <tbody>
              @for (r of registros(); track r.id) {
                <tr>
                  <td data-label="Data" class="num">{{ r.dataHora | date: 'dd/MM/yyyy HH:mm' }}</td>
                  <td data-label="Ação">{{ rotuloAcao(r.acao) }}</td>
                  <td data-label="Área">{{ rotulosEntidade[r.entidade] }}</td>
                  <td data-label="Descrição">{{ r.descricao }}</td>
                  <td data-label="Responsável" class="num">{{ r.usuarioCpf ?? 'Sistema' }}</td>
                </tr>
              }
            </tbody>
          </table>
          <app-paginacao [pagina]="pagina()" [totalPaginas]="totalPaginas()" (mudar)="carregar($event)" />
        }
      </div>
    </section>
  `,
  styles: `
    .filtro {
      min-width: 180px;
    }
  `,
})
export class AuditoriaPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);

  protected readonly rotulosEntidade = ROTULOS_ENTIDADE;
  protected readonly registros = signal<RegistroAuditoria[]>([]);
  protected readonly pagina = signal(0);
  protected readonly totalPaginas = signal(0);
  protected readonly carregando = signal(true);
  private entidade: EntidadeAuditada | '' = '';

  ngOnInit() {
    this.carregar(0);
  }

  protected rotuloAcao(acao: string): string {
    return ROTULOS_ACAO[acao] ?? acao;
  }

  protected filtrar(evento: Event) {
    this.entidade = (evento.target as HTMLSelectElement).value as EntidadeAuditada | '';
    this.carregar(0);
  }

  protected carregar(pagina: number) {
    this.api.auditoria(pagina, this.entidade).subscribe({
      next: (p) => {
        this.registros.set(p.content);
        this.pagina.set(p.page);
        this.totalPaginas.set(p.totalPages);
        this.carregando.set(false);
      },
      error: (e) => {
        this.toast.erro(mensagemDeErro(e));
        this.carregando.set(false);
      },
    });
  }
}

import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Agencia } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { mensagemDeErro } from '../../core/util';
import { AgenciaPipe, ContagemPipe } from '../../shared/formatos';
import { Modal } from '../../shared/modal';
import { Paginacao } from '../../shared/paginacao';

@Component({
  selector: 'app-agencias',
  imports: [ReactiveFormsModule, AgenciaPipe, ContagemPipe, Modal, Paginacao],
  templateUrl: './agencias.html',
})
export class AgenciasPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);

  protected readonly agencias = signal<Agencia[]>([]);
  protected readonly pagina = signal(0);
  protected readonly totalPaginas = signal(0);
  protected readonly total = signal(0);
  protected readonly carregando = signal(true);
  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<Agencia | null>(null);
  protected readonly enviando = signal(false);
  protected readonly erro = signal('');

  protected readonly form = inject(FormBuilder).nonNullable.group({
    numero: [0, [Validators.required, Validators.min(1), Validators.max(9999)]],
    endereco: ['', [Validators.required, Validators.maxLength(100)]],
    cidade: ['', [Validators.required, Validators.maxLength(25)]],
  });

  ngOnInit() {
    this.carregar(0);
  }

  protected carregar(pagina: number) {
    this.api.listarAgencias(pagina).subscribe({
      next: (p) => {
        this.agencias.set(p.content);
        this.pagina.set(p.page);
        this.totalPaginas.set(p.totalPages);
        this.total.set(p.totalElements);
        this.carregando.set(false);
      },
      error: (e) => {
        this.toast.erro(mensagemDeErro(e));
        this.carregando.set(false);
      },
    });
  }

  protected abrir(agencia: Agencia | null) {
    this.editando.set(agencia);
    this.form.reset(agencia ?? { numero: 0, endereco: '', cidade: '' });
    this.erro.set('');
    this.modalAberto.set(true);
  }

  protected salvar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.api.salvarAgencia(this.form.getRawValue(), this.editando()?.id).subscribe({
      next: () => {
        this.toast.sucesso(this.editando() ? 'Agência atualizada' : 'Agência criada');
        this.enviando.set(false);
        this.modalAberto.set(false);
        this.carregar(this.pagina());
      },
      error: (e) => {
        this.erro.set(mensagemDeErro(e));
        this.enviando.set(false);
      },
    });
  }

  protected remover(a: Agencia) {
    if (!confirm(`Excluir a agência ${a.numero}?`)) return;
    this.api.removerAgencia(a.id).subscribe({
      next: () => {
        this.toast.sucesso('Agência excluída');
        this.carregar(this.pagina());
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }
}

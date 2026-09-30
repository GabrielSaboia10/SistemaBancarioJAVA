import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, map } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Agencia, Conta, Pessoa } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { mensagemDeErro } from '../../core/util';
import { AgenciaPipe, ContagemPipe } from '../../shared/formatos';
import { BuscaSelect } from '../../shared/busca-select';
import { Modal } from '../../shared/modal';
import { Paginacao } from '../../shared/paginacao';

type TitularResumo = Pick<Pessoa, 'id' | 'nome' | 'cpf'>;
type AgenciaResumo = Pick<Agencia, 'id' | 'numero' | 'cidade'>;

@Component({
  selector: 'app-contas',
  imports: [CurrencyPipe, ReactiveFormsModule, RouterLink, AgenciaPipe, ContagemPipe, BuscaSelect, Modal, Paginacao],
  templateUrl: './contas.html',
})
export class ContasPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);

  protected readonly contas = signal<Conta[]>([]);
  protected readonly pagina = signal(0);
  protected readonly totalPaginas = signal(0);
  protected readonly total = signal(0);
  protected readonly carregando = signal(true);

  /** Correntista e agência escolhidos na busca (para mostrar o nome no campo). */
  protected readonly pessoaEscolhida = signal<TitularResumo | null>(null);
  protected readonly agenciaEscolhida = signal<AgenciaResumo | null>(null);

  protected readonly rotuloPessoa = (p: TitularResumo) => `${p.nome} — ${p.cpf}`;
  protected readonly rotuloAgencia = (a: AgenciaResumo) =>
    `AG ${String(a.numero).padStart(4, '0')} — ${a.cidade}`;
  protected readonly buscarPessoas = (termo: string): Observable<Pessoa[]> =>
    this.api.listarPessoas(0, termo, 10).pipe(map((p) => p.content));
  protected readonly buscarAgencias = (termo: string): Observable<Agencia[]> =>
    this.api.listarAgencias(0, 10, termo).pipe(map((p) => p.content));

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<Conta | null>(null);
  protected readonly enviando = signal(false);
  protected readonly erro = signal('');

  protected readonly form = inject(FormBuilder).group({
    numero: [null as number | null, [Validators.required, Validators.min(1000), Validators.max(99999)]],
    limiteChequeEspecial: [0, [Validators.required, Validators.min(0), Validators.max(30000)]],
    saldoInicial: [0, [Validators.min(0)]],
    pessoaId: [null as number | null, Validators.required],
    agenciaId: [null as number | null, Validators.required],
  });

  ngOnInit() {
    this.carregar(0);
  }

  protected carregar(pagina: number) {
    this.api.listarContas(pagina).subscribe({
      next: (p) => {
        this.contas.set(p.content);
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

  protected escolherPessoa(p: TitularResumo) {
    this.pessoaEscolhida.set(p);
    this.form.controls.pessoaId.setValue(p.id);
  }

  protected escolherAgencia(a: AgenciaResumo) {
    this.agenciaEscolhida.set(a);
    this.form.controls.agenciaId.setValue(a.id);
  }

  protected abrirNova() {
    this.editando.set(null);
    this.form.reset({ numero: null, limiteChequeEspecial: 0, saldoInicial: 0, pessoaId: null, agenciaId: null });
    this.form.controls.numero.enable();
    this.form.controls.saldoInicial.enable();
    this.form.controls.pessoaId.enable();
    this.pessoaEscolhida.set(null);
    this.agenciaEscolhida.set(null);
    this.erro.set('');
    this.modalAberto.set(true);
  }

  protected abrirEdicao(c: Conta) {
    this.editando.set(c);
    this.form.reset({
      numero: c.numero,
      limiteChequeEspecial: c.limiteChequeEspecial,
      saldoInicial: 0,
      pessoaId: c.correntista.id,
      agenciaId: c.agencia.id,
    });
    // Na edição só limite e agência podem mudar
    this.form.controls.numero.disable();
    this.form.controls.saldoInicial.disable();
    this.form.controls.pessoaId.disable();
    this.pessoaEscolhida.set(c.correntista);
    this.agenciaEscolhida.set(c.agencia);
    this.erro.set('');
    this.modalAberto.set(true);
  }

  protected salvar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const conta = this.editando();
    this.enviando.set(true);
    this.erro.set('');

    const requisicao = conta
      ? this.api.atualizarConta(conta.id, {
          limiteChequeEspecial: Number(v.limiteChequeEspecial),
          agenciaId: Number(v.agenciaId),
        })
      : this.api.criarConta({
          numero: Number(v.numero),
          limiteChequeEspecial: Number(v.limiteChequeEspecial),
          saldoInicial: Number(v.saldoInicial ?? 0),
          pessoaId: Number(v.pessoaId),
          agenciaId: Number(v.agenciaId),
        });

    requisicao.subscribe({
      next: () => {
        this.toast.sucesso(conta ? 'Conta atualizada' : 'Conta aberta');
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

  protected remover(c: Conta) {
    if (!confirm(`Encerrar a conta ${c.numero}? Só é possível com saldo zero.`)) return;
    this.api.removerConta(c.id).subscribe({
      next: () => {
        this.toast.sucesso('Conta encerrada');
        this.carregar(this.pagina());
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }
}

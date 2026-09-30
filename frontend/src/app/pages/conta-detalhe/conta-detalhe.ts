import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Conta, Movimentacao } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { ROTULO_MOVIMENTACAO, ehEntrada, mensagemDeErro } from '../../core/util';
import { AgenciaPipe } from '../../shared/formatos';
import { Icone } from '../../shared/icone';
import { Paginacao } from '../../shared/paginacao';

type Operacao = 'depositar' | 'sacar' | 'transferir';

@Component({
  selector: 'app-conta-detalhe',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, RouterLink, AgenciaPipe, Icone, Paginacao],
  templateUrl: './conta-detalhe.html',
  styleUrl: './conta-detalhe.scss',
})
export class ContaDetalhePage {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);

  /** Vem da rota /contas/:id */
  readonly id = input.required<string>();

  protected readonly conta = signal<Conta | null>(null);
  protected readonly erroCarregar = signal('');
  protected readonly operacao = signal<Operacao>('depositar');
  protected readonly enviando = signal(false);
  protected readonly erro = signal('');

  protected readonly movimentacoes = signal<Movimentacao[]>([]);
  protected readonly pagina = signal(0);
  protected readonly totalPaginas = signal(0);
  /** Período do extrato (yyyy-MM-dd); vazio = sem limite */
  protected readonly de = signal('');
  protected readonly ate = signal('');
  protected readonly exportando = signal(false);

  protected readonly rotulo = ROTULO_MOVIMENTACAO;
  protected readonly ehEntrada = ehEntrada;

  protected readonly form = inject(FormBuilder).group({
    valor: [null as number | null, [Validators.required, Validators.min(0.01)]],
    numeroContaDestino: [null as number | null],
  });

  constructor() {
    // Recarrega só quando o id da rota muda (o filtro de período recarrega por conta própria)
    effect(() => {
      const id = Number(this.id());
      untracked(() => {
        this.api.buscarConta(id).subscribe({
          next: (c) => this.conta.set(c),
          error: (e) => this.erroCarregar.set(mensagemDeErro(e)),
        });
        this.carregarExtrato(0);
      });
    });
  }

  protected escolher(op: Operacao) {
    this.operacao.set(op);
    this.erro.set('');
    const destino = this.form.controls.numeroContaDestino;
    destino.setValidators(op === 'transferir' ? [Validators.required, Validators.min(1)] : []);
    destino.updateValueAndValidity();
  }

  protected confirmar() {
    const conta = this.conta();
    if (!conta || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const valor = Number(this.form.value.valor);
    const destino = Number(this.form.value.numeroContaDestino);
    const chamadas: Record<Operacao, () => Observable<Conta>> = {
      depositar: () => this.api.depositar(conta.id, valor),
      sacar: () => this.api.sacar(conta.id, valor),
      transferir: () => this.api.transferir(conta.id, destino, valor),
    };

    this.enviando.set(true);
    this.erro.set('');
    chamadas[this.operacao()]().subscribe({
      next: (atualizada) => {
        this.conta.set(atualizada);
        this.form.reset();
        this.enviando.set(false);
        this.toast.sucesso(this.mensagemSucesso(valor, destino));
        this.carregarExtrato(0);
      },
      error: (e) => {
        this.erro.set(mensagemDeErro(e));
        this.enviando.set(false);
      },
    });
  }

  protected filtrarPeriodo(de: string, ate: string) {
    if (de && ate && de > ate) {
      this.toast.erro('A data inicial deve ser anterior à data final');
      return;
    }
    this.de.set(de);
    this.ate.set(ate);
    this.carregarExtrato(0);
  }

  protected limparPeriodo() {
    this.de.set('');
    this.ate.set('');
    this.carregarExtrato(0);
  }

  /** O CSV vem pela API autenticada; o arquivo é montado no navegador e baixado. */
  protected exportarCsv() {
    const conta = this.conta();
    if (!conta) return;
    this.exportando.set(true);
    this.api.extratoCsv(conta.id, this.de(), this.ate()).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `extrato-conta-${conta.numero}.csv`;
        link.click();
        URL.revokeObjectURL(url);
        this.exportando.set(false);
      },
      error: (e) => {
        this.toast.erro(mensagemDeErro(e));
        this.exportando.set(false);
      },
    });
  }

  protected carregarExtrato(pagina: number) {
    this.api.extrato(Number(this.id()), pagina, 15, this.de(), this.ate()).subscribe({
      next: (p) => {
        this.movimentacoes.set(p.content);
        this.pagina.set(p.page);
        this.totalPaginas.set(p.totalPages);
      },
    });
  }

  private mensagemSucesso(valor: number, destino: number): string {
    const v = valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    switch (this.operacao()) {
      case 'depositar':
        return `Depósito de ${v} realizado`;
      case 'sacar':
        return `Saque de ${v} realizado`;
      case 'transferir':
        return `Transferência de ${v} para a conta ${destino} realizada`;
    }
  }
}

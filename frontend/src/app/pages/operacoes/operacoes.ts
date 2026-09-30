import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, map } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Conta } from '../../core/models';
import { mensagemDeErro } from '../../core/util';
import { BuscaSelect } from '../../shared/busca-select';
import { AgenciaPipe } from '../../shared/formatos';

type Operacao = 'depositar' | 'sacar' | 'transferir';

const ROTULOS: Record<Operacao, string> = {
  depositar: 'Depósito',
  sacar: 'Saque',
  transferir: 'Transferência',
};

@Component({
  selector: 'app-operacoes',
  imports: [CurrencyPipe, ReactiveFormsModule, RouterLink, AgenciaPipe, BuscaSelect],
  templateUrl: './operacoes.html',
  styleUrl: './operacoes.scss',
})
export class OperacoesPage implements OnInit {
  private readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);

  protected readonly rotulos = ROTULOS;
  protected readonly operacoes: Operacao[] = ['depositar', 'sacar', 'transferir'];

  /** Cliente: as próprias contas (poucas), escolhidas numa lista. Admin: busca por número ou nome. */
  protected readonly contasDoCliente = signal<Conta[]>([]);
  protected readonly carregando = signal(true);
  protected readonly operacao = signal<Operacao>('depositar');
  protected readonly origem = signal<Conta | null>(null);
  protected readonly destino = signal<Conta | null>(null);
  protected readonly enviando = signal(false);
  protected readonly tentouEnviar = signal(false);
  protected readonly resultado = signal<{ tipo: 'sucesso' | 'erro'; texto: string } | null>(null);

  protected readonly form = inject(FormBuilder).group({
    numeroContaDestino: [null as number | null],
    valor: [null as number | null, [Validators.required, Validators.min(0.01)]],
  });

  protected readonly rotuloConta = (c: Conta) => `${c.numero} — ${c.correntista.nome}`;
  protected readonly buscarContas = (termo: string): Observable<Conta[]> =>
    this.api.listarContas(0, 10, termo).pipe(map((p) => p.content));
  protected readonly buscarDestinos = (termo: string): Observable<Conta[]> =>
    this.buscarContas(termo).pipe(map((contas) => contas.filter((c) => c.id !== this.origem()?.id)));

  ngOnInit() {
    if (this.auth.isAdmin()) {
      this.carregando.set(false);
      return;
    }
    this.api.listarContas(0, 100).subscribe({
      next: (p) => {
        this.contasDoCliente.set(p.content);
        this.carregando.set(false);
        if (p.content.length === 1) this.origem.set(p.content[0]);
      },
      error: (e) => {
        this.resultado.set({ tipo: 'erro', texto: mensagemDeErro(e) });
        this.carregando.set(false);
      },
    });
  }

  protected escolherContaDoCliente(evento: Event) {
    const id = Number((evento.target as HTMLSelectElement).value);
    this.selecionarOrigem(this.contasDoCliente().find((c) => c.id === id) ?? null);
  }

  protected selecionarOrigem(conta: Conta | null) {
    this.origem.set(conta);
    this.resultado.set(null);
  }

  protected selecionarDestino(conta: Conta) {
    this.destino.set(conta);
    this.form.controls.numeroContaDestino.setValue(conta.numero);
  }

  protected escolher(op: Operacao) {
    this.operacao.set(op);
    this.resultado.set(null);
    const destino = this.form.controls.numeroContaDestino;
    destino.setValidators(op === 'transferir' ? [Validators.required] : []);
    destino.updateValueAndValidity();
  }

  protected confirmar() {
    this.tentouEnviar.set(true);
    const origem = this.origem();
    if (!origem || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const valor = Number(this.form.value.valor);
    const destino = Number(this.form.value.numeroContaDestino);
    const chamadas: Record<Operacao, () => Observable<Conta>> = {
      depositar: () => this.api.depositar(origem.id, valor),
      sacar: () => this.api.sacar(origem.id, valor),
      transferir: () => this.api.transferir(origem.id, destino, valor),
    };

    this.enviando.set(true);
    this.resultado.set(null);
    chamadas[this.operacao()]().subscribe({
      next: (atualizada) => {
        this.origem.set(atualizada);
        this.contasDoCliente.update((lista) => lista.map((c) => (c.id === atualizada.id ? atualizada : c)));
        this.resultado.set({ tipo: 'sucesso', texto: this.mensagemSucesso(valor, atualizada.saldo) });
        this.form.reset();
        this.destino.set(null);
        this.tentouEnviar.set(false);
        this.enviando.set(false);
      },
      error: (e) => {
        this.resultado.set({ tipo: 'erro', texto: mensagemDeErro(e) });
        this.enviando.set(false);
      },
    });
  }

  private mensagemSucesso(valor: number, novoSaldo: number): string {
    const brl = (n: number) => n.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    const complemento = this.operacao() === 'transferir' ? 'Novo saldo da origem' : 'Novo saldo';
    const realizada = this.operacao() === 'transferir' ? 'realizada' : 'realizado';
    return `${ROTULOS[this.operacao()]} de ${brl(valor)} ${realizada}. ${complemento}: ${brl(novoSaldo)}`;
  }
}

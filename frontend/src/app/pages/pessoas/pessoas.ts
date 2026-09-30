import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject, debounceTime } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ApiService } from '../../core/api.service';
import { Pessoa } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { mascararCpf, mensagemDeErro } from '../../core/util';
import { ContagemPipe } from '../../shared/formatos';
import { Modal } from '../../shared/modal';
import { Paginacao } from '../../shared/paginacao';

@Component({
  selector: 'app-pessoas',
  imports: [DatePipe, ReactiveFormsModule, ContagemPipe, Modal, Paginacao],
  templateUrl: './pessoas.html',
})
export class PessoasPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly busca$ = new Subject<string>();

  protected readonly pessoas = signal<Pessoa[]>([]);
  protected readonly pagina = signal(0);
  protected readonly totalPaginas = signal(0);
  protected readonly total = signal(0);
  protected readonly carregando = signal(true);
  protected readonly busca = signal('');

  protected readonly modalAberto = signal(false);
  protected readonly editando = signal<Pessoa | null>(null);
  protected readonly enviando = signal(false);
  protected readonly erro = signal('');
  /** Exibida uma única vez, logo após cadastrar. */
  protected readonly senhaCriada = signal<{
    nome: string;
    cpf: string;
    senha: string;
    redefinida?: boolean;
  } | null>(null);

  protected readonly form = inject(FormBuilder).nonNullable.group({
    cpf: ['', [Validators.required, Validators.pattern(/^\d{3}\.\d{3}\.\d{3}-\d{2}$/)]],
    nome: ['', [Validators.required, Validators.maxLength(40)]],
    idade: [18, [Validators.required, Validators.min(0), Validators.max(150)]],
  });

  constructor() {
    this.busca$.pipe(debounceTime(300), takeUntilDestroyed()).subscribe((termo) => {
      this.busca.set(termo);
      this.carregar(0);
    });
  }

  ngOnInit() {
    this.carregar(0);
  }

  protected carregar(pagina: number) {
    this.api.listarPessoas(pagina, this.busca()).subscribe({
      next: (p) => {
        this.pessoas.set(p.content);
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

  protected buscar(evento: Event) {
    this.busca$.next((evento.target as HTMLInputElement).value);
  }

  protected aoDigitarCpf(evento: Event) {
    this.form.controls.cpf.setValue(mascararCpf((evento.target as HTMLInputElement).value));
  }

  protected abrirNovo() {
    this.editando.set(null);
    this.form.reset();
    this.form.controls.cpf.enable();
    this.erro.set('');
    this.modalAberto.set(true);
  }

  protected abrirEdicao(p: Pessoa) {
    this.editando.set(p);
    this.form.setValue({ cpf: p.cpf, nome: p.nome, idade: p.idade });
    this.form.controls.cpf.disable(); // CPF é o login: não muda
    this.erro.set('');
    this.modalAberto.set(true);
  }

  protected salvar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { cpf, nome, idade } = this.form.getRawValue();
    const pessoa = this.editando();
    this.enviando.set(true);
    this.erro.set('');

    const fim = () => {
      this.enviando.set(false);
      this.modalAberto.set(false);
      this.carregar(this.pagina());
    };
    const falha = (e: unknown) => {
      this.erro.set(mensagemDeErro(e));
      this.enviando.set(false);
    };

    if (pessoa) {
      this.api.atualizarPessoa(pessoa.id, { nome, idade }).subscribe({
        next: () => {
          this.toast.sucesso('Cadastro atualizado');
          fim();
        },
        error: falha,
      });
    } else {
      this.api.criarPessoa({ cpf, nome, idade }).subscribe({
        next: (res) => {
          this.senhaCriada.set({ nome: res.pessoa.nome, cpf: res.pessoa.cpf, senha: res.senhaTemporaria });
          fim();
        },
        error: falha,
      });
    }
  }

  protected alternarBloqueio(p: Pessoa) {
    const bloquear = p.ativo === true;
    const pergunta = bloquear
      ? `Bloquear o acesso de ${p.nome}? Ele será desconectado na hora.`
      : `Liberar o acesso de ${p.nome}?`;
    if (!confirm(pergunta)) return;
    this.api.alterarBloqueio(p.id, !bloquear).subscribe({
      next: (atualizada) => {
        this.pessoas.update((lista) => lista.map((x) => (x.id === atualizada.id ? atualizada : x)));
        this.toast.sucesso(bloquear ? 'Acesso bloqueado' : 'Acesso liberado');
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }

  protected redefinirSenha(p: Pessoa) {
    if (!confirm(`Gerar uma nova senha temporária para ${p.nome}? A senha atual deixa de funcionar.`)) return;
    this.api.redefinirSenha(p.id).subscribe({
      next: (res) => {
        this.senhaCriada.set({ nome: p.nome, cpf: res.cpf, senha: res.senhaTemporaria, redefinida: true });
        this.pessoas.update((lista) => lista.map((x) => (x.id === p.id ? { ...x, senhaTemporaria: true } : x)));
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }

  protected remover(p: Pessoa) {
    if (!confirm(`Excluir ${p.nome}? O acesso dela ao sistema também será removido.`)) return;
    this.api.removerPessoa(p.id).subscribe({
      next: () => {
        this.toast.sucesso('Cliente excluído');
        this.carregar(this.pagina());
      },
      error: (e) => this.toast.erro(mensagemDeErro(e)),
    });
  }
}

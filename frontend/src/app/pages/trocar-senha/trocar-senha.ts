import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { ToastService } from '../../core/toast.service';
import { mensagemDeErro } from '../../core/util';

function senhasIguais(grupo: AbstractControl): ValidationErrors | null {
  const { novaSenha, confirmacao } = grupo.value as { novaSenha: string; confirmacao: string };
  return novaSenha === confirmacao ? null : { diferentes: true };
}

/** Primeiro acesso (ou senha redefinida pelo admin): troca obrigatória antes de usar o sistema. */
@Component({
  selector: 'app-trocar-senha',
  imports: [ReactiveFormsModule],
  templateUrl: './trocar-senha.html',
  styleUrl: '../login/login.scss',
})
export class TrocarSenhaPage {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  protected readonly enviando = signal(false);
  protected readonly erro = signal('');

  protected readonly form = inject(FormBuilder).nonNullable.group(
    {
      senhaAtual: ['', Validators.required],
      novaSenha: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(72)]],
      confirmacao: ['', Validators.required],
    },
    { validators: senhasIguais },
  );

  protected trocar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { senhaAtual, novaSenha } = this.form.getRawValue();
    this.enviando.set(true);
    this.erro.set('');
    this.auth.trocarSenha(senhaAtual, novaSenha).subscribe({
      next: () => {
        this.toast.sucesso('Senha definida. Entre com a nova senha.');
        this.router.navigate(['/login']);
      },
      error: (e) => {
        this.erro.set(mensagemDeErro(e));
        this.enviando.set(false);
      },
    });
  }
}

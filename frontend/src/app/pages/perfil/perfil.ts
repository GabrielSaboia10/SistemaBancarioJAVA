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

@Component({
  selector: 'app-perfil',
  imports: [ReactiveFormsModule],
  template: `
    <section class="page">
      <h1>Perfil</h1>

      <div class="card dados">
        <div><span class="muted">Nome</span><strong>{{ auth.usuario()?.nome }}</strong></div>
        <div><span class="muted">CPF</span><strong class="num">{{ auth.usuario()?.cpf }}</strong></div>
        <div><span class="muted">Perfil</span><span>{{ auth.isAdmin() ? 'Administrador' : 'Cliente' }}</span></div>
      </div>

      <div class="card">
        <h2>Trocar senha</h2>
        <p class="muted">Depois da troca, você entra de novo com a nova senha.</p>
        <form class="form" [formGroup]="form" (ngSubmit)="trocar()" novalidate>
          <div class="campo">
            <label for="atual">Senha atual</label>
            <input id="atual" type="password" formControlName="senhaAtual" autocomplete="current-password" />
          </div>
          <div class="campo">
            <label for="nova">Nova senha</label>
            <input id="nova" type="password" formControlName="novaSenha" autocomplete="new-password" />
            @if (form.controls.novaSenha.touched && form.controls.novaSenha.invalid) {
              <span class="erro-campo">Mínimo de 6 caracteres</span>
            }
          </div>
          <div class="campo">
            <label for="confirmacao">Confirme a nova senha</label>
            <input id="confirmacao" type="password" formControlName="confirmacao" autocomplete="new-password" />
            @if (form.controls.confirmacao.touched && form.hasError('diferentes')) {
              <span class="erro-campo">As senhas não conferem</span>
            }
          </div>
          @if (erro()) {
            <div class="alerta erro" role="alert">{{ erro() }}</div>
          }
          <button type="submit" class="btn" [disabled]="enviando()">
            {{ enviando() ? 'Salvando…' : 'Trocar senha' }}
          </button>
        </form>
      </div>
    </section>
  `,
  styles: `
    .dados {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
      gap: 12px;
      div {
        display: flex;
        flex-direction: column;
        gap: 2px;
        align-items: flex-start;
      }
    }
    .form {
      margin-top: 12px;
      max-width: 420px;
    }
  `,
})
export class PerfilPage {
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
        this.toast.sucesso('Senha alterada. Entre novamente.');
        this.router.navigate(['/login']);
      },
      error: (e) => {
        this.erro.set(mensagemDeErro(e));
        this.enviando.set(false);
      },
    });
  }
}

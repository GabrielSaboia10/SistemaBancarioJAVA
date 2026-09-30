import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { mascararCpf, mensagemDeErro } from '../../core/util';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly form = inject(FormBuilder).nonNullable.group({
    cpf: ['', [Validators.required, Validators.pattern(/^\d{3}\.\d{3}\.\d{3}-\d{2}$/)]],
    senha: ['', Validators.required],
  });
  protected readonly enviando = signal(false);
  protected readonly erro = signal('');

  protected aoDigitarCpf(evento: Event) {
    const campo = evento.target as HTMLInputElement;
    this.form.controls.cpf.setValue(mascararCpf(campo.value));
  }

  protected entrar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.erro.set('');
    const { cpf, senha } = this.form.getRawValue();
    this.auth.login(cpf, senha).subscribe({
      next: (usuario) => this.router.navigate([usuario.trocarSenha ? '/trocar-senha' : '/']),
      error: (err) => {
        this.erro.set(mensagemDeErro(err));
        this.enviando.set(false);
      },
    });
  }
}

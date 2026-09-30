import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Conta, Stats } from '../../core/models';
import { mensagemDeErro } from '../../core/util';
import { AgenciaPipe } from '../../shared/formatos';

@Component({
  selector: 'app-dashboard',
  imports: [CurrencyPipe, RouterLink, AgenciaPipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class DashboardPage implements OnInit {
  private readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);

  protected readonly stats = signal<Stats | null>(null);
  protected readonly contas = signal<Conta[]>([]);
  protected readonly carregando = signal(true);
  protected readonly erro = signal('');

  ngOnInit() {
    this.api.stats().subscribe({
      next: (s) => this.stats.set(s),
      error: (e) => this.erro.set(mensagemDeErro(e)),
    });
    // O cliente vê as próprias contas; o admin, as primeiras contas do banco
    this.api.listarContas(0, 8).subscribe({
      next: (p) => {
        this.contas.set(p.content);
        this.carregando.set(false);
      },
      error: (e) => {
        this.erro.set(mensagemDeErro(e));
        this.carregando.set(false);
      },
    });
  }
}

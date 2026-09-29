import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import {
  Observable,
  catchError,
  defer,
  finalize,
  firstValueFrom,
  from,
  map,
  of,
  shareReplay,
  tap,
  throwError,
} from 'rxjs';
import { API } from './api.service';
import { TokenResponse, Usuario } from './models';

const REFRESH_KEY = 'banco.refreshToken';

/**
 * Guarda a sessão. O access token (15 min) fica só em memória; o refresh token fica no
 * localStorage para a sessão sobreviver a um F5 ou ao app ser fechado no celular.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _usuario = signal<Usuario | null>(null);
  private accessToken: string | null = null;
  private refreshEmAndamento: Observable<string> | null = null;

  readonly usuario = this._usuario.asReadonly();
  readonly logado = computed(() => this._usuario() !== null);
  readonly isAdmin = computed(() => this._usuario()?.role === 'ADMIN');
  /** Senha temporária: o usuário só pode acessar a tela de troca de senha. */
  readonly precisaTrocarSenha = computed(() => this._usuario()?.trocarSenha === true);

  get token(): string | null {
    return this.accessToken;
  }

  login(cpf: string, senha: string): Observable<Usuario> {
    return this.http
      .post<TokenResponse>(`${API}/auth/login`, { cpf, senha })
      .pipe(map((res) => this.salvarSessao(res)));
  }

  /**
   * Troca o refresh token por um novo access token. O refresh token só pode ser usado uma vez, então:
   * - na mesma aba, várias requisições com 401 esperam o mesmo refresh;
   * - entre abas, um lock do navegador faz uma renovar por vez, cada uma lendo o token mais novo.
   */
  refresh(): Observable<string> {
    if (!this.lerRefreshToken()) return throwError(() => new Error('Sem sessão'));

    this.refreshEmAndamento ??= defer(() => from(this.comTravaEntreAbas(() => this.renovar())))
      .pipe(
        catchError((err) => {
          this.limparSessao();
          return throwError(() => err);
        }),
        finalize(() => (this.refreshEmAndamento = null)),
        shareReplay(1),
      );
    return this.refreshEmAndamento;
  }

  /** Chamado na inicialização do app: restaura a sessão se houver refresh token salvo. */
  restaurarSessao(): Observable<unknown> {
    if (!this.lerRefreshToken()) return of(null);
    return this.refresh().pipe(catchError(() => of(null)));
  }

  logout(): void {
    const refreshToken = this.lerRefreshToken();
    if (refreshToken) {
      this.http.post(`${API}/auth/logout`, { refreshToken }).subscribe({ error: () => {} });
    }
    this.limparSessao();
    this.router.navigate(['/login']);
  }

  trocarSenha(senhaAtual: string, novaSenha: string): Observable<void> {
    return this.http
      .put<void>(`${API}/auth/senha`, { senhaAtual, novaSenha })
      .pipe(tap(() => this.limparSessao()));
  }

  limparSessao(): void {
    this.accessToken = null;
    this._usuario.set(null);
    try {
      localStorage.removeItem(REFRESH_KEY);
    } catch {
      /* armazenamento indisponível (modo privado) */
    }
  }

  private async renovar(): Promise<string> {
    const refreshToken = this.lerRefreshToken();
    if (!refreshToken) throw new Error('Sem sessão');
    const res = await firstValueFrom(
      this.http.post<TokenResponse>(`${API}/auth/refresh`, { refreshToken }),
    );
    this.salvarSessao(res); // salva ainda dentro do lock, para a próxima aba já ler o token novo
    return res.token;
  }

  private comTravaEntreAbas<T>(fn: () => Promise<T>): Promise<T> {
    const locks = typeof navigator !== 'undefined' ? navigator.locks : undefined;
    return locks ? locks.request('banco-refresh-token', fn) : fn();
  }

  private salvarSessao(res: TokenResponse): Usuario {
    this.accessToken = res.token;
    this._usuario.set(res.usuario);
    try {
      localStorage.setItem(REFRESH_KEY, res.refreshToken);
    } catch {
      /* sem persistência: a sessão dura até fechar a aba */
    }
    return res.usuario;
  }

  private lerRefreshToken(): string | null {
    try {
      return localStorage.getItem(REFRESH_KEY);
    } catch {
      return null;
    }
  }
}

import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { API } from './api.service';
import { AuthService } from './auth.service';

const ROTAS_SEM_TOKEN = [`${API}/auth/login`, `${API}/auth/refresh`, `${API}/auth/logout`];

function comToken(req: HttpRequest<unknown>, token: string | null) {
  return token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
}

/**
 * Coloca o Bearer token nas chamadas à API.
 * - 401 (token expirado): renova a sessão com o refresh token e repete a requisição uma vez.
 * - 403 com senha temporária: leva o usuário para a tela de troca obrigatória.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (ROTAS_SEM_TOKEN.includes(req.url)) return next(req);

  return next(comToken(req, auth.token)).pipe(
    catchError((err: unknown) => {
      if (!(err instanceof HttpErrorResponse)) return throwError(() => err);

      if (err.status === 403 && err.error?.codigo === 'TROCA_DE_SENHA_OBRIGATORIA') {
        router.navigate(['/trocar-senha']);
        return throwError(() => err);
      }
      if (err.status !== 401) return throwError(() => err);

      return auth.refresh().pipe(
        catchError(() => {
          auth.logout();
          return throwError(() => err);
        }),
        switchMap((novoToken) => next(comToken(req, novoToken))),
      );
    }),
  );
};

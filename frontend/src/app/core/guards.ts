import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Só usuários logados e sem senha temporária pendente.
 * As permissões de verdade são checadas na API; isto é só navegação.
 */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.logado()) return router.createUrlTree(['/login']);
  return !auth.precisaTrocarSenha() || router.createUrlTree(['/trocar-senha']);
};

/** Tela de troca obrigatória: só para quem está logado com senha temporária. */
export const senhaTemporariaGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.logado()) return router.createUrlTree(['/login']);
  return auth.precisaTrocarSenha() || router.createUrlTree(['/']);
};

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isAdmin() || inject(Router).createUrlTree(['/']);
};

/** Quem já está logado não precisa ver a tela de login. */
export const visitanteGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return !auth.logado() || inject(Router).createUrlTree(['/']);
};

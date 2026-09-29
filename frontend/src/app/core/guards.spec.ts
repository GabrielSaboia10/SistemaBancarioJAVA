import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { adminGuard, authGuard, senhaTemporariaGuard } from './guards';

function executar(guard: typeof authGuard, sessao: { logado: boolean; admin?: boolean; trocarSenha?: boolean }) {
  TestBed.configureTestingModule({
    providers: [
      provideRouter([]),
      {
        provide: AuthService,
        useValue: {
          logado: signal(sessao.logado),
          isAdmin: signal(!!sessao.admin),
          precisaTrocarSenha: signal(!!sessao.trocarSenha),
        },
      },
    ],
  });
  const resultado = TestBed.runInInjectionContext(() =>
    guard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
  );
  const router = TestBed.inject(Router);
  return resultado instanceof UrlTree ? router.serializeUrl(resultado) : resultado;
}

describe('guards', () => {
  it('authGuard: visitante vai para o login', () => {
    expect(executar(authGuard, { logado: false })).toBe('/login');
  });

  it('authGuard: com senha temporária, só a tela de troca', () => {
    expect(executar(authGuard, { logado: true, trocarSenha: true })).toBe('/trocar-senha');
  });

  it('authGuard: logado com senha definitiva entra', () => {
    expect(executar(authGuard, { logado: true })).toBe(true);
  });

  it('senhaTemporariaGuard: quem já tem senha definitiva volta ao início', () => {
    expect(executar(senhaTemporariaGuard, { logado: true })).toBe('/');
  });

  it('adminGuard: cliente não entra na área de administração', () => {
    expect(executar(adminGuard, { logado: true, admin: false })).toBe('/');
    TestBed.resetTestingModule();
    expect(executar(adminGuard, { logado: true, admin: true })).toBe(true);
  });
});

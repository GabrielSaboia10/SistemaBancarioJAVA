import { Routes } from '@angular/router';
import { adminGuard, authGuard, senhaTemporariaGuard, visitanteGuard } from './core/guards';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [visitanteGuard],
    title: 'Entrar · Sistema Bancário',
    loadComponent: () => import('./pages/login/login').then((m) => m.LoginPage),
  },
  {
    path: 'trocar-senha',
    canActivate: [senhaTemporariaGuard],
    title: 'Criar senha · Sistema Bancário',
    loadComponent: () => import('./pages/trocar-senha/trocar-senha').then((m) => m.TrocarSenhaPage),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell').then((m) => m.Shell),
    children: [
      {
        path: '',
        title: 'Sistema Bancário',
        loadComponent: () => import('./pages/dashboard/dashboard').then((m) => m.DashboardPage),
      },
      {
        path: 'contas/:id',
        title: 'Conta · Sistema Bancário',
        loadComponent: () =>
          import('./pages/conta-detalhe/conta-detalhe').then((m) => m.ContaDetalhePage),
      },
      {
        path: 'operacoes',
        title: 'Operações · Sistema Bancário',
        loadComponent: () => import('./pages/operacoes/operacoes').then((m) => m.OperacoesPage),
      },
      {
        path: 'perfil',
        title: 'Perfil · Sistema Bancário',
        loadComponent: () => import('./pages/perfil/perfil').then((m) => m.PerfilPage),
      },
      {
        path: 'admin',
        canActivate: [adminGuard],
        children: [
          {
            path: 'pessoas',
            title: 'Clientes · Sistema Bancário',
            loadComponent: () => import('./pages/pessoas/pessoas').then((m) => m.PessoasPage),
          },
          {
            path: 'agencias',
            title: 'Agências · Sistema Bancário',
            loadComponent: () => import('./pages/agencias/agencias').then((m) => m.AgenciasPage),
          },
          {
            path: 'auditoria',
            title: 'Auditoria · Sistema Bancário',
            loadComponent: () => import('./pages/auditoria/auditoria').then((m) => m.AuditoriaPage),
          },
          {
            path: 'contas',
            title: 'Contas · Sistema Bancário',
            loadComponent: () => import('./pages/contas/contas').then((m) => m.ContasPage),
          },
        ],
      },
    ],
  },
  { path: '**', redirectTo: '' },
];

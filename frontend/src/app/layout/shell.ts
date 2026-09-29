import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { Icone, NomeIcone } from '../shared/icone';

interface ItemMenu {
  rota: string;
  rotulo: string;
  icone: NomeIcone;
  exato?: boolean;
  /** Aparece na barra inferior do celular (cabem 5 itens; o resto fica no Painel). */
  noCelular: boolean;
}

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Icone],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  protected readonly auth = inject(AuthService);

  protected readonly menu = computed<ItemMenu[]>(() => {
    if (this.auth.isAdmin()) {
      return [
        { rota: '/', rotulo: 'Painel', icone: 'inicio', exato: true, noCelular: true },
        { rota: '/admin/pessoas', rotulo: 'Clientes', icone: 'pessoas', noCelular: true },
        { rota: '/admin/agencias', rotulo: 'Agências', icone: 'agencias', noCelular: false },
        { rota: '/admin/contas', rotulo: 'Contas', icone: 'contas', noCelular: true },
        { rota: '/operacoes', rotulo: 'Operações', icone: 'operacoes', noCelular: true },
        { rota: '/admin/auditoria', rotulo: 'Auditoria', icone: 'auditoria', noCelular: false },
        { rota: '/perfil', rotulo: 'Perfil', icone: 'perfil', noCelular: true },
      ];
    }
    return [
      { rota: '/', rotulo: 'Minhas contas', icone: 'contas', exato: true, noCelular: true },
      { rota: '/operacoes', rotulo: 'Operações', icone: 'operacoes', noCelular: true },
      { rota: '/perfil', rotulo: 'Perfil', icone: 'perfil', noCelular: true },
    ];
  });

  protected readonly menuCelular = computed(() => this.menu().filter((i) => i.noCelular));
}

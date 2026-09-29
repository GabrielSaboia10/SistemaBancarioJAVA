import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { BuscaSelect } from './busca-select';

interface Item {
  id: number;
  nome: string;
}

const ITENS: Item[] = [
  { id: 1, nome: 'Maria Silva' },
  { id: 2, nome: 'João Souza' },
];

@Component({
  imports: [BuscaSelect],
  template: `<app-busca-select [buscar]="buscar" [rotulo]="rotulo" (selecionado)="escolhido.set($event)" />`,
})
class Hospedeiro {
  readonly escolhido = signal<Item | null>(null);
  readonly termos: string[] = [];
  readonly buscar = (termo: string) => {
    this.termos.push(termo);
    return of(ITENS.filter((i) => i.nome.toLowerCase().includes(termo.toLowerCase())));
  };
  readonly rotulo = (i: Item) => i.nome;
}

const esperarDebounce = () => new Promise((r) => setTimeout(r, 300));

describe('BuscaSelect', () => {
  it('busca conforme digita e emite o item escolhido', async () => {
    const fixture = TestBed.createComponent(Hospedeiro);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const campo = el.querySelector('input')!;

    campo.value = 'mar';
    campo.dispatchEvent(new Event('input'));
    await esperarDebounce();
    await fixture.whenStable();

    expect(fixture.componentInstance.termos).toContain('mar');
    const opcoes = el.querySelectorAll('[role=option]');
    expect(opcoes.length).toBe(1);
    expect(opcoes[0].textContent).toContain('Maria Silva');

    opcoes[0].dispatchEvent(new Event('mousedown'));
    await fixture.whenStable();
    expect(fixture.componentInstance.escolhido()?.id).toBe(1);
    expect(campo.value).toBe('Maria Silva');
    expect(el.querySelector('[role=listbox]')).toBeNull();
  });

  it('avisa quando nada é encontrado', async () => {
    const fixture = TestBed.createComponent(Hospedeiro);
    await fixture.whenStable();
    const campo = (fixture.nativeElement as HTMLElement).querySelector('input')!;
    campo.value = 'zzz';
    campo.dispatchEvent(new Event('input'));
    await esperarDebounce();
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Nada encontrado');
  });
});

import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DEFAULT_CURRENCY_CODE, LOCALE_ID, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { Conta } from '../../core/models';
import { OperacoesPage } from './operacoes';
import '../../../test-setup-locale';

/** toLocaleString usa espaço não separável entre "R$" e o valor */
const texto = (el: Element | null) => (el?.textContent ?? '').replace(/ /g, ' ');

const conta: Conta = {
  id: 7,
  numero: 1001,
  saldo: 100,
  limiteChequeEspecial: 50,
  saldoDisponivel: 150,
  correntista: { id: 1, nome: 'Maria Silva', cpf: '529.982.247-25' },
  agencia: { id: 1, numero: 1, cidade: 'Rio de Janeiro' },
};

describe('OperacoesPage (cliente)', () => {
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [OperacoesPage],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { isAdmin: signal(false) } },
        { provide: LOCALE_ID, useValue: 'pt-BR' },
        { provide: DEFAULT_CURRENCY_CODE, useValue: 'BRL' },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  async function abrirComUmaConta() {
    const fixture = TestBed.createComponent(OperacoesPage);
    fixture.detectChanges();
    backend.expectOne((r) => r.url === '/api/contas').flush({
      content: [conta], page: 0, size: 100, totalElements: 1, totalPages: 1,
    });
    await fixture.whenStable();
    return fixture;
  }

  it('seleciona sozinha a única conta do cliente e mostra o saldo', async () => {
    const fixture = await abrirComUmaConta();
    const conteudo = texto(fixture.nativeElement as HTMLElement);
    expect(conteudo).toContain('R$ 100,00');
    expect(conteudo).toContain('AG 0001');
  });

  it('faz o saque e mostra o novo saldo', async () => {
    const fixture = await abrirComUmaConta();
    const el = fixture.nativeElement as HTMLElement;

    const abaSaque = Array.from(el.querySelectorAll<HTMLButtonElement>('[role=tab]')).find((b) =>
      b.textContent!.includes('Saque'),
    )!;
    abaSaque.click();
    const valor = el.querySelector<HTMLInputElement>('#valor')!;
    valor.value = '30';
    valor.dispatchEvent(new Event('input'));
    el.querySelector('form')!.dispatchEvent(new Event('submit'));

    const req = backend.expectOne('/api/contas/7/sacar');
    expect(req.request.body).toEqual({ valor: 30 });
    req.flush({ ...conta, saldo: 70, saldoDisponivel: 120 });
    await fixture.whenStable();

    expect(texto(el.querySelector('[role=status]'))).toContain('Saque de R$ 30,00 realizado. Novo saldo: R$ 70,00');
  });

  it('mostra o erro da API (ex.: saldo insuficiente)', async () => {
    const fixture = await abrirComUmaConta();
    const el = fixture.nativeElement as HTMLElement;
    const valor = el.querySelector<HTMLInputElement>('#valor')!;
    valor.value = '999';
    valor.dispatchEvent(new Event('input'));
    el.querySelector('form')!.dispatchEvent(new Event('submit'));

    backend.expectOne('/api/contas/7/depositar').flush(
      { status: 422, mensagem: 'Saldo insuficiente. Disponível: R$ 150,00' },
      { status: 422, statusText: 'Unprocessable' },
    );
    await fixture.whenStable();
    expect(texto(el.querySelector('[role=status]'))).toContain('Saldo insuficiente');
  });
});

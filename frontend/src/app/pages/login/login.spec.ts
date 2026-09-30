import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { LoginPage } from './login';

const usuario = { id: 2, cpf: '529.982.247-25', nome: 'Maria', role: 'CLIENTE', pessoaId: 1 };

describe('LoginPage', () => {
  let backend: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    backend = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  afterEach(() => backend.verify());

  async function preencherEEnviar(cpf: string, senha: string) {
    const fixture = TestBed.createComponent(LoginPage);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const campoCpf = el.querySelector<HTMLInputElement>('#cpf')!;
    campoCpf.value = cpf;
    campoCpf.dispatchEvent(new Event('input'));
    const campoSenha = el.querySelector<HTMLInputElement>('#senha')!;
    campoSenha.value = senha;
    campoSenha.dispatchEvent(new Event('input'));
    el.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
    return fixture;
  }

  it('aplica a máscara e envia o CPF formatado', async () => {
    await preencherEEnviar('52998224725', 'cliente123');
    const req = backend.expectOne('/api/auth/login');
    expect(req.request.body).toEqual({ cpf: '529.982.247-25', senha: 'cliente123' });
    req.flush({ token: 't', refreshToken: 'r', expiraEm: 900, usuario: { ...usuario, trocarSenha: false } });
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('manda para a troca de senha quando a senha é temporária', async () => {
    await preencherEEnviar('52998224725', 'Tmp12345');
    backend.expectOne('/api/auth/login')
      .flush({ token: 't', refreshToken: 'r', expiraEm: 900, usuario: { ...usuario, trocarSenha: true } });
    expect(router.navigate).toHaveBeenCalledWith(['/trocar-senha']);
  });

  it('mostra a mensagem da API quando o login falha', async () => {
    const fixture = await preencherEEnviar('52998224725', 'errada');
    backend.expectOne('/api/auth/login')
      .flush({ status: 401, mensagem: 'CPF ou senha inválidos' }, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).querySelector('[role=alert]')?.textContent)
      .toContain('CPF ou senha inválidos');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('não envia com CPF incompleto', async () => {
    const fixture = await preencherEEnviar('123', 'x');
    backend.expectNone('/api/auth/login');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Informe um CPF com 11 dígitos');
  });
});

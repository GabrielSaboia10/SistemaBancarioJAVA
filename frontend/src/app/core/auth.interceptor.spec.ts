import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

const usuario = { id: 1, cpf: '000.000.000-00', nome: 'Admin', role: 'ADMIN', pessoaId: null };

describe('authInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let auth: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([{ path: 'login', children: [] }]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });

  afterEach(() => backend.verify());

  function logar() {
    auth.login('00000000000', 'admin123').subscribe();
    backend.expectOne('/api/auth/login').flush({ token: 'token-1', refreshToken: 'refresh-1', expiraEm: 900, usuario });
  }

  it('envia o Bearer token nas chamadas à API', () => {
    logar();
    http.get('/api/contas').subscribe();
    const req = backend.expectOne('/api/contas');
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-1');
    req.flush({});
  });

  /** A renovação passa por uma Promise (lock entre abas), então a continuação é assíncrona. */
  const aguardar = () => new Promise((r) => setTimeout(r));

  it('renova o token ao receber 401 e repete a requisição', async () => {
    logar();
    let resposta: unknown;
    http.get('/api/contas').subscribe((r) => (resposta = r));

    backend.expectOne('/api/contas').flush({}, { status: 401, statusText: 'Unauthorized' });
    const refresh = backend.expectOne('/api/auth/refresh');
    expect(refresh.request.body).toEqual({ refreshToken: 'refresh-1' });
    refresh.flush({ token: 'token-2', refreshToken: 'refresh-2', expiraEm: 900, usuario });
    await aguardar();

    expect(localStorage.getItem('banco.refreshToken')).toBe('refresh-2');
    const repetida = backend.expectOne('/api/contas');
    expect(repetida.request.headers.get('Authorization')).toBe('Bearer token-2');
    repetida.flush({ ok: true });
    expect(resposta).toEqual({ ok: true });
  });

  it('encerra a sessão se o refresh também falhar', async () => {
    logar();
    http.get('/api/contas').subscribe({ error: () => {} });

    backend.expectOne('/api/contas').flush({}, { status: 401, statusText: 'Unauthorized' });
    backend.expectOne('/api/auth/refresh').flush({}, { status: 401, statusText: 'Unauthorized' });
    await aguardar();

    expect(auth.logado()).toBe(false);
    expect(localStorage.getItem('banco.refreshToken')).toBeNull();
  });
});

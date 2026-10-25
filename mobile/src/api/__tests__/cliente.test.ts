const guardado: { token: string | null } = { token: null };

jest.mock('../armazenamento', () => ({
  armazenamento: {
    ler: jest.fn(async () => guardado.token),
    salvar: jest.fn(async (t: string) => { guardado.token = t; }),
    apagar: jest.fn(async () => { guardado.token = null; }),
  },
}));

import { api, ErroApi } from '../cliente';

const usuario = { id: 1, cpf: '000.000.000-00', nome: 'Admin', role: 'ADMIN', pessoaId: null, trocarSenha: false };

function resposta(status: number, corpo?: unknown) {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => corpo,
    text: async () => JSON.stringify(corpo),
  } as Response;
}

const fetchMock = jest.fn();
globalThis.fetch = fetchMock as unknown as typeof fetch;

beforeEach(async () => {
  fetchMock.mockReset();
  guardado.token = null;
  fetchMock.mockResolvedValueOnce(resposta(200, { token: 'acesso-1', refreshToken: 'refresh-1', expiraEm: 900, usuario }));
  await api.login('00000000000', 'admin123');
  fetchMock.mockReset();
});

it('guarda o refresh token e manda o Bearer nas requisições', async () => {
  expect(guardado.token).toBe('refresh-1');
  fetchMock.mockResolvedValueOnce(resposta(200, []));
  await api.requisitar('GET', '/api/contas');
  expect(fetchMock.mock.calls[0][1].headers.Authorization).toBe('Bearer acesso-1');
});

it('renova o token ao receber 401 e repete a requisição', async () => {
  fetchMock
    .mockResolvedValueOnce(resposta(401, { mensagem: 'expirado' }))
    .mockResolvedValueOnce(resposta(200, { token: 'acesso-2', refreshToken: 'refresh-2', expiraEm: 900, usuario }))
    .mockResolvedValueOnce(resposta(200, { ok: true }));

  const r = await api.requisitar('GET', '/api/contas');

  expect(r).toEqual({ ok: true });
  expect(JSON.parse(fetchMock.mock.calls[1][1].body)).toEqual({ refreshToken: 'refresh-1' });
  expect(fetchMock.mock.calls[2][1].headers.Authorization).toBe('Bearer acesso-2');
  expect(guardado.token).toBe('refresh-2');
});

it('várias requisições com 401 ao mesmo tempo usam uma única renovação', async () => {
  fetchMock.mockImplementation(async (url: string, init: RequestInit) => {
    if (url.endsWith('/api/auth/refresh')) {
      return resposta(200, { token: 'acesso-2', refreshToken: 'refresh-2', expiraEm: 900, usuario });
    }
    const auth = (init.headers as Record<string, string>).Authorization;
    return auth === 'Bearer acesso-2' ? resposta(200, { ok: true }) : resposta(401, {});
  });

  await Promise.all([api.requisitar('GET', '/a'), api.requisitar('GET', '/b'), api.requisitar('GET', '/c')]);

  const renovacoes = fetchMock.mock.calls.filter(([url]: [string]) => String(url).endsWith('/api/auth/refresh'));
  expect(renovacoes).toHaveLength(1);
});

it('encerra a sessão se a renovação falhar', async () => {
  const ouvinte = jest.fn();
  const parar = api.aoMudarSessao(ouvinte);
  fetchMock.mockResolvedValueOnce(resposta(401, {})).mockResolvedValueOnce(resposta(401, { mensagem: 'Sessão expirada' }));

  await expect(api.requisitar('GET', '/api/contas')).rejects.toBeInstanceOf(ErroApi);
  expect(guardado.token).toBeNull();
  expect(ouvinte).toHaveBeenLastCalledWith(null);
  parar();
});

it('traduz os erros de validação da API numa mensagem só', async () => {
  fetchMock.mockResolvedValueOnce(
    resposta(400, { status: 400, mensagem: 'Dados inválidos', campos: { cpf: 'CPF inválido', nome: 'Informe o nome' } }),
  );
  await expect(api.requisitar('POST', '/api/pessoas', {})).rejects.toMatchObject({
    status: 400,
    message: 'CPF inválido · Informe o nome',
  });
});

it('avisa quando não há conexão com a API', async () => {
  fetchMock.mockRejectedValueOnce(new TypeError('Network request failed'));
  await expect(api.requisitar('GET', '/api/contas')).rejects.toMatchObject({ status: 0 });
});

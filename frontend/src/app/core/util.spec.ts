import { HttpErrorResponse } from '@angular/common/http';
import { mascararCpf, mensagemDeErro } from './util';

describe('mascararCpf', () => {
  it('formata conforme o usuário digita', () => {
    expect(mascararCpf('529')).toBe('529');
    expect(mascararCpf('5299822')).toBe('529.982.2');
    expect(mascararCpf('52998224725')).toBe('529.982.247-25');
  });

  it('ignora letras e corta dígitos a mais', () => {
    expect(mascararCpf('529a982b24725999')).toBe('529.982.247-25');
  });
});

describe('mensagemDeErro', () => {
  it('usa a mensagem da API', () => {
    const err = new HttpErrorResponse({ status: 422, error: { status: 422, mensagem: 'Saldo insuficiente' } });
    expect(mensagemDeErro(err)).toBe('Saldo insuficiente');
  });

  it('junta os erros de validação por campo', () => {
    const err = new HttpErrorResponse({
      status: 400,
      error: { status: 400, mensagem: 'Dados inválidos', campos: { cpf: 'CPF inválido', nome: 'Informe o nome' } },
    });
    expect(mensagemDeErro(err)).toBe('CPF inválido · Informe o nome');
  });

  it('avisa quando o servidor está fora do ar', () => {
    expect(mensagemDeErro(new HttpErrorResponse({ status: 0 }))).toContain('Sem conexão');
  });
});

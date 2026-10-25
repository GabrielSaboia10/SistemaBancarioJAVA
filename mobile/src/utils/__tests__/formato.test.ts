import { agencia, brl, contagem, cpfCompleto, diasAtras, lerValor, mascararCpf } from '../formato';

describe('brl', () => {
  it('formata no padrão brasileiro', () => {
    expect(brl(1234.5)).toBe('R$ 1.234,50');
    expect(brl(0)).toBe('R$ 0,00');
    expect(brl(1000000)).toBe('R$ 1.000.000,00');
    expect(brl(-50.25)).toBe('-R$ 50,25');
  });
});

describe('CPF', () => {
  it('aplica a máscara conforme digita e ignora o que não é número', () => {
    expect(mascararCpf('529')).toBe('529');
    expect(mascararCpf('5299822')).toBe('529.982.2');
    expect(mascararCpf('529a98224725999')).toBe('529.982.247-25');
  });

  it('só considera completo com 11 dígitos', () => {
    expect(cpfCompleto('529.982.247-25')).toBe(true);
    expect(cpfCompleto('529.982.247')).toBe(false);
  });
});

describe('lerValor', () => {
  it('aceita vírgula ou ponto decimal', () => {
    expect(lerValor('1234,56')).toBe(1234.56);
    expect(lerValor('1.234,56')).toBe(1234.56);
    expect(lerValor('10')).toBe(10);
    expect(Number.isNaN(lerValor('abc'))).toBe(true);
  });
});

describe('outros formatos', () => {
  it('agência com zeros à esquerda', () => {
    expect(agencia(1)).toBe('AG 0001');
  });

  it('plural simples', () => {
    expect(contagem(1, 'cliente', 'cadastrado')).toBe('1 cliente cadastrado');
    expect(contagem(3, 'conta', 'aberta')).toBe('3 contas abertas');
  });

  it('data de N dias atrás no formato da API', () => {
    expect(diasAtras(7, new Date(2026, 8, 25))).toBe('2026-09-18');
    expect(diasAtras(30, new Date(2026, 2, 15))).toBe('2026-02-13');
  });
});

import { AgenciaPipe, ContagemPipe } from './formatos';

describe('AgenciaPipe', () => {
  const pipe = new AgenciaPipe();

  it('completa com zeros à esquerda', () => {
    expect(pipe.transform(1)).toBe('AG 0001');
    expect(pipe.transform(1234)).toBe('AG 1234');
  });

  it('devolve vazio sem número', () => {
    expect(pipe.transform(null)).toBe('');
  });
});

describe('ContagemPipe', () => {
  const pipe = new ContagemPipe();

  it('concorda singular e plural', () => {
    expect(pipe.transform(1, 'cliente', 'cadastrado')).toBe('1 cliente cadastrado');
    expect(pipe.transform(3, 'cliente', 'cadastrado')).toBe('3 clientes cadastrados');
    expect(pipe.transform(0, 'conta', 'aberta')).toBe('0 contas abertas');
    expect(pipe.transform(2, 'agência', 'cadastrada')).toBe('2 agências cadastradas');
  });
});

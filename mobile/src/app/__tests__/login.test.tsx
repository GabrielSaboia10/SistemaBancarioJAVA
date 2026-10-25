import { fireEvent, render, screen, waitFor } from '@testing-library/react-native';
import Login from '../login';

const mockLogin = jest.fn();
jest.mock('@/auth/AuthContext', () => ({
  useSessao: () => ({ login: mockLogin }),
}));

beforeEach(() => mockLogin.mockReset());

it('aplica a máscara no CPF e envia o login', async () => {
  mockLogin.mockResolvedValueOnce({});
  await render(<Login />);

  await fireEvent.changeText(screen.getByLabelText('CPF'), '52998224725');
  await fireEvent.changeText(screen.getByLabelText('Senha'), 'cliente123');
  await fireEvent.press(screen.getByRole('button', { name: 'Entrar' }));

  await waitFor(() => expect(mockLogin).toHaveBeenCalledWith('529.982.247-25', 'cliente123'));
});

it('não envia com CPF incompleto e mostra o motivo', async () => {
  await render(<Login />);
  await fireEvent.changeText(screen.getByLabelText('CPF'), '123');
  await fireEvent.press(screen.getByRole('button', { name: 'Entrar' }));

  expect(mockLogin).not.toHaveBeenCalled();
  expect(screen.getByText('Informe um CPF com 11 dígitos')).toBeTruthy();
});

it('mostra a mensagem da API quando o login falha', async () => {
  const { ErroApi } = jest.requireActual('@/api/cliente');
  mockLogin.mockRejectedValueOnce(new ErroApi(401, 'CPF ou senha inválidos'));
  await render(<Login />);

  await fireEvent.changeText(screen.getByLabelText('CPF'), '52998224725');
  await fireEvent.changeText(screen.getByLabelText('Senha'), 'errada');
  await fireEvent.press(screen.getByRole('button', { name: 'Entrar' }));

  expect(await screen.findByText('CPF ou senha inválidos')).toBeTruthy();
});

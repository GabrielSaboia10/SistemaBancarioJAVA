import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { api } from '@/api/cliente';
import type { Usuario } from '@/api/tipos';

interface Sessao {
  usuario: Usuario | null;
  /** true enquanto o app confere se há uma sessão salva */
  carregando: boolean;
  isAdmin: boolean;
  login: (cpf: string, senha: string) => Promise<Usuario>;
  logout: () => Promise<void>;
  trocarSenha: (senhaAtual: string, novaSenha: string) => Promise<void>;
}

const SessaoContext = createContext<Sessao | null>(null);

export function SessaoProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    // O cliente avisa quando a sessão muda (inclusive quando expira sozinha)
    const parar = api.aoMudarSessao(setUsuario);
    api.restaurar().then((u) => {
      setUsuario(u);
      setCarregando(false);
    });
    return () => {
      parar();
    };
  }, []);

  const valor = useMemo<Sessao>(
    () => ({
      usuario,
      carregando,
      isAdmin: usuario?.role === 'ADMIN',
      login: (cpf, senha) => api.login(cpf, senha),
      logout: () => api.logout(),
      trocarSenha: (senhaAtual, novaSenha) => api.trocarSenha(senhaAtual, novaSenha),
    }),
    [usuario, carregando],
  );

  return <SessaoContext.Provider value={valor}>{children}</SessaoContext.Provider>;
}

export function useSessao(): Sessao {
  const sessao = useContext(SessaoContext);
  if (!sessao) throw new Error('useSessao precisa estar dentro de <SessaoProvider>');
  return sessao;
}

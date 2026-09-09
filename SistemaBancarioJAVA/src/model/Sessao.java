package model;

public class Sessao {

	private static Usuario usuarioLogado = null;

	public static Usuario getUsuarioLogado() {
		return usuarioLogado;
	}

	public static void setUsuarioLogado(Usuario u) {
		usuarioLogado = u;
	}

	public static void limpar() {
		usuarioLogado = null;
	}

	public static boolean isAdmin() {
		return usuarioLogado != null && usuarioLogado.getRole() == Role.ADMIN;
	}
}

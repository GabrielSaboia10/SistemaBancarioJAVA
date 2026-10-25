package controller;

import model.Sessao;
import model.SenhaUtil;
import model.Usuario;
import model.dao.DaoUsuario;
import viewer.JanelaAlterarSenha;

public class CtrlAlterarSenha extends CtrlAbstrato {

	private JanelaAlterarSenha janela;

	public CtrlAlterarSenha(ICtrl ctrlPai) {
		super(ctrlPai);
		this.janela = new JanelaAlterarSenha(this);
	}

	public void alterarSenha(String senhaAtual, String novaSenha, String confirmar) {
		if (senhaAtual == null || senhaAtual.isEmpty()) {
			janela.notificar("Informe a senha atual!");
			return;
		}
		if (novaSenha == null || novaSenha.length() < 4) {
			janela.notificar("A nova senha deve ter pelo menos 4 caracteres!");
			return;
		}
		if (!novaSenha.equals(confirmar)) {
			janela.notificar("As novas senhas não conferem!");
			return;
		}
		Usuario u = Sessao.getUsuarioLogado();
		if (!SenhaUtil.verificar(senhaAtual, u.getSenhaHash())) {
			janela.notificar("Senha atual incorreta!");
			return;
		}
		u.setSenhaHash(SenhaUtil.hashSha256(novaSenha));
		new DaoUsuario().alterar(u);
		janela.notificar("Senha alterada com sucesso!");
		this.finalizar();
	}

	@Override
	public void finalizar() {
		if (this.janela != null) this.janela.finalizar();
		this.getCtrlPai().ctrlFilhoFinalizado(this);
	}

	@Override
	public Object getBemTangivel() { return null; }

	@Override
	public void ctrlFilhoFinalizado(ICtrl ctrlFilho) { }
}

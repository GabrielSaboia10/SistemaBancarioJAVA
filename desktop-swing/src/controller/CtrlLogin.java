package controller;

import model.Sessao;
import model.SenhaUtil;
import model.Usuario;
import model.dao.DaoUsuario;
import viewer.JanelaLogin;

public class CtrlLogin extends CtrlAbstrato {

	private JanelaLogin janela;

	public CtrlLogin(ICtrl ctrlPai) {
		super(ctrlPai);
		this.janela = new JanelaLogin(this);
	}

	public void efetuarLogin(String cpf, String senha) {
		if (cpf == null || cpf.isEmpty() || senha == null || senha.isEmpty()) {
			this.janela.notificar("Preencha CPF e senha!");
			return;
		}
		DaoUsuario dao = new DaoUsuario();
		Usuario u = dao.consultarPorCpf(cpf);
		if (u == null || !SenhaUtil.verificar(senha, u.getSenhaHash())) {
			this.janela.notificar("CPF ou senha inválidos!");
			return;
		}
		Sessao.setUsuarioLogado(u);
		this.janela.finalizar();
		this.finalizar();
	}

	@Override
	public void finalizar() {
		if (this.janela != null) this.janela.finalizar();
		this.getCtrlPai().ctrlFilhoFinalizado(this);
	}

	@Override
	public Object getBemTangivel() { return Sessao.getUsuarioLogado(); }

	@Override
	public void ctrlFilhoFinalizado(ICtrl ctrlFilho) { }
}

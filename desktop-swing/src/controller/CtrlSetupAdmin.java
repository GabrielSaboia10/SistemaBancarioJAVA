package controller;

import model.ModelException;
import model.Role;
import model.SenhaUtil;
import model.Usuario;
import model.dao.DaoUsuario;
import viewer.JanelaSetupAdmin;

public class CtrlSetupAdmin extends CtrlAbstrato {

	private JanelaSetupAdmin janela;

	public CtrlSetupAdmin(ICtrl ctrlPai) {
		super(ctrlPai);
		this.janela = new JanelaSetupAdmin(this);
	}

	public void criarAdmin(String cpf, String senha, String confirmar) {
		if (cpf == null || cpf.isEmpty()) {
			janela.notificar("CPF não pode ser vazio!");
			return;
		}
		if (senha == null || senha.length() < 4) {
			janela.notificar("A senha deve ter pelo menos 4 caracteres!");
			return;
		}
		if (!senha.equals(confirmar)) {
			janela.notificar("As senhas não conferem!");
			return;
		}
		try {
			Usuario admin = new Usuario(cpf, SenhaUtil.hashSha256(senha), Role.ADMIN, null);
			boolean ok = new DaoUsuario().incluir(admin);
			if (!ok) {
				janela.notificar("Erro: já existe um usuário com esse CPF!");
				return;
			}
		} catch (ModelException e) {
			janela.notificar("Erro ao criar administrador: " + e.getMessage());
			return;
		}
		janela.notificar("Administrador criado com sucesso! Faça login.");
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

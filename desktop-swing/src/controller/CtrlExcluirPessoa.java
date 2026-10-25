package controller;

import model.dao.DaoPessoa;
import model.dao.DaoUsuario;

public class CtrlExcluirPessoa extends CtrlAbstratoPessoa {

	public CtrlExcluirPessoa(ICtrl c) {
		super(c);
		this.setPessoaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}

	public void efetuar(String cpf, String nome, int idade, String endereco, String telefone) {
		if (this.getPessoaEmEdicao() == null) {
			this.getMeuViewer().notificar("Selecione uma Pessoa primeiro!");
			return;
		}
		DaoPessoa dao = new DaoPessoa();
		dao.remover(this.getPessoaEmEdicao());

		// Remove o usuário vinculado em cascata
		DaoUsuario daoU = new DaoUsuario();
		var u = daoU.consultarPorCpf(cpf);
		if (u != null) daoU.remover(u);

		this.getMeuViewer().notificar("Pessoa excluída: " + this.getPessoaEmEdicao());
		this.finalizar();
	}
}

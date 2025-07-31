package controller;

import model.ModelException;
import model.Pessoa;
import model.dao.DaoPessoa;

public class CtrlExcluirPessoa extends CtrlAbstratoPessoa {

	public CtrlExcluirPessoa(ICtrl c) {
		super(c);
		this.setPessoaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}

	public void efetuar(String cpf, String nome, int idade) {
		try {
			Pessoa p = new Pessoa(cpf, nome, idade);
			this.setPessoaEmEdicao(p);
			this.getMeuViewer().notificar("Pessoa excluida: " + p);
		} catch (ModelException me) {
			this.getMeuViewer().notificar(me.getMessage());
			return;
		}
		DaoPessoa dao = new DaoPessoa();
		dao.remover(this.getPessoaEmEdicao());
		this.finalizar();
	}

}

package controller;

import model.ModelException;
import model.Pessoa;
import model.dao.DaoPessoa;

public class CtrlAlterarPessoa extends CtrlAbstratoPessoa {
	//
	// MÉTODOS
	//
	public CtrlAlterarPessoa(ICtrl c) {
		super(c);
		this.setPessoaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}

	public void efetuar(String cpf, String nome, int idade, String endereco, String telefone) {
		try {
			Pessoa p = this.getPessoaEmEdicao();
			// CPF não é alterado — campo fica desabilitado após busca
			p.setNome(nome);
			p.setIdade(idade);
			p.setEndereco(endereco);
			p.setTelefone(telefone);
		} catch (ModelException e1) {
			this.getMeuViewer().notificar("Erro: " + e1);
			return;
		}
		DaoPessoa dao = new DaoPessoa();
		dao.alterar(this.getPessoaEmEdicao());
		this.getMeuViewer().notificar("Alteração da Pessoa realizada com sucesso!");
		this.finalizar();
	}
}

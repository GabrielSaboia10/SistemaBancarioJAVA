package controller;

import model.ModelException;
import model.Pessoa;
import model.dao.DaoPessoa;

public class CtrlIncluirPessoa extends CtrlAbstratoPessoa {
	//
	// MÉTODOS
	//
	public CtrlIncluirPessoa(ICtrl c) {
		// Executando o construtor de CtrlAbstratoPessoa
		super(c);
		// Informo que não há pessoa sendo editada previamente
		this.setPessoaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}

	public void efetuar(String cpf, String nome, int idade) {
		try {
			Pessoa p = new Pessoa(cpf, nome, idade);
			this.setPessoaEmEdicao(p);
		} catch (ModelException e1) {
			this.getMeuViewer().notificar("Erro: " + e1);
			return;
		}
		DaoPessoa dao = new DaoPessoa();
		boolean inserido = dao.incluir(this.getPessoaEmEdicao());
		if (inserido) {
			this.getMeuViewer().notificar("Inclusão da Pessoa " + getPessoaEmEdicao() + " realizada com sucesso!");
			this.finalizar();
		} else {
			// Aqui você mostra a mensagem de erro pra quem estiver usando a aplicação
			this.getMeuViewer().notificar("Erro ao incluir Pessoa (CPF duplicado ou outro problema).");
		}
	}
}

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
	
	/**
	 * Método disparado por solicitação do viewer para que o controlador
	 * recupere a pessoa com o cpf indicado e repasse os dados para o viewer
	 * @param cpf
	 */
	
	public void efetuar(String cpf, String nome, int idade) {
		try {
			Pessoa p = this.getPessoaEmEdicao();
			p.setCpf(cpf);
			p.setNome(nome);
			p.setIdade(idade);			
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

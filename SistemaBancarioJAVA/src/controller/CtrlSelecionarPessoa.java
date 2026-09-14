package controller;

public class CtrlSelecionarPessoa extends CtrlAbstratoPessoa {

	public CtrlSelecionarPessoa(ICtrl c) {
		super(c);
		this.setPessoaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}

	public void efetuar(String cpf, String nome, int idade, String endereco, String telefone) {
		this.getMeuViewer().notificar("Última Pessoa pesquisada: " + getPessoaEmEdicao());
		this.finalizar();
	}
}

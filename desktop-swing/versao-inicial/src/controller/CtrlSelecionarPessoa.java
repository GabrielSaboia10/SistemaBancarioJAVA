package controller;

public class CtrlSelecionarPessoa extends CtrlAbstratoPessoa {

	public CtrlSelecionarPessoa(ICtrl c) {
		// Executando o construtor de CtrlAbstratoPessoa
		super(c);
		// Informo que não há pessoa sendo editada previamente
		this.setPessoaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}

	public void efetuar(String cpf, String nome, int idade) {
		this.getMeuViewer().notificar("Ultima Pessoa pesquisada: " + getPessoaEmEdicao());
		this.finalizar();
	}

}

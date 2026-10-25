package controller;

public class CtrlSelecionarAgencia extends CtrlAbstratoAgencia {

	public CtrlSelecionarAgencia(ICtrl c) {
		// Executando o construtor de CtrlAbstratoPessoa
		super(c);
		// Informo que não há pessoa sendo editada previamente
		this.setAgenciaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}

	public void efetuar(int numero, String endereco, String cidade) {
		this.getMeuViewer().notificar("Ultima Agencia pesquisada: " + getAgenciaEmEdicao());
		this.finalizar();
	}

}

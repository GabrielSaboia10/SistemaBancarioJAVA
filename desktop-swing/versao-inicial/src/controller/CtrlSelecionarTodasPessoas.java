package controller;

import model.Pessoa;
import viewer.JanelaConsultarPessoas;

public class CtrlSelecionarTodasPessoas extends CtrlAbstrato {

	private JanelaConsultarPessoas viewer;
	private Pessoa pessoa;

	public CtrlSelecionarTodasPessoas(ICtrl c) {
		// Chamando o construtor da superclasse. Irá guardar a
		// informação que o CtrlPrograma é o pai desse controlador.
		super(c);
		this.viewer = new JanelaConsultarPessoas(this);
		this.viewer.apresentar();
	}

	public void finalizar() {
		this.viewer.finalizar();
		this.getCtrlPai().ctrlFilhoFinalizado(this);
	}


	@Override
	public Object getBemTangivel() {
		return this.pessoa;
	}

}

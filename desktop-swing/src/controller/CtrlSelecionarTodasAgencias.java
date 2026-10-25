package controller;

import model.AgenciaBancaria;
import viewer.JanelaConsultarAgencias;

public class CtrlSelecionarTodasAgencias extends CtrlAbstrato {

	private JanelaConsultarAgencias viewer;
	private AgenciaBancaria Agencia;

	public CtrlSelecionarTodasAgencias(ICtrl c) {
		// Chamando o construtor da superclasse. Irá guardar a
		// informação que o CtrlPrograma é o pai desse controlador.
		super(c);
		this.viewer = new JanelaConsultarAgencias(this);
		this.viewer.apresentar();
	}

	public void finalizar() {
		this.viewer.finalizar();
		this.getCtrlPai().ctrlFilhoFinalizado(this);
	}


	@Override
	public Object getBemTangivel() {
		return this.Agencia;
	}

}

package controller;

import model.ContaBancaria;
import viewer.JanelaConsultarContas;
import viewer.JanelaConsultarPessoas;

public class CtrlSelecionarTodasContas extends CtrlAbstrato {

	private JanelaConsultarContas viewer;
	private ContaBancaria conta;

	public CtrlSelecionarTodasContas(ICtrl c) {
		super(c);
		this.viewer = new JanelaConsultarContas(this);
		this.viewer.apresentar();
	}

	public void finalizar() {
		this.viewer.finalizar();
		this.getCtrlPai().ctrlFilhoFinalizado(this);
	}

	
	@Override
	public Object getBemTangivel() {
		return this.conta;
	}

}

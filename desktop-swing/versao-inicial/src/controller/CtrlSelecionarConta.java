package controller;

import model.AgenciaBancaria;
import model.Pessoa;

public class CtrlSelecionarConta extends CtrlAbstratoConta {

	public CtrlSelecionarConta(ICtrl c) {
		super(c);
		this.setContaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}

	public void efetuar(int numero, double limite, double saldo, Pessoa correntista, AgenciaBancaria agencia) {
		this.getMeuViewer().notificar("Ultima Conta pesquisada: " + getContaEmEdicao().getNumContaCorrente());
		this.finalizar();
	}

}

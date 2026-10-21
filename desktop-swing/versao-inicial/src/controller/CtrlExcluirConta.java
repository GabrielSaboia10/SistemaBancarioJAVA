package controller;

import model.AgenciaBancaria;
import model.ContaBancaria;
import model.ModelException;
import model.Pessoa;
import model.dao.DaoContaBancaria;

public class CtrlExcluirConta extends CtrlAbstratoConta {

	public CtrlExcluirConta(ICtrl c) {
		super(c);
		this.setContaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}

	public void efetuar(int numero, double limite, double saldo, Pessoa correntista, AgenciaBancaria agencia) {
		try {
			ContaBancaria c = new ContaBancaria(numero, limite,  saldo,  correntista,  agencia);
			this.setContaEmEdicao(c);
			this.getMeuViewer().notificar("Conta excluida: " + c.getNumContaCorrente());
		} catch (ModelException me) {
			this.getMeuViewer().notificar(me.getMessage());
			return;
		}
		DaoContaBancaria dao = new DaoContaBancaria();
		dao.remover(this.getContaEmEdicao());
		this.finalizar();
	}

}

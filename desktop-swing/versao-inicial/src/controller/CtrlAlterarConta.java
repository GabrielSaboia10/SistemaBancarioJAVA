package controller;

import model.AgenciaBancaria;
import model.ContaBancaria;
import model.ModelException;
import model.Pessoa;
import model.dao.DaoContaBancaria;

public class CtrlAlterarConta extends CtrlAbstratoConta {
	//
	// MÉTODOS
	//
	public CtrlAlterarConta(ICtrl c) {
		super(c);
		this.setContaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}
	
	public void efetuar(int numero, double limite, double saldo, Pessoa correntista, AgenciaBancaria agencia) {
		try {
			ContaBancaria c = this.getContaEmEdicao();
			c.setNumContaCorrente(numero);
			c.setLimiteChequeEspecial(limite);
			c.setSaldo(saldo);
			c.setCorrentista(correntista);
			c.setAgencia(agencia);
		} catch (ModelException e1) {
			this.getMeuViewer().notificar("Erro: " + e1);
			return;
		}
		DaoContaBancaria dao = new DaoContaBancaria();
		dao.alterar(this.getContaEmEdicao());
		this.getMeuViewer().notificar("Alteração da Conta realizada com sucesso!");
		this.finalizar();
	}	
}

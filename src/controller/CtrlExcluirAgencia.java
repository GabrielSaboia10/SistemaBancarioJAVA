package controller;

import model.AgenciaBancaria;
import model.ModelException;
import model.dao.DaoAgenciaBancaria;

public class CtrlExcluirAgencia extends CtrlAbstratoAgencia {

	public CtrlExcluirAgencia(ICtrl c) {
		super(c);
		this.setAgenciaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}

	public void efetuar(int numero, String endereco, String cidade) {
		try {
			AgenciaBancaria p = new AgenciaBancaria(numero, endereco, cidade);
			this.setAgenciaEmEdicao(p);
			this.getMeuViewer().notificar("Agencia excluida: " + p);
		} catch (ModelException me) {
			this.getMeuViewer().notificar(me.getMessage());
			return;
		}
		DaoAgenciaBancaria dao = new DaoAgenciaBancaria();
		dao.remover(getAgenciaEmEdicao());
		this.finalizar();
	}

}

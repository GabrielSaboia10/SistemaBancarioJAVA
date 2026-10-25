package controller;

import model.AgenciaBancaria;
import model.ModelException;
import model.dao.DaoAgenciaBancaria;

public class CtrlIncluirAgenciaBancaria extends CtrlAbstratoAgencia {
	
	
	public CtrlIncluirAgenciaBancaria(ICtrl c) {
		// Chamando o construtor da superclasse. Irá guardar a 
		// informação que o CtrlPrograma é o pai desse controlador.
		super(c);	
		this.setAgenciaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}
	
	public void efetuar(int numero, String endereco, String cidade) {		
		try {
			AgenciaBancaria a = new AgenciaBancaria(numero, endereco, cidade);
			this.setAgenciaEmEdicao(a);
		}
		catch(ModelException me) {
			this.getMeuViewer().notificar(me.getMessage());
			return;
		}		
		DaoAgenciaBancaria dao = new DaoAgenciaBancaria();
		dao.incluir(this.getAgenciaEmEdicao());		
		this.finalizar();		
	}

}

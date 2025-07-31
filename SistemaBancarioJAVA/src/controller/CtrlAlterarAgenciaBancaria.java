package controller;

import model.ModelException;
import model.AgenciaBancaria;
import model.dao.DaoAgenciaBancaria;

public class CtrlAlterarAgenciaBancaria extends CtrlAbstratoAgencia {
	//
	// MÉTODOS
	//
	public CtrlAlterarAgenciaBancaria(ICtrl c) {
		super(c);
		this.setAgenciaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}
	
	/**
	 * Método disparado por solicitação do viewer para que o controlador
	 * recupere a AgenciaBancaria com o cpf indicado e repasse os dados para o viewer
	 * @param cpf
	 */
	
	public void efetuar(int numero, String endereco, String cidade) {
		try {
			AgenciaBancaria p = this.getAgenciaEmEdicao();
			p.setNumero(numero);
			p.setEndereco(endereco);
			p.setCidade(cidade);			
		} catch (ModelException e1) {
			this.getMeuViewer().notificar("Erro: " + e1);
			return;
		}
		DaoAgenciaBancaria dao = new DaoAgenciaBancaria();
		dao.alterar(this.getAgenciaEmEdicao());
		this.getMeuViewer().notificar("Alteração da AgenciaBancaria realizada com sucesso!");
		this.finalizar();
	}

}

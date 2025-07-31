package controller;

import model.AgenciaBancaria;
import model.dao.DaoAgenciaBancaria;
import viewer.JanelaAgenciaBancaria;

abstract public class CtrlAbstratoAgencia extends CtrlAbstrato {
	//
	// ATRIBUTOS 
	// 
	// Todo Controlador deve ter um atributo para guardar a referência
	// para o objeto viewer com quem se relaciona.
	//
	private JanelaAgenciaBancaria meuViewer;
	private AgenciaBancaria       AgenciaEmEdicao;
	
	//
	// MÉTODOS
	//
	public CtrlAbstratoAgencia(ICtrl c) {
		super(c);
		this.meuViewer = new JanelaAgenciaBancaria(this); 
	}
	
	abstract public void efetuar(int numero, String endereco, String cidade);
	
	public void finalizar() {
		this.meuViewer.finalizar();
		this.getCtrlPai().ctrlFilhoFinalizado(this);
	}
	
	public Object getBemTangivel() {
		return this.AgenciaEmEdicao;
	}
	
	public JanelaAgenciaBancaria getMeuViewer() {
		return meuViewer;
	}

	public void setMeuViewer(JanelaAgenciaBancaria meuViewer) {
		this.meuViewer = meuViewer;
	}

	public AgenciaBancaria getAgenciaEmEdicao() {
		return AgenciaEmEdicao;
	}

	public void setAgenciaEmEdicao(AgenciaBancaria AgenciaEmEdicao) {
		this.AgenciaEmEdicao = AgenciaEmEdicao;
	}
	
	public void procurarAgenciaComNumero(int numero) {
		DaoAgenciaBancaria dao = new DaoAgenciaBancaria();
		AgenciaBancaria a  = dao.consultarPorNumero(numero);
		if(a == null) {
			this.getMeuViewer().notificar("Não tem AgenciaBancaria com esse numero! Verifique o valor passado!");
			return;
		}
		this.setAgenciaEmEdicao(a);
		this.getMeuViewer().atualizarDados(a.getNumero(),a.getEndereco(),a.getCidade());
	}
}

package controller;

import model.AgenciaBancaria;
import model.ContaBancaria;
import model.Pessoa;
import model.dao.DaoAgenciaBancaria;
import model.dao.DaoContaBancaria;
import model.dao.DaoPessoa;
import viewer.JanelaContaBancaria;

abstract public class CtrlAbstratoConta extends CtrlAbstrato {
	//
	// ATRIBUTOS 
	// 
	// Todo Controlador deve ter um atributo para guardar a referência
	// para o objeto viewer com quem se relaciona.
	//
	private JanelaContaBancaria meuViewer;
	private ContaBancaria       contaEmEdicao;

	//
	// MÉTODOS
	//
	public CtrlAbstratoConta(ICtrl c) {
		super(c);
		DaoPessoa daoPessoas = new DaoPessoa();
		DaoAgenciaBancaria daoAgenciaBancaria = new DaoAgenciaBancaria();
		this.meuViewer = new JanelaContaBancaria(this, daoPessoas.consultarTodos(), daoAgenciaBancaria.consultarTodos()); 
	}
	
	abstract public void efetuar(int numero, double limite, double saldo, 
            Pessoa correntista, AgenciaBancaria agencia);
	
	public void finalizar() {
		this.meuViewer.finalizar();
		this.getCtrlPai().ctrlFilhoFinalizado(this);
	}
	
	public Object getBemTangivel() {
		return this.contaEmEdicao;
	}
	
	public JanelaContaBancaria getMeuViewer() {
		return meuViewer;
	}

	public void setMeuViewer(JanelaContaBancaria meuViewer) {
		this.meuViewer = meuViewer;
	}

	public ContaBancaria getContaEmEdicao() {
		return contaEmEdicao;
	}

	public void setContaEmEdicao(ContaBancaria contaEmEdicao) {
		this.contaEmEdicao = contaEmEdicao;
	}
	
	public void procurarContaComNumero(int numero) {
		DaoContaBancaria dao = new DaoContaBancaria();
		ContaBancaria c  = dao.consultarPorNumero(numero);
		if(c == null) {
			this.getMeuViewer().notificar("Não tem conta com esse numero! Verifique o valor passado!");
			return;
		}
		this.setContaEmEdicao(c);
		this.getMeuViewer().atualizarDados(c.getNumContaCorrente(),c.getLimiteChequeEspecial(),c.getSaldo(),c.getCorrentista(),c.getAgencia());
	}
}

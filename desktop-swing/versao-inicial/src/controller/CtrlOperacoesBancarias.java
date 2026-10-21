package controller;

import model.ContaBancaria;
import model.ModelException;
import model.dao.DaoContaBancaria;
import viewer.JanelaOperacoesBancarias;

public class CtrlOperacoesBancarias extends CtrlAbstrato {

	private JanelaOperacoesBancarias janela;
	private DaoContaBancaria daoContas;

	public CtrlOperacoesBancarias(ICtrl pai) {
		super(pai);
		daoContas = new DaoContaBancaria();
		ContaBancaria[] contas = daoContas.consultarTodos();
		this.janela = new JanelaOperacoesBancarias(this, contas);
		janela.apresentar();
	}

	public ContaBancaria[] getContas() {
		return daoContas.consultarTodos();
	}

	public void depositar(ContaBancaria conta, double valor) {
		if (conta == null) {
			janela.notificar("Selecione uma conta.");
			return;
		}
		try {
			conta.depositar(valor);
			daoContas.salvarEstado();
			janela.notificar(String.format("Depósito de R$ %.2f realizado com sucesso!\nNovo saldo: R$ %.2f", valor, conta.getSaldo()));
			janela.atualizarSaldo(conta);
		} catch (ModelException e) {
			janela.notificar("Erro: " + e.getMessage());
		}
	}

	public void sacar(ContaBancaria conta, double valor) {
		if (conta == null) {
			janela.notificar("Selecione uma conta.");
			return;
		}
		try {
			conta.sacar(valor);
			daoContas.salvarEstado();
			janela.notificar(String.format("Saque de R$ %.2f realizado com sucesso!\nNovo saldo: R$ %.2f", valor, conta.getSaldo()));
			janela.atualizarSaldo(conta);
		} catch (ModelException e) {
			janela.notificar("Erro: " + e.getMessage());
		}
	}

	public void transferir(ContaBancaria origem, ContaBancaria destino, double valor) {
		if (origem == null || destino == null) {
			janela.notificar("Selecione as contas de origem e destino.");
			return;
		}
		if (origem == destino) {
			janela.notificar("As contas de origem e destino não podem ser a mesma.");
			return;
		}
		try {
			origem.transferir(valor, destino);
			daoContas.salvarEstado();
			janela.notificar(String.format("Transferência de R$ %.2f realizada!\nSaldo conta %d: R$ %.2f",
					valor, origem.getNumContaCorrente(), origem.getSaldo()));
			janela.atualizarSaldo(origem);
		} catch (ModelException e) {
			janela.notificar("Erro: " + e.getMessage());
		}
	}

	@Override
	public void finalizar() {
		janela.finalizar();
		getCtrlPai().ctrlFilhoFinalizado(this);
	}

	@Override
	public Object getBemTangivel() {
		return null;
	}
}

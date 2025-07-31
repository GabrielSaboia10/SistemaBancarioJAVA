package controller;

import viewer.JanelaAbstrata;
import viewer.JanelaCrudConta;

public class CtrlCrudConta extends CtrlAbstrato {

	private JanelaAbstrata meuViewer;
	private CtrlIncluirContaBancaria ctrlIncluirConta;
	private CtrlAlterarConta ctrlAlterarConta;
	private CtrlExcluirConta ctrlExcluirConta;
	private CtrlSelecionarTodasContas ctrlSelecionarTodas;
	private CtrlSelecionarConta ctrlSelecionarConta;

	public CtrlCrudConta(ICtrl c) {
		super(c);
		this.meuViewer = new JanelaCrudConta(this);
		this.ctrlIncluirConta = null;
		this.meuViewer.apresentar();
	}

	public void iniciarIncluirContaBancaria() {
		if (this.ctrlIncluirConta == null)
			this.ctrlIncluirConta = new CtrlIncluirContaBancaria(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Incluir Conta");
	}

	public void iniciarAlterarConta() {
		if (this.ctrlAlterarConta == null)
			this.ctrlAlterarConta = new CtrlAlterarConta(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Alterar Conta");
	}

	public void iniciarExcluirConta() {
		if (this.ctrlExcluirConta == null)
			this.ctrlExcluirConta = new CtrlExcluirConta(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Excluir Conta");
	}

	public void iniciarSelecionarTodasContas() {
		if (this.ctrlSelecionarTodas == null)
			this.ctrlSelecionarTodas = new CtrlSelecionarTodasContas(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Selecionar Todas as Conta");
	}

	public void iniciarSelecionarConta() {
		if (this.ctrlSelecionarConta == null)
			this.ctrlSelecionarConta = new CtrlSelecionarConta(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Selecionar Conta especifica! ");
	}

	public void ctrlFilhoFinalizado(ICtrl ctrlFilho) {
		if (ctrlFilho instanceof CtrlIncluirContaBancaria) {
			// this.correntista = (Conta) this.ctrlIncluirConta.getBemTangivel();
			this.ctrlIncluirConta = null;
		}
		else if (ctrlFilho instanceof CtrlAlterarConta)
			this.ctrlAlterarConta = null;
		else if (ctrlFilho instanceof CtrlExcluirConta)
			this.ctrlExcluirConta = null;
		else if (ctrlFilho instanceof CtrlSelecionarTodasContas)
			this.ctrlSelecionarTodas = null;
		else if (ctrlFilho instanceof CtrlSelecionarConta) 
			this.ctrlSelecionarConta = null;
	}

	public void finalizar() {
		this.meuViewer.finalizar();
		this.getCtrlPai().ctrlFilhoFinalizado(this);
	}

	@Override
	public Object getBemTangivel() {
		// TODO Auto-generated method stub
		return null;
	}

}

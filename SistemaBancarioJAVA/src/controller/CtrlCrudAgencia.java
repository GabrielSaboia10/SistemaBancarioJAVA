package controller;

import viewer.JanelaAbstrata;
import viewer.JanelaCrudAgencia;

public class CtrlCrudAgencia extends CtrlAbstrato {

	private JanelaAbstrata meuViewer;
	private CtrlIncluirAgenciaBancaria ctrlIncluirAgencia;
	private CtrlAlterarAgenciaBancaria ctrlAlterarAgencia;
	private CtrlExcluirAgencia ctrlExcluirAgencia;
	private CtrlSelecionarTodasAgencias ctrlSelecionarTodas;
	private CtrlSelecionarAgencia ctrlSelecionarAgencia;

	public CtrlCrudAgencia(ICtrl c) {
		super(c);
		this.meuViewer = new JanelaCrudAgencia(this);
		this.ctrlIncluirAgencia = null;
		this.meuViewer.apresentar();
	}

	public void iniciarIncluirAgencia() {
		if (this.ctrlIncluirAgencia == null)
			this.ctrlIncluirAgencia = new CtrlIncluirAgenciaBancaria(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Incluir Agencia");
	}

	public void iniciarAlterarAgencia() {
		if (this.ctrlAlterarAgencia == null)
			this.ctrlAlterarAgencia = new CtrlAlterarAgenciaBancaria(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Alterar Agencia");
	}

	public void iniciarExcluirAgencia() {
		if (this.ctrlExcluirAgencia == null)
			this.ctrlExcluirAgencia = new CtrlExcluirAgencia(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Excluir Agencia");
	}

	public void iniciarSelecionarTodasAgencias() {
		if (this.ctrlSelecionarTodas == null)
			this.ctrlSelecionarTodas = new CtrlSelecionarTodasAgencias(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Selecionar Todas as Agencia");
	}

	public void iniciarSelecionarAgencia() {
		if (this.ctrlSelecionarAgencia == null)
			this.ctrlSelecionarAgencia = new CtrlSelecionarAgencia(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Selecionar Agencia especifica! ");
	}

	public void ctrlFilhoFinalizado(ICtrl ctrlFilho) {
		if (ctrlFilho instanceof CtrlIncluirAgenciaBancaria) {
			// this.correntista = (Agencia) this.ctrlIncluirAgencia.getBemTangivel();
			this.ctrlIncluirAgencia = null;
		}
		else if (ctrlFilho instanceof CtrlAlterarAgenciaBancaria)
			this.ctrlAlterarAgencia = null;
		else if (ctrlFilho instanceof CtrlExcluirAgencia)
			this.ctrlExcluirAgencia = null;
		else if (ctrlFilho instanceof CtrlSelecionarTodasAgencias)
			this.ctrlSelecionarTodas = null;
		else if (ctrlFilho instanceof CtrlSelecionarAgencia) {
			this.ctrlSelecionarAgencia = null;
		}
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

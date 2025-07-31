package controller;

import viewer.JanelaAbstrata;
import viewer.JanelaCrudPessoa;

public class CtrlCrudPessoa extends CtrlAbstrato {

	private JanelaAbstrata meuViewer;
	private CtrlIncluirPessoa ctrlIncluirPessoa;
	private CtrlAlterarPessoa ctrlAlterarPessoa;
	private CtrlExcluirPessoa ctrlExcluirPessoa;
	private CtrlSelecionarTodasPessoas ctrlSelecionarTodas;
	private CtrlSelecionarPessoa ctrlSelecionarPessoa;
	// private Pessoa correntista;

	public CtrlCrudPessoa(ICtrl c) {
		super(c);
		this.meuViewer = new JanelaCrudPessoa(this);
		// experimente trocar por
		// this.meuViewer = new ConsolePessoa(this);
		this.ctrlIncluirPessoa = null;
		this.meuViewer.apresentar();
	}

	public void iniciarIncluirPessoa() {
		if (this.ctrlIncluirPessoa == null)
			this.ctrlIncluirPessoa = new CtrlIncluirPessoa(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Incluir Pessoa");
	}

	public void iniciarAlterarPessoa() {
		if (this.ctrlAlterarPessoa == null)
			this.ctrlAlterarPessoa = new CtrlAlterarPessoa(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Alterar Pessoa");
	}

	public void iniciarExcluirPessoa() {
		if (this.ctrlExcluirPessoa == null)
			this.ctrlExcluirPessoa = new CtrlExcluirPessoa(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Excluir Pessoa");
	}

	public void iniciarSelecionarTodasPessoas() {
		if (this.ctrlSelecionarTodas == null)
			this.ctrlSelecionarTodas = new CtrlSelecionarTodasPessoas(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Selecionar Todas as Pessoa");
	}

	public void iniciarSelecionarPessoa() {
		if (this.ctrlSelecionarPessoa == null)
			this.ctrlSelecionarPessoa = new CtrlSelecionarPessoa(this);
		else
			this.meuViewer.notificar("Você já iniciou a funcionalidade de Selecionar Pessoa especifica! ");
	}

	public void ctrlFilhoFinalizado(ICtrl ctrlFilho) {
		if (ctrlFilho instanceof CtrlIncluirPessoa) {
			// this.correntista = (Pessoa) this.ctrlIncluirPessoa.getBemTangivel();
			this.ctrlIncluirPessoa = null;
		} else if (ctrlFilho instanceof CtrlAlterarPessoa)
			this.ctrlAlterarPessoa = null;
		else if (ctrlFilho instanceof CtrlExcluirPessoa)
			this.ctrlExcluirPessoa = null;
		else if (ctrlFilho instanceof CtrlSelecionarTodasPessoas)
			this.ctrlSelecionarTodas = null;
		else if (ctrlFilho instanceof CtrlSelecionarPessoa) {
			this.ctrlSelecionarPessoa = null;
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

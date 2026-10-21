package controller;

import javax.swing.UIManager;
import viewer.JanelaPrincipal;

public class CtrlPrograma extends CtrlAbstrato {

	private JanelaPrincipal              janela;
	private CtrlCrudConta                ctrlCrudConta;
	private CtrlCrudPessoa               ctrlCrudPessoa;
	private CtrlCrudAgencia              ctrlCrudAgencia;
	private CtrlOperacoesBancarias       ctrlOperacoes;

	public CtrlPrograma() {
		super(null);
		this.janela = new JanelaPrincipal(this);
		janela.setLocationRelativeTo(null);
	}

	public void iniciarCrudPessoa() {
		if (ctrlCrudPessoa == null)
			ctrlCrudPessoa = new CtrlCrudPessoa(this);
		else
			janela.notificar("O gerenciamento de pessoas já está aberto.");
	}

	public void iniciarCrudContaBancaria() {
		if (ctrlCrudConta == null)
			ctrlCrudConta = new CtrlCrudConta(this);
		else
			janela.notificar("O gerenciamento de contas já está aberto.");
	}

	public void iniciarIncluirCrudAgenciaBancaria() {
		if (ctrlCrudAgencia == null)
			ctrlCrudAgencia = new CtrlCrudAgencia(this);
		else
			janela.notificar("O gerenciamento de agências já está aberto.");
	}

	public void iniciarOperacoesBancarias() {
		if (ctrlOperacoes == null)
			ctrlOperacoes = new CtrlOperacoesBancarias(this);
		else
			janela.notificar("As operações bancárias já estão abertas.");
	}

	@Override
	public void ctrlFilhoFinalizado(ICtrl ctrlFilho) {
		if (ctrlFilho instanceof CtrlCrudPessoa)           ctrlCrudPessoa = null;
		else if (ctrlFilho instanceof CtrlCrudConta)       ctrlCrudConta = null;
		else if (ctrlFilho instanceof CtrlCrudAgencia)     ctrlCrudAgencia = null;
		else if (ctrlFilho instanceof CtrlOperacoesBancarias) ctrlOperacoes = null;
	}

	public void finalizar() {
		janela.notificar("Encerrando o sistema bancário. Até logo!");
		janela.finalizar();
		System.exit(0);
	}

	public Object getBemTangivel() {
		return null;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			// usa look and feel padrão se Nimbus não estiver disponível
		}
		new CtrlPrograma();
	}
}

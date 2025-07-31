package controller;

import viewer.JanelaPrincipal;

public class CtrlPrograma extends CtrlAbstrato{

	//
	// ATRIBUTOS DE RELACIONAMENTO
	//
	// O CtrlPrograma deve ter um atributo para referenciar a janela
	// com o menu principal do sistema e um atributo para cada caso de
	// uso que o usuário puder disparar a partir desse menu.
	//
	private JanelaPrincipal            janela;
	private CtrlCrudConta 			   ctrlCrudConta;
	private CtrlCrudPessoa 			   ctrlCrudPessoa;
	private CtrlCrudAgencia 		   ctrlCrudAgencia;

	//
	// MÉTODOS
	//
	public CtrlPrograma() {
		super(null);
		this.janela = new JanelaPrincipal(this);
		janela.setLocationRelativeTo(null);
		this.ctrlCrudPessoa = null;
		this.ctrlCrudConta = null;
		this.ctrlCrudAgencia = null;
	}

	public void iniciarCrudPessoa() {
		if (this.ctrlCrudPessoa == null)
			this.ctrlCrudPessoa = new CtrlCrudPessoa(this);
		else
			this.janela.notificar("Você já iniciou a funcionalidade de CRUD Pessoa");
	}

	public void iniciarCrudContaBancaria() {
		if (this.ctrlCrudConta == null)
			this.ctrlCrudConta = new CtrlCrudConta(this);
		else
			this.janela.notificar("Você já iniciou a funcionalidade de CRUD Conta Bancária");
	}

	public void iniciarIncluirCrudAgenciaBancaria() {
		if (this.ctrlCrudAgencia == null)
			this.ctrlCrudAgencia = new CtrlCrudAgencia(this);
		else
			this.janela.notificar("Você já iniciou a funcionalidade de CRUD Agência Bancária");
	}

	@Override
	public void ctrlFilhoFinalizado(ICtrl ctrlFilho) {
		if(ctrlFilho instanceof CtrlCrudPessoa)			
			this.ctrlCrudPessoa = null;
		else if(ctrlFilho instanceof CtrlCrudConta)
			this.ctrlCrudConta = null;
		else if(ctrlFilho instanceof CtrlCrudAgencia)
			this.ctrlCrudAgencia = null;
	}
	/**
	 * Implementação do método presente em ICtrl. 
	 * Fará a finalização do programa
	 */
	public void finalizar() {
		this.janela.notificar("Encerrando o programa!");
		this.janela.finalizar();
		System.exit(0);
	}

	/**
	 * Implementação do método herdado de ICtrl. Neste caso,
	 * como é o controlador do programa, não há bem tangível
	 */
	public Object getBemTangivel() {
		return null;
	}
	
	public static void main(String[] args) {
		new CtrlPrograma();
	}
}

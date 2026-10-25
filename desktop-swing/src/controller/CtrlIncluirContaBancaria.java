package controller;

import model.AgenciaBancaria;
import model.ContaBancaria;
import model.ModelException;
import model.Pessoa;
import model.dao.DaoContaBancaria;

public class CtrlIncluirContaBancaria extends CtrlAbstratoConta {
	//
	// ATRIBUTOS
	//
	// Todo Controlador deve ter um atributo para guardar a referência
	// para o objeto viewer com quem se relaciona.
	//
	private ContaBancaria contaBancariaCriada;
	// Como teremos uma relação entre o caso de uso "Incluir Conta Bancária"
	// com "Incluir Pessoa", então precisamos colocar esse atributo para
	// disparar a execução desse caso de uso <<include>>
	private CtrlIncluirPessoa ctrlIncluirPessoa;
	private Pessoa            correntista;
	// Como teremos uma relação entre o caso de uso "Incluir Conta Bancária"
	// com "Incluir Agência Bancária", então precisamos colocar esse atributo para
	// disparar a execução desse caso de uso <<include>>
	private CtrlIncluirAgenciaBancaria ctrlIncluirAgenciaBancaria;
	private AgenciaBancaria agencia;
	
	//
	// MÉTODOS
	//
	public CtrlIncluirContaBancaria(ICtrl c) {
		super(c);
		this.ctrlIncluirPessoa = null;
		this.ctrlIncluirAgenciaBancaria = null;
		this.getMeuViewer().apresentar();
	}

	/**
	 * Método usado pelo viewer para indicar que o usuário quer executar o
	 * caso de uso "Incluir Pessoa" (relacionamento de <<extend>>)
	 */
	public void iniciarIncluirPessoa() {
		if (this.ctrlIncluirPessoa == null)
			this.ctrlIncluirPessoa = new CtrlIncluirPessoa(this);
		else
			this.getMeuViewer().notificar("Você já iniciou a funcionalidade de Incluir Pessoa");
	}

	/**
	 * Método usado pelo viewer para indicar que o usuário quer executar o
	 * caso de uso "Incluir Agência Bancária" (relacionamento de <<extend>>)
	 */
	public void iniciarIncluirAgenciaBancaria() {
		if (this.ctrlIncluirAgenciaBancaria == null)
			this.ctrlIncluirAgenciaBancaria = new CtrlIncluirAgenciaBancaria(this);
		else
			this.getMeuViewer().notificar("Você já iniciou a funcionalidade de Incluir Agência Bancária");
	}

	@Override
	public void ctrlFilhoFinalizado(ICtrl ctrlFilho) {
		if (ctrlFilho instanceof CtrlIncluirPessoa) {
			this.correntista = (Pessoa)this.ctrlIncluirPessoa.getBemTangivel();
			if(this.correntista != null)
				this.getMeuViewer().atualizarCorrentista(this.correntista);
			this.ctrlIncluirPessoa = null;
		} else if (ctrlFilho instanceof CtrlIncluirAgenciaBancaria) {			
			this.agencia = (AgenciaBancaria)this.ctrlIncluirAgenciaBancaria.getBemTangivel();
			if(this.agencia != null)
				this.getMeuViewer().atualizarAgencia(this.agencia);
			this.ctrlIncluirAgenciaBancaria = null;
		}
	}

	public void efetuar(int numero, double limite, double saldo, 
			                    Pessoa correntista, AgenciaBancaria agencia) {
		try {
			if(correntista == null) {
				this.getMeuViewer().notificar("Você ainda não definiu o correntista");
				return;
			}
			if(agencia == null) {
				this.getMeuViewer().notificar("Você ainda não definiu a agência");
				return;
			}
			this.contaBancariaCriada = new ContaBancaria(numero, limite, saldo,  
					                                     correntista, agencia);
		} catch (ModelException e1) {
			this.getMeuViewer().notificar("Erro: " + e1);
			return;
		}
		DaoContaBancaria dao = new DaoContaBancaria();
		dao.incluir(this.contaBancariaCriada);
		this.getMeuViewer().notificar("Conta criada: " + contaBancariaCriada.getNumContaCorrente());
		//System.out.println("Conta " + contaBancariaCriada.getNumContaCorrente());
		this.finalizar();
	}


	/**
	 * Retorna a referência para a conta bancária gerada (se tudo ocorreu
	 * corretamente) ou null (se o caso de uso não terminou ou se houve falha na
	 * execução)
	 */

}

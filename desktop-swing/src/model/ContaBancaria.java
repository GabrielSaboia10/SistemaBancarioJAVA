package model;

public class ContaBancaria {

	//
	// LEMBRE-SE! 'this' é a referência para o objeto que estiver
	//            executando o método em questão. 
	//
	//            Se o método é NÃO ESTÁTICO, então sempre tem um 
	//            objeto responsável pela execução do método.
	//
	
	
	//
	// CONSTANTES
	//
	final public static int VALOR_MIN_NUM_CONTA = 1000; 
	final public static int VALOR_MAX_NUM_CONTA = 99999; 
	final public static double VALOR_MAX_LIMITE = 30000.00; 

	//
	// ATRIBUTOS
	// 
	private int    numContaCorrente;
	private double limiteChequeEspecial;
	private double saldo;
	
	//
	// ATRIBUTO DE RELACIONAMENTO
	//
	private Pessoa correntista;
	private AgenciaBancaria agencia;
	
	//
	// MÉTODOS
	//
	public ContaBancaria(int numContaCorrente, 
			             double limiteChequeEspecial, 
			             double saldo,
			             Pessoa correntista,
			             AgenciaBancaria agencia) throws ModelException {
		super();
		this.setNumContaCorrente(numContaCorrente);
		this.setLimiteChequeEspecial(limiteChequeEspecial);
		this.setSaldo(saldo);
		this.setCorrentista(correntista);
		this.setAgencia(agencia);
	}

	public int getNumContaCorrente() {
		return this.numContaCorrente;
	}

	public void setNumContaCorrente(int numContaCorrente) throws ModelException{
		ContaBancaria.validarNumContaCorrente(numContaCorrente);
		this.numContaCorrente = numContaCorrente;
	}

	public double getLimiteChequeEspecial() {
		return this.limiteChequeEspecial;
	}

	public void setLimiteChequeEspecial(double limiteChequeEspecial) throws ModelException{
		ContaBancaria.validarLimiteChequeEspecial(limiteChequeEspecial);
		this.limiteChequeEspecial = limiteChequeEspecial;
	}

	public double getSaldo() {
		return this.saldo;
	}

	public void setSaldo(double saldo) throws ModelException {
		ContaBancaria.validarSaldo(saldo, this.limiteChequeEspecial);
		this.saldo = saldo;
	}
	
	public Pessoa getCorrentista() {
		return this.correntista;
	}

	public void setCorrentista(Pessoa correntista) throws ModelException {
		ContaBancaria.validarCorrentista(correntista);
		this.correntista = correntista;
	}
	
	public AgenciaBancaria getAgencia() {
		return this.agencia;
	}

	public void setAgencia(AgenciaBancaria agencia) throws ModelException {
		ContaBancaria.validarAgencia(agencia);
		this.agencia = agencia;
	}
	
	//
	// Métodos de Validação
	//	
	public static void validarNumContaCorrente(int numero) throws ModelException {
		if(numero < VALOR_MIN_NUM_CONTA|| numero > VALOR_MAX_NUM_CONTA)
			throw new ModelException("Número de Conta Inválido: " + numero);
	}

	public static void validarLimiteChequeEspecial(double limite) throws ModelException {
		if(limite < 0 || limite > VALOR_MAX_LIMITE)
			throw new ModelException("Valor Inválido para o limite: " + limite);
	}

	public static void validarSaldo(double saldo, double limite) throws ModelException {
		if(saldo < -VALOR_MAX_LIMITE)
			throw new ModelException("O saldo (" + saldo + ") não pode ser menor que valor do limite da conta: " + limite);
	}

	public static void validarCorrentista(Pessoa correntista) throws ModelException {
		if(correntista == null)
			throw new ModelException("É obrigatório indicar quem é o correntista dono da conta");
	}

	public static void validarAgencia(AgenciaBancaria agencia) throws ModelException {
		if(agencia == null)
			throw new ModelException("É obrigatório indicar qual é a agência da conta");
	}
}



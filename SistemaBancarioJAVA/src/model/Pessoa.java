package model;

public class Pessoa {
	//
	// CONSTANTES
	//
	final public static int TAM_CPF         = 14;
	final public static int TAM_MAXIMO_NOME = 40;
	final public static int IDADE_MAXIMA    = 150;
	
	// 
	// ATRIBUTOS
	//
	private String cpf;
	private String nome;
	private int    idade;
		
	//
	// MÉTODOS
	//
	public Pessoa(String cpf, String nome, int idade) throws ModelException {
		super();
		this.setCpf(cpf);
		this.setNome(nome);
		this.setIdade(idade);
	}

	public String getCpf() {
		return this.cpf;
	}

	public void setCpf(String cpf) throws ModelException {
		Pessoa.validarCpf(cpf);
		this.cpf = cpf;
	}

	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) throws ModelException {
		Pessoa.validarNome(nome);
		this.nome = nome;
	}

	public int getIdade() {
		return this.idade;
	}

	public void setIdade(int idade) throws ModelException {
		Pessoa.validarIdade(idade);
		this.idade = idade;
	}

	public String toString() {
		return this.nome;
	}
	//
	// Métodos de Validação
	//
	public static void validarCpf(String cpf) throws ModelException {
		if(cpf == null || cpf.length() == 0 || cpf.isEmpty())
			throw new ModelException("O CPF não pode ser nulo!");
		if(cpf.length() != TAM_CPF)
			throw new ModelException("O CPF deve ter " + TAM_CPF + " caracteres!");
	}
	
	public static void validarNome(String nome) throws ModelException {
		if(nome == null || nome.length() == 0)
			throw new ModelException("O nome da Pessoa não pode ser nulo!");
		if(nome.length() > TAM_MAXIMO_NOME)
			throw new ModelException("O nome da Pessoa deve ter até " + 
		                             TAM_MAXIMO_NOME + " caracteres!");
	}
	
	public static void validarIdade(int idade) throws ModelException {
		if(idade < 0 || idade > IDADE_MAXIMA)
			throw new ModelException("A idade indicada é inválida: " + idade);
	}
}

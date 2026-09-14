package model;

import java.io.Serializable;

public class Pessoa implements Serializable {

	private static final long serialVersionUID = 1L;

	//
	// CONSTANTES
	//
	final public static int TAM_CPF          = 14;
	final public static int TAM_MAXIMO_NOME  = 40;
	final public static int IDADE_MAXIMA     = 150;
	final public static int TAM_MAX_ENDERECO = 100;
	final public static int TAM_MAX_TELEFONE = 15;

	//
	// ATRIBUTOS
	//
	private String cpf;
	private String nome;
	private int    idade;
	private String endereco;
	private String telefone;

	//
	// MÉTODOS
	//
	public Pessoa(String cpf, String nome, int idade, String endereco, String telefone) throws ModelException {
		super();
		this.setCpf(cpf);
		this.setNome(nome);
		this.setIdade(idade);
		this.setEndereco(endereco);
		this.setTelefone(telefone);
	}

	public String getCpf() { return this.cpf; }

	public void setCpf(String cpf) throws ModelException {
		Pessoa.validarCpf(cpf);
		this.cpf = cpf;
	}

	public String getNome() { return this.nome; }

	public void setNome(String nome) throws ModelException {
		Pessoa.validarNome(nome);
		this.nome = nome;
	}

	public int getIdade() { return this.idade; }

	public void setIdade(int idade) throws ModelException {
		Pessoa.validarIdade(idade);
		this.idade = idade;
	}

	public String getEndereco() { return this.endereco; }

	public void setEndereco(String endereco) throws ModelException {
		Pessoa.validarEndereco(endereco);
		this.endereco = endereco;
	}

	public String getTelefone() { return this.telefone; }

	public void setTelefone(String telefone) throws ModelException {
		Pessoa.validarTelefone(telefone);
		this.telefone = telefone;
	}

	@Override
	public String toString() { return this.nome; }

	//
	// Métodos de Validação
	//
	public static void validarCpf(String cpf) throws ModelException {
		if (cpf == null || cpf.length() == 0 || cpf.isEmpty())
			throw new ModelException("O CPF não pode ser nulo!");
		if (cpf.length() != TAM_CPF)
			throw new ModelException("O CPF deve ter " + TAM_CPF + " caracteres!");
	}

	public static void validarNome(String nome) throws ModelException {
		if (nome == null || nome.length() == 0)
			throw new ModelException("O nome da Pessoa não pode ser nulo!");
		if (nome.length() > TAM_MAXIMO_NOME)
			throw new ModelException("O nome da Pessoa deve ter até " + TAM_MAXIMO_NOME + " caracteres!");
	}

	public static void validarIdade(int idade) throws ModelException {
		if (idade < 0 || idade > IDADE_MAXIMA)
			throw new ModelException("A idade indicada é inválida: " + idade);
	}

	public static void validarEndereco(String endereco) throws ModelException {
		if (endereco == null || endereco.isEmpty())
			throw new ModelException("O endereço não pode ser vazio!");
		if (endereco.length() > TAM_MAX_ENDERECO)
			throw new ModelException("O endereço deve ter até " + TAM_MAX_ENDERECO + " caracteres!");
		if (!endereco.matches("[a-zA-ZÀ-ÿ0-9 ,.ºª-]+"))
			throw new ModelException("O endereço contém caracteres inválidos!");
	}

	public static void validarTelefone(String telefone) throws ModelException {
		if (telefone == null || telefone.isEmpty())
			throw new ModelException("O telefone não pode ser vazio!");
		if (telefone.length() > TAM_MAX_TELEFONE)
			throw new ModelException("O telefone deve ter até " + TAM_MAX_TELEFONE + " caracteres!");
		if (!telefone.matches("[0-9() +-]+"))
			throw new ModelException("O telefone contém caracteres inválidos!");
	}
}

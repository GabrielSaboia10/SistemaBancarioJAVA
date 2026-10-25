package model;

public class AgenciaBancaria {
	//
	// CONSTANTES
	//
	final public static int TAM_ENDERECO = 100;
	final public static int TAM_CIDADE   = 25;
	final public static int NUM_MAX_AGENCIA = 10000;
	
	//
	// ATRIBUTOS
	//
	private int    numero;
	private String endereco;
	private String cidade;
	
	public AgenciaBancaria(int numero, String endereco, String cidade) throws ModelException {
		super();
		this.setNumero(numero);
		this.setEndereco(endereco);
		this.setCidade(cidade);
	}

	public int getNumero() {
		return this.numero;
	}

	public void setNumero(int numero) throws ModelException{
		AgenciaBancaria.validarNumero(numero);
		this.numero = numero;
	}

	public String getEndereco() {
		return this.endereco;
	}

	public void setEndereco(String endereco) throws ModelException {
		AgenciaBancaria.validarEndereco(endereco);
		this.endereco = endereco;
	}

	public String getCidade() {
		return this.cidade;
	}

	public void setCidade(String cidade) throws ModelException{
		AgenciaBancaria.validarCidade(cidade);
		this.cidade = cidade;
	}

	public String toString() {
		return this.numero + "-" + this.cidade;
	}
	//
	// Métodos de Validação
	//
	public static void validarEndereco(String endereco) throws ModelException {
		if(endereco == null || endereco .length() == 0)
			throw new ModelException("O endereço não pode ser nulo!");
		if(endereco.length() > TAM_ENDERECO)
			throw new ModelException("O endereço deve ter até " + TAM_ENDERECO + " caracteres!");
		for(int i = 0; i < endereco.length(); i++) {
			char c = endereco.charAt(i);
			if(!Character.isAlphabetic(c) && !Character.isDigit(c) &&
			   !Character.isSpaceChar(c))
				throw new ModelException("O endereço tem um caracter inválido na posição " + i + ": " + c);	
		}
	}
	
	public static void validarCidade(String cidade) throws ModelException {
		if(cidade == null || cidade.length() == 0)
			throw new ModelException("A cidade não pode ser nula!");
		if(cidade.length() > TAM_CIDADE)
			throw new ModelException("A cidade deve ter até " + TAM_ENDERECO + " caracteres!");
		for(int i = 0; i < cidade.length(); i++) {
			char c = cidade.charAt(i);
			if(!Character.isAlphabetic(c) && !Character.isDigit(c) &&
			   !Character.isSpaceChar(c))
				throw new ModelException("A cidade tem um caracter inválido na posição " + i + ": " + c);	
		}
	}
	
	public static void validarNumero(int numero) throws ModelException {
		if(numero < 0 || numero > NUM_MAX_AGENCIA)
			throw new ModelException("O número da agência é inválido: " + numero);
	}
}

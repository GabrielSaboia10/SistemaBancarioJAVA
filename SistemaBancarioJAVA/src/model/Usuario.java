package model;

import java.io.Serializable;

public class Usuario implements Serializable {

	private static final long serialVersionUID = 1L;

	final public static int TAM_CPF     = 14;
	final public static int TAM_SENHA   = 64; // SHA-256 hex

	private String  cpf;
	private String  senhaHash;
	private Role    role;
	private Pessoa  pessoaVinculada; // null para ADMIN

	public Usuario(String cpf, String senhaHash, Role role, Pessoa pessoaVinculada) throws ModelException {
		this.setCpf(cpf);
		this.senhaHash = senhaHash;
		this.role = role;
		this.pessoaVinculada = pessoaVinculada;
	}

	public String getCpf() { return cpf; }

	public void setCpf(String cpf) throws ModelException {
		if (cpf == null || cpf.length() != TAM_CPF)
			throw new ModelException("CPF do usuário deve ter " + TAM_CPF + " caracteres!");
		this.cpf = cpf;
	}

	public String getSenhaHash() { return senhaHash; }

	public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

	public Role getRole() { return role; }

	public void setRole(Role role) { this.role = role; }

	public Pessoa getPessoaVinculada() { return pessoaVinculada; }

	public void setPessoaVinculada(Pessoa pessoaVinculada) { this.pessoaVinculada = pessoaVinculada; }

	@Override
	public String toString() {
		return cpf + " [" + role + "]";
	}
}

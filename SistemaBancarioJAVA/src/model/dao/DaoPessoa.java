package model.dao;

import java.util.HashSet;
import java.util.Set;

import model.Pessoa;

public class DaoPessoa {
	//
	// ATRIBUTOS
	//
	private static Set<Pessoa> conjPessoas = new HashSet<Pessoa>();
	
	//
	// MÉTODOS
	//
	public DaoPessoa() {		
	}
	
	/**
	 * Método para darmos "persistência" a um novo objeto Pessoa
	 * @param p
	 * @return
	 */
	public boolean incluir(Pessoa p) {
		return DaoPessoa.conjPessoas.add(p);
	}
	
	public boolean alterar(Pessoa p) {
		return DaoPessoa.conjPessoas.add(p);
	}
	
	public boolean remover(Pessoa p) {
		return DaoPessoa.conjPessoas.remove(p);
	}
	
	public Pessoa consultarPorCpf(String cpf) {
		for(Pessoa p : DaoPessoa.conjPessoas) 
			if(p.getCpf().equals(cpf))
				return p;
		return null;
	}
	
	public Pessoa[] consultarTodos() {
		int numElementos = DaoPessoa.conjPessoas.size();
		Pessoa[] arrayRetorno = new Pessoa[numElementos];
		int i = 0;
		for(Pessoa p : DaoPessoa.conjPessoas) 
			arrayRetorno[i++] = p;
		return arrayRetorno;
	}
}

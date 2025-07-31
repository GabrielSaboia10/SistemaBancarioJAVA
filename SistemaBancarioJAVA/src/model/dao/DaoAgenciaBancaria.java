package model.dao;

import java.util.HashSet;
import java.util.Set;

import model.AgenciaBancaria;

public class DaoAgenciaBancaria {
	//
	// ATRIBUTOS
	//
	private static Set<AgenciaBancaria> conjAgenciasBancarias = new HashSet<AgenciaBancaria>();
	
	//
	// MÉTODOS
	//
	public DaoAgenciaBancaria() {		
	}
	
	/**
	 * Método para darmos "persistência" a um novo objeto AgenciaBancaria
	 * @param p
	 * @return
	 */
	public boolean incluir(AgenciaBancaria p) {
		return DaoAgenciaBancaria.conjAgenciasBancarias.add(p);
	}
	
	public boolean alterar(AgenciaBancaria p) {
		return DaoAgenciaBancaria.conjAgenciasBancarias.add(p);
	}
	
	public boolean remover(AgenciaBancaria p) {
		return DaoAgenciaBancaria.conjAgenciasBancarias.remove(p);
	}
	
	public AgenciaBancaria consultarPorNumero(int numero) {
		for(AgenciaBancaria c : DaoAgenciaBancaria.conjAgenciasBancarias) 
			if(c.getNumero() == numero)
				return c;
		return null;
	}
	
	public AgenciaBancaria[] consultarTodos() {
		int numElementos = DaoAgenciaBancaria.conjAgenciasBancarias.size();
		AgenciaBancaria[] arrayRetorno = new AgenciaBancaria[numElementos];
		int i = 0;
		for(AgenciaBancaria p : DaoAgenciaBancaria.conjAgenciasBancarias) 
			arrayRetorno[i++] = p;
		return arrayRetorno;
	}
}

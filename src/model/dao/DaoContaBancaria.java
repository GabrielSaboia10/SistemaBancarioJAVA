package model.dao;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import model.ContaBancaria;

public class DaoContaBancaria {

	private static final String ARQUIVO = "dados/contas.dat";
	private static Set<ContaBancaria> conjContasBancarias = null;

	public DaoContaBancaria() {
		if (conjContasBancarias == null)
			conjContasBancarias = carregar();
	}

	public boolean incluir(ContaBancaria p) {
		boolean resultado = conjContasBancarias.add(p);
		if (resultado) salvar();
		return resultado;
	}

	public boolean alterar(ContaBancaria p) {
		removerPorNumero(p.getNumContaCorrente());
		boolean resultado = conjContasBancarias.add(p);
		if (resultado) salvar();
		return resultado;
	}

	public boolean remover(ContaBancaria p) {
		ContaBancaria existente = consultarPorNumero(p.getNumContaCorrente());
		if (existente != null) {
			boolean resultado = conjContasBancarias.remove(existente);
			if (resultado) salvar();
			return resultado;
		}
		return false;
	}

	public ContaBancaria consultarPorNumero(int numero) {
		for (ContaBancaria c : conjContasBancarias)
			if (c.getNumContaCorrente() == numero)
				return c;
		return null;
	}

	public ContaBancaria[] consultarTodos() {
		return conjContasBancarias.toArray(new ContaBancaria[0]);
	}

	public void salvarEstado() {
		salvar();
	}

	private void removerPorNumero(int numero) {
		conjContasBancarias.removeIf(c -> c.getNumContaCorrente() == numero);
	}

	@SuppressWarnings("unchecked")
	private Set<ContaBancaria> carregar() {
		File f = new File(ARQUIVO);
		if (!f.exists()) return new HashSet<>();
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
			return (Set<ContaBancaria>) ois.readObject();
		} catch (Exception e) {
			return new HashSet<>();
		}
	}

	private void salvar() {
		new File("dados").mkdirs();
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO))) {
			oos.writeObject(conjContasBancarias);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

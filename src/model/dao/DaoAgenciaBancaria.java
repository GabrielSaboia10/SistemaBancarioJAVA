package model.dao;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import model.AgenciaBancaria;

public class DaoAgenciaBancaria {

	private static final String ARQUIVO = "dados/agencias.dat";
	private static Set<AgenciaBancaria> conjAgenciasBancarias = null;

	public DaoAgenciaBancaria() {
		if (conjAgenciasBancarias == null)
			conjAgenciasBancarias = carregar();
	}

	public boolean incluir(AgenciaBancaria p) {
		boolean resultado = conjAgenciasBancarias.add(p);
		if (resultado) salvar();
		return resultado;
	}

	public boolean alterar(AgenciaBancaria p) {
		removerPorNumero(p.getNumero());
		boolean resultado = conjAgenciasBancarias.add(p);
		if (resultado) salvar();
		return resultado;
	}

	public boolean remover(AgenciaBancaria p) {
		AgenciaBancaria existente = consultarPorNumero(p.getNumero());
		if (existente != null) {
			boolean resultado = conjAgenciasBancarias.remove(existente);
			if (resultado) salvar();
			return resultado;
		}
		return false;
	}

	public AgenciaBancaria consultarPorNumero(int numero) {
		for (AgenciaBancaria a : conjAgenciasBancarias)
			if (a.getNumero() == numero)
				return a;
		return null;
	}

	public AgenciaBancaria[] consultarTodos() {
		return conjAgenciasBancarias.toArray(new AgenciaBancaria[0]);
	}

	private void removerPorNumero(int numero) {
		conjAgenciasBancarias.removeIf(a -> a.getNumero() == numero);
	}

	@SuppressWarnings("unchecked")
	private Set<AgenciaBancaria> carregar() {
		File f = new File(ARQUIVO);
		if (!f.exists()) return new HashSet<>();
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
			return (Set<AgenciaBancaria>) ois.readObject();
		} catch (Exception e) {
			return new HashSet<>();
		}
	}

	private void salvar() {
		new File("dados").mkdirs();
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO))) {
			oos.writeObject(conjAgenciasBancarias);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

package model.dao;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import model.Pessoa;

public class DaoPessoa {

	private static final String ARQUIVO = "dados/pessoas.dat";
	private static Set<Pessoa> conjPessoas = null;

	public DaoPessoa() {
		if (conjPessoas == null)
			conjPessoas = carregar();
	}

	public boolean incluir(Pessoa p) {
		boolean resultado = conjPessoas.add(p);
		if (resultado) salvar();
		return resultado;
	}

	public boolean alterar(Pessoa p) {
		removerPorCpf(p.getCpf());
		boolean resultado = conjPessoas.add(p);
		if (resultado) salvar();
		return resultado;
	}

	public boolean remover(Pessoa p) {
		boolean resultado = conjPessoas.remove(p);
		if (resultado) salvar();
		return resultado;
	}

	public Pessoa consultarPorCpf(String cpf) {
		for (Pessoa p : conjPessoas)
			if (p.getCpf().equals(cpf))
				return p;
		return null;
	}

	public Pessoa[] consultarTodos() {
		return conjPessoas.toArray(new Pessoa[0]);
	}

	private void removerPorCpf(String cpf) {
		conjPessoas.removeIf(p -> p.getCpf().equals(cpf));
	}

	@SuppressWarnings("unchecked")
	private Set<Pessoa> carregar() {
		File f = new File(ARQUIVO);
		if (!f.exists()) return new HashSet<>();
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
			return (Set<Pessoa>) ois.readObject();
		} catch (Exception e) {
			return new HashSet<>();
		}
	}

	private void salvar() {
		new File("dados").mkdirs();
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO))) {
			oos.writeObject(conjPessoas);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

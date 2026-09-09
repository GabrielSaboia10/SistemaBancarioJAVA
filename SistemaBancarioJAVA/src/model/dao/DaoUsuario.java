package model.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import model.Usuario;

public class DaoUsuario {

	private static final String ARQUIVO = "usuarios.dat";
	private static Set<Usuario> conjUsuarios = null;

	public DaoUsuario() {
		if (conjUsuarios == null)
			conjUsuarios = carregarDoArquivo();
	}

	@SuppressWarnings("unchecked")
	private Set<Usuario> carregarDoArquivo() {
		File f = new File(ARQUIVO);
		if (!f.exists())
			return new HashSet<>();
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
			return (Set<Usuario>) ois.readObject();
		} catch (Exception e) {
			return new HashSet<>();
		}
	}

	private void salvarNoArquivo() {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARQUIVO))) {
			oos.writeObject(conjUsuarios);
		} catch (Exception e) {
			// silently fail — in-memory state is still valid
		}
	}

	public boolean incluir(Usuario u) {
		boolean ok = conjUsuarios.add(u);
		if (ok) salvarNoArquivo();
		return ok;
	}

	public boolean alterar(Usuario u) {
		conjUsuarios.removeIf(x -> x.getCpf().equals(u.getCpf()));
		conjUsuarios.add(u);
		salvarNoArquivo();
		return true;
	}

	public boolean remover(Usuario u) {
		boolean ok = conjUsuarios.removeIf(x -> x.getCpf().equals(u.getCpf()));
		if (ok) salvarNoArquivo();
		return ok;
	}

	public Usuario consultarPorCpf(String cpf) {
		for (Usuario u : conjUsuarios)
			if (u.getCpf().equals(cpf))
				return u;
		return null;
	}

	public Usuario[] consultarTodos() {
		return conjUsuarios.toArray(new Usuario[0]);
	}

	public boolean existeAdmin() {
		for (Usuario u : conjUsuarios)
			if (u.getRole() == model.Role.ADMIN)
				return true;
		return false;
	}
}

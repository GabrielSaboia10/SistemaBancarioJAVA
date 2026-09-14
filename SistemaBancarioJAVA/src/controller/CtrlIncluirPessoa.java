package controller;

import model.ModelException;
import model.Pessoa;
import model.Role;
import model.SenhaUtil;
import model.Usuario;
import model.dao.DaoPessoa;
import model.dao.DaoUsuario;

public class CtrlIncluirPessoa extends CtrlAbstratoPessoa {
	//
	// MÉTODOS
	//
	public CtrlIncluirPessoa(ICtrl c) {
		super(c);
		this.setPessoaEmEdicao(null);
		this.getMeuViewer().apresentar();
	}

	public void efetuar(String cpf, String nome, int idade, String endereco, String telefone) {
		Pessoa p;
		try {
			p = new Pessoa(cpf, nome, idade, endereco, telefone);
			this.setPessoaEmEdicao(p);
		} catch (ModelException e1) {
			this.getMeuViewer().notificar("Erro: " + e1);
			return;
		}

		DaoPessoa dao = new DaoPessoa();
		boolean inserido = dao.incluir(p);
		if (!inserido) {
			this.getMeuViewer().notificar("Erro ao incluir Pessoa (CPF duplicado ou outro problema).");
			return;
		}

		// Auto-cria usuário CLIENTE com senha inicial = últimos 6 chars do CPF
		String senhaInicial = cpf.substring(cpf.length() - 6);
		try {
			Usuario u = new Usuario(cpf, SenhaUtil.hashSha256(senhaInicial), Role.CLIENTE, p);
			new DaoUsuario().incluir(u);
			this.getMeuViewer().notificar(
				"Pessoa " + p + " incluída!\n" +
				"Usuário criado automaticamente.\nSenha inicial: " + senhaInicial
			);
		} catch (ModelException e) {
			this.getMeuViewer().notificar("Pessoa incluída, mas erro ao criar usuário: " + e.getMessage());
		}

		this.finalizar();
	}
}

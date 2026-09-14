package controller;

import model.Role;
import model.Sessao;
import model.dao.DaoUsuario;
import viewer.JanelaPrincipal;
import viewer.JanelaPrincipalCliente;

public class CtrlPrograma extends CtrlAbstrato {

	//
	// ATRIBUTOS DE RELACIONAMENTO — menu admin
	//
	private JanelaPrincipal        janelaAdmin;
	private JanelaPrincipalCliente janelaCliente;
	private CtrlCrudConta          ctrlCrudConta;
	private CtrlCrudPessoa         ctrlCrudPessoa;
	private CtrlCrudAgencia        ctrlCrudAgencia;

	// menu cliente
	private CtrlMinhasContas  ctrlMinhasContas;
	private CtrlMeusDados     ctrlMeusDados;
	private CtrlAlterarSenha  ctrlAlterarSenha;

	// autenticação
	private CtrlLogin      ctrlLogin;
	private CtrlSetupAdmin ctrlSetupAdmin;

	//
	// MÉTODOS
	//
	public CtrlPrograma() {
		super(null);
		iniciarAutenticacao();
	}

	private void iniciarAutenticacao() {
		DaoUsuario dao = new DaoUsuario();
		if (!dao.existeAdmin()) {
			this.ctrlSetupAdmin = new CtrlSetupAdmin(this);
		} else {
			this.ctrlLogin = new CtrlLogin(this);
		}
	}

	private void abrirMenuPorRole() {
		if (Sessao.isAdmin()) {
			this.janelaAdmin = new JanelaPrincipal(this);
			this.janelaAdmin.setLocationRelativeTo(null);
		} else {
			this.janelaCliente = new JanelaPrincipalCliente(this);
		}
	}

	// ── Ações do ADMIN ────────────────────────────────────────────────────────

	public void iniciarCrudPessoa() {
		if (this.ctrlCrudPessoa == null)
			this.ctrlCrudPessoa = new CtrlCrudPessoa(this);
		else
			this.janelaAdmin.notificar("Você já iniciou o CRUD Pessoa.");
	}

	public void iniciarCrudContaBancaria() {
		if (this.ctrlCrudConta == null)
			this.ctrlCrudConta = new CtrlCrudConta(this);
		else
			this.janelaAdmin.notificar("Você já iniciou o CRUD Conta Bancária.");
	}

	public void iniciarIncluirCrudAgenciaBancaria() {
		if (this.ctrlCrudAgencia == null)
			this.ctrlCrudAgencia = new CtrlCrudAgencia(this);
		else
			this.janelaAdmin.notificar("Você já iniciou o CRUD Agência Bancária.");
	}

	// ── Ações do CLIENTE ─────────────────────────────────────────────────────

	public void iniciarMinhasContas() {
		if (this.ctrlMinhasContas == null)
			this.ctrlMinhasContas = new CtrlMinhasContas(this);
		else
			this.janelaCliente.notificar("Minhas Contas já está aberto.");
	}

	public void iniciarMeusDados() {
		if (this.ctrlMeusDados == null)
			this.ctrlMeusDados = new CtrlMeusDados(this);
		else
			this.janelaCliente.notificar("Meus Dados já está aberto.");
	}

	public void iniciarAlterarSenha() {
		if (this.ctrlAlterarSenha == null)
			this.ctrlAlterarSenha = new CtrlAlterarSenha(this);
		else
			this.janelaCliente.notificar("Alterar Senha já está aberto.");
	}

	// ── Callback de filhos ────────────────────────────────────────────────────

	@Override
	public void ctrlFilhoFinalizado(ICtrl ctrlFilho) {
		if (ctrlFilho instanceof CtrlSetupAdmin) {
			this.ctrlSetupAdmin = null;
			// Após setup → vai para login
			this.ctrlLogin = new CtrlLogin(this);
		} else if (ctrlFilho instanceof CtrlLogin) {
			this.ctrlLogin = null;
			abrirMenuPorRole();
		} else if (ctrlFilho instanceof CtrlCrudPessoa) {
			this.ctrlCrudPessoa = null;
		} else if (ctrlFilho instanceof CtrlCrudConta) {
			this.ctrlCrudConta = null;
		} else if (ctrlFilho instanceof CtrlCrudAgencia) {
			this.ctrlCrudAgencia = null;
		} else if (ctrlFilho instanceof CtrlMinhasContas) {
			this.ctrlMinhasContas = null;
		} else if (ctrlFilho instanceof CtrlMeusDados) {
			this.ctrlMeusDados = null;
		} else if (ctrlFilho instanceof CtrlAlterarSenha) {
			this.ctrlAlterarSenha = null;
		}
	}

	@Override
	public void finalizar() {
		Sessao.limpar();
		System.exit(0);
	}

	@Override
	public Object getBemTangivel() { return null; }

	public static void main(String[] args) {
		new CtrlPrograma();
	}
}

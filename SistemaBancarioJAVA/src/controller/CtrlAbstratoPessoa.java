package controller;

import model.Pessoa;
import model.dao.DaoPessoa;
import viewer.JanelaPessoa;

abstract public class CtrlAbstratoPessoa extends CtrlAbstrato {
	//
	// ATRIBUTOS
	//
	private JanelaPessoa meuViewer;
	private Pessoa       pessoaEmEdicao;

	//
	// MÉTODOS
	//
	public CtrlAbstratoPessoa(ICtrl c) {
		super(c);
		this.meuViewer = new JanelaPessoa(this);
	}

	abstract public void efetuar(String cpf, String nome, int idade, String endereco, String telefone);

	public void finalizar() {
		this.meuViewer.finalizar();
		this.getCtrlPai().ctrlFilhoFinalizado(this);
	}

	public Object getBemTangivel() {
		return this.pessoaEmEdicao;
	}

	public JanelaPessoa getMeuViewer() { return meuViewer; }

	public void setMeuViewer(JanelaPessoa meuViewer) { this.meuViewer = meuViewer; }

	public Pessoa getPessoaEmEdicao() { return pessoaEmEdicao; }

	public void setPessoaEmEdicao(Pessoa pessoaEmEdicao) { this.pessoaEmEdicao = pessoaEmEdicao; }

	public void procurarPessoaComCpf(String cpf) {
		DaoPessoa dao = new DaoPessoa();
		Pessoa p = dao.consultarPorCpf(cpf);
		if (p == null) {
			this.getMeuViewer().notificar("Não tem pessoa com esse CPF! Verifique o valor passado!");
			return;
		}
		this.setPessoaEmEdicao(p);
		this.getMeuViewer().atualizarDados(p.getCpf(), p.getNome(), p.getIdade(), p.getEndereco(), p.getTelefone());
	}
}

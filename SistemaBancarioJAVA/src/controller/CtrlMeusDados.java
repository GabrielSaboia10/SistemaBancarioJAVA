package controller;

import model.ModelException;
import model.Pessoa;
import model.Sessao;
import model.dao.DaoPessoa;
import viewer.JanelaMeusDados;

public class CtrlMeusDados extends CtrlAbstrato {

	private JanelaMeusDados janela;
	private Pessoa          pessoaEmEdicao;

	public CtrlMeusDados(ICtrl ctrlPai) {
		super(ctrlPai);
		String cpf = Sessao.getUsuarioLogado().getCpf();
		this.pessoaEmEdicao = new DaoPessoa().consultarPorCpf(cpf);
		this.janela = new JanelaMeusDados(this);
		if (pessoaEmEdicao != null) {
			this.janela.preencherDados(
				pessoaEmEdicao.getCpf(),
				pessoaEmEdicao.getNome(),
				pessoaEmEdicao.getIdade(),
				pessoaEmEdicao.getEndereco(),
				pessoaEmEdicao.getTelefone()
			);
		} else {
			this.janela.notificar("Dados pessoais não encontrados para este usuário.");
		}
	}

	public void salvar(String nome, int idade, String endereco, String telefone) {
		if (pessoaEmEdicao == null) {
			janela.notificar("Não foi possível salvar: dados não encontrados.");
			return;
		}
		try {
			pessoaEmEdicao.setNome(nome);
			pessoaEmEdicao.setIdade(idade);
			pessoaEmEdicao.setEndereco(endereco);
			pessoaEmEdicao.setTelefone(telefone);
		} catch (ModelException e) {
			janela.notificar("Erro: " + e.getMessage());
			return;
		}
		new DaoPessoa().alterar(pessoaEmEdicao);
		janela.notificar("Dados atualizados com sucesso!");
		this.finalizar();
	}

	@Override
	public void finalizar() {
		if (this.janela != null) this.janela.finalizar();
		this.getCtrlPai().ctrlFilhoFinalizado(this);
	}

	@Override
	public Object getBemTangivel() { return pessoaEmEdicao; }

	@Override
	public void ctrlFilhoFinalizado(ICtrl ctrlFilho) { }
}

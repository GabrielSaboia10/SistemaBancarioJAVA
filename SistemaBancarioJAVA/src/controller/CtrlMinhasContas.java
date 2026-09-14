package controller;

import model.ContaBancaria;
import model.Sessao;
import model.dao.DaoContaBancaria;
import viewer.JanelaMinhasContas;

public class CtrlMinhasContas extends CtrlAbstrato {

	private JanelaMinhasContas janela;

	public CtrlMinhasContas(ICtrl ctrlPai) {
		super(ctrlPai);
		ContaBancaria[] contas = buscarContasDoCliente();
		this.janela = new JanelaMinhasContas(this, contas);
	}

	private ContaBancaria[] buscarContasDoCliente() {
		String cpfLogado = Sessao.getUsuarioLogado().getCpf();
		DaoContaBancaria dao = new DaoContaBancaria();
		ContaBancaria[] todas = dao.consultarTodos();
		long count = 0;
		for (ContaBancaria c : todas)
			if (c.getCorrentista() != null && c.getCorrentista().getCpf().equals(cpfLogado))
				count++;
		ContaBancaria[] minhas = new ContaBancaria[(int) count];
		int i = 0;
		for (ContaBancaria c : todas)
			if (c.getCorrentista() != null && c.getCorrentista().getCpf().equals(cpfLogado))
				minhas[i++] = c;
		return minhas;
	}

	@Override
	public void finalizar() {
		if (this.janela != null) this.janela.finalizar();
		this.getCtrlPai().ctrlFilhoFinalizado(this);
	}

	@Override
	public Object getBemTangivel() { return null; }

	@Override
	public void ctrlFilhoFinalizado(ICtrl ctrlFilho) { }
}

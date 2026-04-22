package viewer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import controller.CtrlOperacoesBancarias;
import model.ContaBancaria;

public class JanelaOperacoesBancarias extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;

	private JComboBox<ContaBancaria> cbContaOrigem;
	private JComboBox<ContaBancaria> cbContaDestino;
	private JTextField tfValor;
	private JLabel lblDestino;

	public JanelaOperacoesBancarias(CtrlOperacoesBancarias c, ContaBancaria[] contas) {
		super(c);
		setTitle("Operações Bancárias");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(480, 420);
		setLocationRelativeTo(null);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBackground(new Color(245, 247, 250));
		setContentPane(contentPane);

		JLabel titulo = new JLabel("Operações Bancárias", SwingConstants.CENTER);
		titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
		titulo.setForeground(new Color(30, 80, 160));
		titulo.setBorder(new EmptyBorder(18, 0, 10, 0));
		contentPane.add(titulo, BorderLayout.NORTH);

		JPanel form = new JPanel(new GridBagLayout());
		form.setBackground(new Color(245, 247, 250));
		form.setBorder(new EmptyBorder(0, 30, 0, 30));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 6, 10, 6);
		gbc.anchor = GridBagConstraints.WEST;

		cbContaOrigem = new JComboBox<>(contas);
		cbContaOrigem.setFont(new Font("SansSerif", Font.PLAIN, 13));

		cbContaDestino = new JComboBox<>(contas);
		cbContaDestino.setFont(new Font("SansSerif", Font.PLAIN, 13));

		tfValor = new JTextField(12);
		tfValor.setFont(new Font("SansSerif", Font.PLAIN, 13));

		adicionarCombo(form, gbc, "Conta:", cbContaOrigem, 0);

		lblDestino = new JLabel("Conta Destino:");
		lblDestino.setFont(new Font("SansSerif", Font.BOLD, 13));
		lblDestino.setVisible(false);
		cbContaDestino.setVisible(false);

		gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
		form.add(lblDestino, gbc);
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
		form.add(cbContaDestino, gbc);

		adicionarCampo(form, gbc, "Valor (R$):", tfValor, 2);

		JLabel lblSaldo = new JLabel("Selecione uma conta para ver o saldo.");
		lblSaldo.setFont(new Font("SansSerif", Font.ITALIC, 12));
		lblSaldo.setForeground(new Color(80, 80, 80));
		gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
		form.add(lblSaldo, gbc);
		gbc.gridwidth = 1;

		cbContaOrigem.addActionListener(e -> {
			ContaBancaria sel = (ContaBancaria) cbContaOrigem.getSelectedItem();
			if (sel != null)
				lblSaldo.setText(String.format("Saldo atual: R$ %.2f  |  Limite: R$ %.2f", sel.getSaldo(), sel.getLimiteChequeEspecial()));
		});
		if (contas.length > 0) cbContaOrigem.setSelectedIndex(0);

		contentPane.add(form, BorderLayout.CENTER);

		JPanel botoes = new JPanel(new GridLayout(2, 2, 10, 10));
		botoes.setBackground(new Color(245, 247, 250));
		botoes.setBorder(new EmptyBorder(0, 30, 20, 30));

		JButton btDepositar = criarBotao("Depositar", new Color(34, 150, 100));
		btDepositar.addActionListener(e -> {
			esconderDestino();
			executarOperacao("deposito");
		});

		JButton btSacar = criarBotao("Sacar", new Color(52, 120, 210));
		btSacar.addActionListener(e -> {
			esconderDestino();
			executarOperacao("saque");
		});

		JButton btTransferir = criarBotao("Transferir", new Color(150, 80, 200));
		btTransferir.addActionListener(e -> {
			mostrarDestino();
			executarOperacao("transferencia");
		});

		JButton btVoltar = criarBotao("Voltar", new Color(120, 120, 120));
		btVoltar.addActionListener(e -> ((CtrlOperacoesBancarias) getCtrl()).finalizar());

		botoes.add(btDepositar);
		botoes.add(btSacar);
		botoes.add(btTransferir);
		botoes.add(btVoltar);
		contentPane.add(botoes, BorderLayout.SOUTH);

		setVisible(true);
	}

	private void executarOperacao(String tipo) {
		double valor;
		try {
			valor = Double.parseDouble(tfValor.getText().trim().replace(",", "."));
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this, "Valor inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		ContaBancaria origem = (ContaBancaria) cbContaOrigem.getSelectedItem();
		ContaBancaria destino = (ContaBancaria) cbContaDestino.getSelectedItem();
		CtrlOperacoesBancarias ctrl = (CtrlOperacoesBancarias) getCtrl();
		switch (tipo) {
			case "deposito":     ctrl.depositar(origem, valor); break;
			case "saque":        ctrl.sacar(origem, valor); break;
			case "transferencia": ctrl.transferir(origem, destino, valor); break;
		}
	}

	private void mostrarDestino() {
		lblDestino.setVisible(true);
		cbContaDestino.setVisible(true);
	}

	private void esconderDestino() {
		lblDestino.setVisible(false);
		cbContaDestino.setVisible(false);
	}

	private void adicionarCampo(JPanel form, GridBagConstraints gbc, String label, JTextField tf, int row) {
		gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
		JLabel lbl = new JLabel(label);
		lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
		form.add(lbl, gbc);
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
		form.add(tf, gbc);
	}

	private void adicionarCombo(JPanel form, GridBagConstraints gbc, String label, JComboBox<?> cb, int row) {
		gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
		JLabel lbl = new JLabel(label);
		lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
		form.add(lbl, gbc);
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
		form.add(cb, gbc);
	}

	private JButton criarBotao(String texto, Color cor) {
		JButton btn = new JButton(texto);
		btn.setFont(new Font("SansSerif", Font.BOLD, 14));
		btn.setBackground(cor);
		btn.setForeground(Color.WHITE);
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return btn;
	}

	public void atualizarSaldo(ContaBancaria conta) {
		cbContaOrigem.removeAllItems();
		cbContaDestino.removeAllItems();
		CtrlOperacoesBancarias ctrl = (CtrlOperacoesBancarias) getCtrl();
		ContaBancaria[] contas = ctrl.getContas();
		for (ContaBancaria c : contas) {
			cbContaOrigem.addItem(c);
			cbContaDestino.addItem(c);
		}
		cbContaOrigem.setSelectedItem(conta);
	}
}

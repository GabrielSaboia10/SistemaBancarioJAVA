package viewer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import controller.CtrlAbstratoConta;
import controller.CtrlAlterarConta;
import controller.CtrlExcluirConta;
import controller.CtrlIncluirContaBancaria;
import controller.CtrlSelecionarConta;
import model.AgenciaBancaria;
import model.Pessoa;

public class JanelaContaBancaria extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;

	private JTextField tfNumero;
	private JTextField tfLimite;
	private JTextField tfSaldo;
	private JComboBox<Pessoa> cbCorrentista;
	private JComboBox<AgenciaBancaria> cbAgencia;
	private Boolean contaEscolhida = false;
	private JButton btProcurarConta;

	@SuppressWarnings("unchecked")
	public JanelaContaBancaria(CtrlAbstratoConta c, Pessoa[] conjPessoas, AgenciaBancaria[] conjAgencias) {
		super(c);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 420);
		setLocationRelativeTo(null);

		if (getCtrl() instanceof CtrlIncluirContaBancaria) setTitle("Incluir Conta Bancária");
		else if (getCtrl() instanceof CtrlAlterarConta)    setTitle("Alterar Conta Bancária");
		else if (getCtrl() instanceof CtrlExcluirConta)    setTitle("Excluir Conta Bancária");
		else if (getCtrl() instanceof CtrlSelecionarConta) setTitle("Buscar Conta Bancária");

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBackground(new Color(245, 247, 250));
		setContentPane(contentPane);

		JLabel titulo = new JLabel(getTitle(), SwingConstants.CENTER);
		titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
		titulo.setForeground(new Color(30, 80, 160));
		titulo.setBorder(new EmptyBorder(18, 0, 10, 0));
		contentPane.add(titulo, BorderLayout.NORTH);

		JPanel form = new JPanel(new GridBagLayout());
		form.setBackground(new Color(245, 247, 250));
		form.setBorder(new EmptyBorder(0, 30, 0, 30));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 6, 8, 6);
		gbc.anchor = GridBagConstraints.WEST;

		tfNumero = new JTextField(10);
		tfLimite = new JTextField(12);
		tfSaldo = new JTextField(12);

		adicionarCampo(form, gbc, "Número da Conta:", tfNumero, 0);
		adicionarCampo(form, gbc, "Limite (R$):", tfLimite, 1);
		adicionarCampo(form, gbc, "Saldo (R$):", tfSaldo, 2);

		if (getCtrl() instanceof CtrlIncluirContaBancaria) {
			cbCorrentista = new JComboBox<>(conjPessoas);
			cbAgencia = new JComboBox<>(conjAgencias);
		} else {
			cbCorrentista = new JComboBox<>();
			cbAgencia = new JComboBox<>();
		}
		cbCorrentista.setFont(new Font("SansSerif", Font.PLAIN, 13));
		cbAgencia.setFont(new Font("SansSerif", Font.PLAIN, 13));

		adicionarCombo(form, gbc, "Correntista:", cbCorrentista, 3);
		adicionarCombo(form, gbc, "Agência:", cbAgencia, 4);

		if (!(getCtrl() instanceof CtrlIncluirContaBancaria)) {
			btProcurarConta = new JButton("Buscar por Número");
			estilizarBotao(btProcurarConta, new Color(52, 120, 210));
			btProcurarConta.addActionListener(e -> {
				try {
					int num = Integer.parseInt(tfNumero.getText().trim());
					((CtrlAbstratoConta) getCtrl()).procurarContaComNumero(num);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this, "Número inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
				}
			});
			gbc.gridx = 1; gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
			form.add(btProcurarConta, gbc);
		} else {
			JButton btNovaPessoa = new JButton("+ Novo Correntista");
			estilizarBotao(btNovaPessoa, new Color(52, 120, 210));
			btNovaPessoa.addActionListener(e -> ((CtrlIncluirContaBancaria) getCtrl()).iniciarIncluirPessoa());
			gbc.gridx = 1; gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
			form.add(btNovaPessoa, gbc);

			JButton btNovaAgencia = new JButton("+ Nova Agência");
			estilizarBotao(btNovaAgencia, new Color(52, 120, 210));
			btNovaAgencia.addActionListener(e -> ((CtrlIncluirContaBancaria) getCtrl()).iniciarIncluirAgenciaBancaria());
			gbc.gridx = 1; gbc.gridy = 6; gbc.fill = GridBagConstraints.HORIZONTAL;
			form.add(btNovaAgencia, gbc);
		}

		contentPane.add(form, BorderLayout.CENTER);

		JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
		botoes.setBackground(new Color(245, 247, 250));

		JButton btOk = new JButton("Confirmar");
		estilizarBotao(btOk, new Color(34, 150, 100));
		btOk.addActionListener(e -> confirmar());

		JButton btCancelar = new JButton("Cancelar");
		estilizarBotao(btCancelar, new Color(120, 120, 120));
		btCancelar.addActionListener(e -> ((CtrlAbstratoConta) getCtrl()).finalizar());

		botoes.add(btOk);
		botoes.add(btCancelar);
		contentPane.add(botoes, BorderLayout.SOUTH);

		setVisible(true);
	}

	private void adicionarCampo(JPanel form, GridBagConstraints gbc, String label, JTextField tf, int row) {
		gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
		JLabel lbl = new JLabel(label);
		lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
		form.add(lbl, gbc);
		gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
		tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
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

	private void estilizarBotao(JButton btn, Color cor) {
		btn.setFont(new Font("SansSerif", Font.BOLD, 13));
		btn.setBackground(cor);
		btn.setForeground(Color.WHITE);
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.setPreferredSize(new Dimension(140, 34));
	}

	private void confirmar() {
		if ((getCtrl() instanceof CtrlAlterarConta) && !contaEscolhida) {
			JOptionPane.showMessageDialog(this, "Busque primeiro a conta pelo número.", "Aviso", JOptionPane.WARNING_MESSAGE);
			return;
		}
		int numero;
		double limite, saldo;
		try {
			numero = Integer.parseInt(tfNumero.getText().trim());
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this, "Número de conta inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			limite = Double.parseDouble(tfLimite.getText().trim().replace(",", "."));
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this, "Valor de limite inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			saldo = Double.parseDouble(tfSaldo.getText().trim().replace(",", "."));
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this, "Valor de saldo inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		((CtrlAbstratoConta) getCtrl()).efetuar(numero, limite, saldo,
				(Pessoa) cbCorrentista.getSelectedItem(),
				(AgenciaBancaria) cbAgencia.getSelectedItem());
	}

	public void atualizarCorrentista(Pessoa nova) {
		cbCorrentista.addItem(nova);
		cbCorrentista.setSelectedItem(nova);
	}

	public void atualizarAgencia(AgenciaBancaria nova) {
		cbAgencia.addItem(nova);
		cbAgencia.setSelectedItem(nova);
	}

	public void atualizarDados(int numero, double limite, double saldo, Pessoa correntista, AgenciaBancaria agencia) {
		cbAgencia.removeAllItems();
		cbCorrentista.removeAllItems();
		tfNumero.setText(Integer.toString(numero));
		tfLimite.setText(String.format("%.2f", limite));
		tfSaldo.setText(String.format("%.2f", saldo));
		atualizarCorrentista(correntista);
		atualizarAgencia(agencia);
		contaEscolhida = true;
		if (getCtrl() instanceof CtrlAlterarConta || getCtrl() instanceof CtrlExcluirConta) {
			btProcurarConta.setEnabled(false);
			tfNumero.setEnabled(false);
		}
	}
}

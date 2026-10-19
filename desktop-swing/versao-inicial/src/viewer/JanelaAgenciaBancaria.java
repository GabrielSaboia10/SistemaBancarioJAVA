package viewer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import controller.CtrlAbstratoAgencia;
import controller.CtrlAlterarAgenciaBancaria;
import controller.CtrlExcluirAgencia;
import controller.CtrlIncluirAgenciaBancaria;
import controller.CtrlSelecionarAgencia;

public class JanelaAgenciaBancaria extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;

	private JTextField tfNumero;
	private JTextField tfEndereco;
	private JTextField tfCidade;
	private Boolean agenciaEscolhida = false;
	private JButton btProcurarNumero;

	public JanelaAgenciaBancaria(CtrlAbstratoAgencia c) {
		super(c);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(420, 320);
		setLocationRelativeTo(null);

		if (getCtrl() instanceof CtrlIncluirAgenciaBancaria)    setTitle("Incluir Agência");
		else if (getCtrl() instanceof CtrlAlterarAgenciaBancaria) setTitle("Alterar Agência");
		else if (getCtrl() instanceof CtrlExcluirAgencia)        setTitle("Excluir Agência");
		else if (getCtrl() instanceof CtrlSelecionarAgencia)     setTitle("Buscar Agência");

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
		tfEndereco = new JTextField(22);
		tfCidade = new JTextField(16);

		adicionarCampo(form, gbc, "Número:", tfNumero, 0);
		adicionarCampo(form, gbc, "Endereço:", tfEndereco, 1);
		adicionarCampo(form, gbc, "Cidade:", tfCidade, 2);

		if (!(getCtrl() instanceof CtrlIncluirAgenciaBancaria)) {
			btProcurarNumero = new JButton("Buscar por Número");
			estilizarBotao(btProcurarNumero, new Color(52, 120, 210));
			btProcurarNumero.addActionListener(e -> {
				try {
					int num = Integer.parseInt(tfNumero.getText().trim());
					((CtrlAbstratoAgencia) getCtrl()).procurarAgenciaComNumero(num);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(this, "Número inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
				}
			});
			gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
			form.add(btProcurarNumero, gbc);
		}

		contentPane.add(form, BorderLayout.CENTER);

		JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
		botoes.setBackground(new Color(245, 247, 250));

		JButton btOk = new JButton("Confirmar");
		estilizarBotao(btOk, new Color(34, 150, 100));
		btOk.addActionListener(e -> confirmar());

		JButton btCancelar = new JButton("Cancelar");
		estilizarBotao(btCancelar, new Color(120, 120, 120));
		btCancelar.addActionListener(e -> ((CtrlAbstratoAgencia) getCtrl()).finalizar());

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

	private void estilizarBotao(JButton btn, Color cor) {
		btn.setFont(new Font("SansSerif", Font.BOLD, 13));
		btn.setBackground(cor);
		btn.setForeground(Color.WHITE);
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn.setPreferredSize(new Dimension(140, 36));
	}

	private void confirmar() {
		if ((getCtrl() instanceof CtrlAlterarAgenciaBancaria || getCtrl() instanceof CtrlExcluirAgencia
				|| getCtrl() instanceof CtrlSelecionarAgencia) && !agenciaEscolhida) {
			JOptionPane.showMessageDialog(this, "Busque primeiro a agência pelo número.", "Aviso", JOptionPane.WARNING_MESSAGE);
			return;
		}
		int numero;
		try {
			numero = Integer.parseInt(tfNumero.getText().trim());
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this, "Número inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String endereco = tfEndereco.getText().trim();
		String cidade = tfCidade.getText().trim();
		((CtrlAbstratoAgencia) getCtrl()).efetuar(numero, endereco, cidade);
	}

	public void atualizarDados(int numero, String endereco, String cidade) {
		tfNumero.setText(Integer.toString(numero));
		tfEndereco.setText(endereco);
		tfCidade.setText(cidade);
		agenciaEscolhida = true;
		if (getCtrl() instanceof CtrlAlterarAgenciaBancaria || getCtrl() instanceof CtrlExcluirAgencia) {
			btProcurarNumero.setEnabled(false);
			tfNumero.setEnabled(false);
		}
	}
}

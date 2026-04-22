package viewer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import controller.CtrlAbstratoPessoa;
import controller.CtrlAlterarPessoa;
import controller.CtrlExcluirPessoa;
import controller.CtrlIncluirPessoa;
import controller.CtrlSelecionarPessoa;

public class JanelaPessoa extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;

	private JTextField tfCpf;
	private JTextField tfNome;
	private JTextField tfIdade;
	private JButton btProcurarCpf;
	private boolean pessoaEscolhida = false;

	public JanelaPessoa(CtrlAbstratoPessoa c) {
		super(c);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(420, 320);
		setLocationRelativeTo(null);

		if (getCtrl() instanceof CtrlIncluirPessoa)        setTitle("Incluir Pessoa");
		else if (getCtrl() instanceof CtrlExcluirPessoa)   setTitle("Excluir Pessoa");
		else if (getCtrl() instanceof CtrlAlterarPessoa)   setTitle("Alterar Pessoa");
		else if (getCtrl() instanceof CtrlSelecionarPessoa) setTitle("Buscar Pessoa");

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

		tfCpf = new JTextField(16);
		tfNome = new JTextField(20);
		tfIdade = new JTextField(6);

		adicionarCampo(form, gbc, "CPF:", tfCpf, 0);
		adicionarCampo(form, gbc, "Nome:", tfNome, 1);
		adicionarCampo(form, gbc, "Idade:", tfIdade, 2);

		if (!(getCtrl() instanceof CtrlIncluirPessoa)) {
			btProcurarCpf = new JButton("Buscar por CPF");
			estilizarBotao(btProcurarCpf, new Color(52, 120, 210));
			btProcurarCpf.addActionListener(e -> {
				CtrlAbstratoPessoa ctrl = (CtrlAbstratoPessoa) getCtrl();
				ctrl.procurarPessoaComCpf(tfCpf.getText());
			});
			gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
			form.add(btProcurarCpf, gbc);
		}

		contentPane.add(form, BorderLayout.CENTER);

		JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
		botoes.setBackground(new Color(245, 247, 250));

		JButton btOk = new JButton("Confirmar");
		estilizarBotao(btOk, new Color(34, 150, 100));
		btOk.addActionListener(e -> confirmar());

		JButton btCancelar = new JButton("Cancelar");
		estilizarBotao(btCancelar, new Color(120, 120, 120));
		btCancelar.addActionListener(e -> ((CtrlAbstratoPessoa) getCtrl()).finalizar());

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
		btn.setPreferredSize(new Dimension(130, 36));
	}

	private void confirmar() {
		if ((getCtrl() instanceof CtrlAlterarPessoa || getCtrl() instanceof CtrlExcluirPessoa) && !pessoaEscolhida) {
			JOptionPane.showMessageDialog(this, "Busque primeiro a pessoa pelo CPF.", "Aviso", JOptionPane.WARNING_MESSAGE);
			return;
		}
		String cpf = tfCpf.getText().trim();
		String nome = tfNome.getText().trim();
		int idade;
		try {
			idade = Integer.parseInt(tfIdade.getText().trim());
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this, "Idade inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		((CtrlAbstratoPessoa) getCtrl()).efetuar(cpf, nome, idade);
	}

	public void atualizarDados(String cpf, String nome, int idade) {
		tfCpf.setText(cpf);
		tfNome.setText(nome);
		tfIdade.setText(Integer.toString(idade));
		pessoaEscolhida = true;
		if (getCtrl() instanceof CtrlAlterarPessoa || getCtrl() instanceof CtrlExcluirPessoa) {
			btProcurarCpf.setEnabled(false);
			tfCpf.setEnabled(false);
		}
	}
}

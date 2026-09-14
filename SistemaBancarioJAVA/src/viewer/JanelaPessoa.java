package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import controller.CtrlAbstratoPessoa;
import controller.CtrlAlterarPessoa;
import controller.CtrlExcluirPessoa;
import controller.CtrlIncluirPessoa;
import controller.CtrlSelecionarPessoa;

public class JanelaPessoa extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;

	private JPanel     contentPane;
	private JTextField tfCpf;
	private JTextField tfNome;
	private JTextField tfIdade;
	private JTextField tfEndereco;
	private JTextField tfTelefone;
	private JButton    btProcurarCpf;
	private JButton    btOk;
	private JButton    btCancelar;
	private boolean    pessoaEscolhida;

	public JanelaPessoa(CtrlAbstratoPessoa c) {
		super(c);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 480, 380);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		this.pessoaEscolhida = false;

		if (getCtrl() instanceof CtrlIncluirPessoa) {
			setTitle("Incluir Pessoa");
		} else {
			configurarBotaoProcurarCpf();
			if (getCtrl() instanceof CtrlExcluirPessoa)
				setTitle("Excluir Pessoa");
			else if (getCtrl() instanceof CtrlAlterarPessoa)
				setTitle("Alterar Pessoa");
			else if (getCtrl() instanceof CtrlSelecionarPessoa)
				setTitle("Selecionar Pessoa");
		}

		// CPF
		JLabel lblCpf = new JLabel("CPF:");
		lblCpf.setBounds(20, 20, 80, 20);
		contentPane.add(lblCpf);
		tfCpf = new JTextField();
		tfCpf.setBounds(110, 20, 200, 20);
		contentPane.add(tfCpf);

		// Nome
		JLabel lblNome = new JLabel("Nome:");
		lblNome.setBounds(20, 55, 80, 20);
		contentPane.add(lblNome);
		tfNome = new JTextField();
		tfNome.setBounds(110, 55, 320, 20);
		contentPane.add(tfNome);

		// Idade
		JLabel lblIdade = new JLabel("Idade:");
		lblIdade.setBounds(20, 90, 80, 20);
		contentPane.add(lblIdade);
		tfIdade = new JTextField();
		tfIdade.setBounds(110, 90, 80, 20);
		contentPane.add(tfIdade);

		// Endereço
		JLabel lblEndereco = new JLabel("Endereço:");
		lblEndereco.setBounds(20, 125, 80, 20);
		contentPane.add(lblEndereco);
		tfEndereco = new JTextField();
		tfEndereco.setBounds(110, 125, 320, 20);
		contentPane.add(tfEndereco);

		// Telefone
		JLabel lblTelefone = new JLabel("Telefone:");
		lblTelefone.setBounds(20, 160, 80, 20);
		contentPane.add(lblTelefone);
		tfTelefone = new JTextField();
		tfTelefone.setBounds(110, 160, 150, 20);
		contentPane.add(tfTelefone);

		// Botões
		btOk = new JButton("Ok");
		btOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ((getCtrl() instanceof CtrlAlterarPessoa || getCtrl() instanceof CtrlExcluirPessoa)
						&& !pessoaEscolhida) {
					JOptionPane.showMessageDialog(null, "Você ainda não definiu qual é a Pessoa!");
					return;
				}
				String cpf      = tfCpf.getText();
				String nome     = tfNome.getText();
				String endereco = tfEndereco.getText();
				String telefone = tfTelefone.getText();
				int    idade;
				try {
					idade = Integer.parseInt(tfIdade.getText().trim());
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(null, "Idade inválida: " + tfIdade.getText());
					return;
				}
				CtrlAbstratoPessoa ctrl = (CtrlAbstratoPessoa) getCtrl();
				ctrl.efetuar(cpf, nome, idade, endereco, telefone);
			}
		});
		btOk.setBounds(90, 290, 89, 30);
		contentPane.add(btOk);

		btCancelar = new JButton("Cancelar");
		btCancelar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((CtrlAbstratoPessoa) getCtrl()).finalizar();
			}
		});
		btCancelar.setBounds(260, 290, 89, 30);
		contentPane.add(btCancelar);

		this.setVisible(true);
	}

	public void atualizarDados(String cpf, String nome, int idade, String endereco, String telefone) {
		this.tfCpf.setText(cpf);
		this.tfNome.setText(nome);
		this.tfIdade.setText(Integer.toString(idade));
		this.tfEndereco.setText(endereco != null ? endereco : "");
		this.tfTelefone.setText(telefone != null ? telefone : "");
		this.pessoaEscolhida = true;
		if (getCtrl() instanceof CtrlAlterarPessoa || getCtrl() instanceof CtrlExcluirPessoa) {
			this.btProcurarCpf.setEnabled(false);
			this.tfCpf.setEnabled(false); // CPF imutável após busca
		}
	}

	private void configurarBotaoProcurarCpf() {
		btProcurarCpf = new JButton("Procurar CPF");
		btProcurarCpf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cpf = tfCpf.getText();
				((CtrlAbstratoPessoa) getCtrl()).procurarPessoaComCpf(cpf);
			}
		});
		btProcurarCpf.setBounds(320, 18, 127, 23);
		contentPane.add(btProcurarCpf);
	}
}

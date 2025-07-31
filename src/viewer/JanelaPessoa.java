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
	//
	// ATRIBUTOS (Componentes Gráficos)
	//
	private JPanel contentPane;
	private JTextField tfCpf;
	private JTextField tfNome;
	private JTextField tfIdade;
	private JButton btProcurarCpf;
	private JButton btOk;
	private JButton btCancelar;
	private boolean pessoaEscolhida;

	/**
	 * Create the frame.
	 */
	public JanelaPessoa(CtrlAbstratoPessoa c) {
		super(c);

		// Definimos o content pane da janela
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);

		// Vou redundantemente indicar que o atributo
		// "pessoaEscolhida" é igual a "false", pois, no início
		// o usuário ainda não indicou a pessoa a ser alterada
		this.pessoaEscolhida = false;
		// Verificando qual é o controlador dessa janela

		if ((getCtrl() instanceof CtrlIncluirPessoa)) {
			setTitle("Incluir Pessoa");
		} else {
			configurarBotaoProcurarCpf();
			if (getCtrl() instanceof CtrlExcluirPessoa)
				setTitle("Excluir Pessoa");
			else if (getCtrl() instanceof CtrlAlterarPessoa)
				setTitle("Alterar Pessoa");
			else if (getCtrl() instanceof CtrlSelecionarPessoa)
				setTitle("Selecionar Pessoa Especificas");
		}

		JLabel lblNewLabel = new JLabel("CPF:");
		lblNewLabel.setBounds(39, 37, 46, 14);
		contentPane.add(lblNewLabel);

		tfCpf = new JTextField();
		tfCpf.setBounds(88, 34, 186, 20);
		contentPane.add(tfCpf);
		tfCpf.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel("Nome:");
		lblNewLabel_1.setBounds(39, 86, 46, 14);
		contentPane.add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("Idade:");
		lblNewLabel_2.setBounds(39, 137, 46, 14);
		contentPane.add(lblNewLabel_2);

		tfNome = new JTextField();
		tfNome.setBounds(88, 83, 288, 20);
		contentPane.add(tfNome);
		tfNome.setColumns(10);

		tfIdade = new JTextField();
		tfIdade.setBounds(88, 134, 86, 20);
		contentPane.add(tfIdade);
		tfIdade.setColumns(10);

		btOk = new JButton("Ok");
		btOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Se a pessoa a ser alterada ainda não foi escolhida, saímos.
				if ((getCtrl() instanceof CtrlAlterarPessoa || getCtrl() instanceof CtrlExcluirPessoa)
						&& !pessoaEscolhida) {
					JOptionPane.showMessageDialog(null, "Você ainda não definiu qual é a Pessoa a ser alterada!");
					return;
				}
				// Recuperando os dados preenchidos pelo usuário
				String cpf = tfCpf.getText(); // 123.123.123-12
				String nome = tfNome.getText();
				String aux = tfIdade.getText();
				int idade;
				try {
					idade = Integer.parseInt(aux);
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(null, "Idade Inválida: " + aux);
					return;
				}

				// Informando ao controlador do caso de uso que ele
				// deve efetuar a efetuar a operação com a pessoa
				CtrlAbstratoPessoa ctrl = (CtrlAbstratoPessoa) getCtrl();
				ctrl.efetuar(cpf, nome, idade);
			}
		});
		btOk.setBounds(85, 200, 89, 23);
		contentPane.add(btOk);

		btCancelar = new JButton("Cancelar");
		btCancelar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlAbstratoPessoa ctrl = (CtrlAbstratoPessoa) getCtrl();
				ctrl.finalizar();
			}
		});
		btCancelar.setBounds(251, 200, 89, 23);
		contentPane.add(btCancelar);

		this.setVisible(true);
	}

	public void atualizarDados(String cpf, String nome, int idade) {
		this.tfCpf.setText(cpf);
		this.tfNome.setText(nome);
		this.tfIdade.setText(Integer.toString(idade));
		this.pessoaEscolhida = true;
		if (getCtrl() instanceof CtrlAlterarPessoa || getCtrl() instanceof CtrlExcluirPessoa) {
			this.btProcurarCpf.setEnabled(false);
			this.tfCpf.setEnabled(false);
		}

	}

	private void configurarBotaoProcurarCpf() {
		btProcurarCpf = new JButton("Procurar CPF");
		btProcurarCpf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cpf = tfCpf.getText();
				CtrlAbstratoPessoa ctrl = (CtrlAbstratoPessoa) getCtrl();
				ctrl.procurarPessoaComCpf(cpf);
			}
		});
		btProcurarCpf.setBounds(284, 33, 127, 23);
		contentPane.add(btProcurarCpf);
	}

}

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

import controller.CtrlAbstratoAgencia;
import controller.CtrlAlterarAgenciaBancaria;
import controller.CtrlExcluirAgencia;
import controller.CtrlIncluirAgenciaBancaria;
import controller.CtrlSelecionarAgencia;

public class JanelaAgenciaBancaria extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;
	//
	// ATRIBUTOS (Componentes Gráficos)
	//
	private JPanel contentPane;
	private JTextField tfNumero;
	private JTextField tfEndereco;
	private JTextField tfCidade;
	private Boolean agenciaEscolhida;
	private JButton btProcurarNumero;

	/**
	 * Create the frame.
	 */
	public JanelaAgenciaBancaria(CtrlAbstratoAgencia c) {
		super(c);
		this.agenciaEscolhida = false;
		setTitle("Agência Bancária");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		if (getCtrl() instanceof CtrlIncluirAgenciaBancaria) {
			setTitle("Incluir Agencia");

		} else {
			configurarBotaoProcurarNumero();
			if (getCtrl() instanceof CtrlAlterarAgenciaBancaria)
				setTitle("Alterar Agencia");
			else if (getCtrl() instanceof CtrlExcluirAgencia)
				setTitle("Excluir Agencia");
			else if (getCtrl() instanceof CtrlSelecionarAgencia)
				setTitle("Selecionar Agencia");

		}
		JLabel lblNewLabel = new JLabel("Número:");
		lblNewLabel.setBounds(28, 37, 57, 14);
		contentPane.add(lblNewLabel);

		tfNumero = new JTextField();
		tfNumero.setBounds(88, 34, 186, 20);
		contentPane.add(tfNumero);
		tfNumero.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel("Endereço:");
		lblNewLabel_1.setBounds(28, 86, 57, 14);
		contentPane.add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("Cidade:");
		lblNewLabel_2.setBounds(39, 137, 46, 14);
		contentPane.add(lblNewLabel_2);

		tfEndereco = new JTextField();
		tfEndereco.setBounds(88, 83, 288, 20);
		contentPane.add(tfEndereco);
		tfEndereco.setColumns(10);

		tfCidade = new JTextField();
		tfCidade.setBounds(88, 134, 86, 20);
		contentPane.add(tfCidade);
		tfCidade.setColumns(10);

		JButton btOk = new JButton("Ok");
		btOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Recuperando os dados preenchidos pelo usuário
				if ((getCtrl() instanceof CtrlAlterarAgenciaBancaria || getCtrl() instanceof CtrlExcluirAgencia
						|| getCtrl() instanceof CtrlSelecionarAgencia) && !agenciaEscolhida) {
					JOptionPane.showMessageDialog(null, "Você ainda não definiu qual é a Agencia a ser alterada!");
					return;
				}
				String aux = tfNumero.getText();
				int numero;
				try {
					numero = Integer.parseInt(aux);
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(null, "Número Inválido: " + aux);
					return;
				}
				String endereco = tfEndereco.getText();
				String cidade = tfCidade.getText();

				// Informando ao controlador do caso de uso que ele
				// deve efetuar a inclusão da pessoa
				CtrlAbstratoAgencia ctrl = (CtrlAbstratoAgencia) getCtrl();
				ctrl.efetuar(numero, endereco, cidade);
			}
		});
		btOk.setBounds(85, 200, 89, 23);
		contentPane.add(btOk);

		JButton btCancelar = new JButton("Cancelar");
		btCancelar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlAbstratoAgencia ctrl = (CtrlAbstratoAgencia) getCtrl();
				ctrl.finalizar();
			}
		});
		btCancelar.setBounds(251, 200, 89, 23);
		contentPane.add(btCancelar);
		this.setVisible(true);
	}

	public void atualizarDados(int numero, String endereco, String cidade) {
		this.tfNumero.setText(Integer.toString(numero));
		this.tfEndereco.setText(endereco);
		this.tfCidade.setText(cidade);
		this.agenciaEscolhida = true;
		if (getCtrl() instanceof CtrlAlterarAgenciaBancaria || getCtrl() instanceof CtrlExcluirAgencia) {
			this.btProcurarNumero.setEnabled(false);
			this.tfNumero.setEnabled(false);
		}
	}

	private void configurarBotaoProcurarNumero() {
		btProcurarNumero = new JButton("Procurar Numero!");
		btProcurarNumero.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String aux = tfNumero.getText();
				int numero;
				try {
					numero = Integer.parseInt(aux);
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(null, "Número Inválido: " + aux);
					return;
				}
				CtrlAbstratoAgencia ctrl = (CtrlAbstratoAgencia) getCtrl();
				ctrl.procurarAgenciaComNumero(numero);

			}
		});
		btProcurarNumero.setBounds(284, 33, 140, 23);
		contentPane.add(btProcurarNumero);
	}
}

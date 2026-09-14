package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import controller.CtrlMeusDados;

public class JanelaMeusDados extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;

	private JTextField tfCpf;
	private JTextField tfNome;
	private JTextField tfIdade;
	private JTextField tfEndereco;
	private JTextField tfTelefone;

	public JanelaMeusDados(CtrlMeusDados c) {
		super(c);
		setTitle("Meus Dados");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 460, 340);

		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(10, 10, 10, 10));
		pane.setLayout(null);
		setContentPane(pane);

		// CPF (somente leitura)
		JLabel lblCpf = new JLabel("CPF:");
		lblCpf.setBounds(20, 20, 100, 20);
		pane.add(lblCpf);
		tfCpf = new JTextField();
		tfCpf.setEnabled(false);
		tfCpf.setBounds(130, 20, 200, 22);
		pane.add(tfCpf);

		JLabel lblNome = new JLabel("Nome:");
		lblNome.setBounds(20, 55, 100, 20);
		pane.add(lblNome);
		tfNome = new JTextField();
		tfNome.setBounds(130, 55, 280, 22);
		pane.add(tfNome);

		JLabel lblIdade = new JLabel("Idade:");
		lblIdade.setBounds(20, 90, 100, 20);
		pane.add(lblIdade);
		tfIdade = new JTextField();
		tfIdade.setBounds(130, 90, 80, 22);
		pane.add(tfIdade);

		JLabel lblEndereco = new JLabel("Endereço:");
		lblEndereco.setBounds(20, 125, 100, 20);
		pane.add(lblEndereco);
		tfEndereco = new JTextField();
		tfEndereco.setBounds(130, 125, 280, 22);
		pane.add(tfEndereco);

		JLabel lblTelefone = new JLabel("Telefone:");
		lblTelefone.setBounds(20, 160, 100, 20);
		pane.add(lblTelefone);
		tfTelefone = new JTextField();
		tfTelefone.setBounds(130, 160, 150, 22);
		pane.add(tfTelefone);

		JButton btSalvar = new JButton("Salvar");
		btSalvar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String nome     = tfNome.getText();
				String endereco = tfEndereco.getText();
				String telefone = tfTelefone.getText();
				int    idade;
				try {
					idade = Integer.parseInt(tfIdade.getText().trim());
				} catch (NumberFormatException ex) {
					notificar("Idade inválida!");
					return;
				}
				((CtrlMeusDados) getCtrl()).salvar(nome, idade, endereco, telefone);
			}
		});
		btSalvar.setBounds(90, 260, 100, 30);
		pane.add(btSalvar);

		JButton btCancelar = new JButton("Cancelar");
		btCancelar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((CtrlMeusDados) getCtrl()).finalizar();
			}
		});
		btCancelar.setBounds(250, 260, 100, 30);
		pane.add(btCancelar);

		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void preencherDados(String cpf, String nome, int idade, String endereco, String telefone) {
		tfCpf.setText(cpf);
		tfNome.setText(nome);
		tfIdade.setText(Integer.toString(idade));
		tfEndereco.setText(endereco != null ? endereco : "");
		tfTelefone.setText(telefone != null ? telefone : "");
	}
}

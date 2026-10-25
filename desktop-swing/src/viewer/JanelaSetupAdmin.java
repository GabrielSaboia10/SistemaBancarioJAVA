package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import controller.CtrlSetupAdmin;

public class JanelaSetupAdmin extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;

	private JTextField     tfCpf;
	private JPasswordField tfSenha;
	private JPasswordField tfConfirmar;

	public JanelaSetupAdmin(CtrlSetupAdmin c) {
		super(c);
		setTitle("Configuração Inicial — Criar Administrador");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 400, 260);

		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(10, 10, 10, 10));
		pane.setLayout(null);
		setContentPane(pane);

		JLabel lblInfo = new JLabel("Nenhum administrador encontrado. Crie o primeiro:");
		lblInfo.setBounds(20, 10, 360, 20);
		pane.add(lblInfo);

		JLabel lblCpf = new JLabel("CPF:");
		lblCpf.setBounds(20, 45, 100, 20);
		pane.add(lblCpf);
		tfCpf = new JTextField();
		tfCpf.setBounds(130, 45, 200, 22);
		pane.add(tfCpf);

		JLabel lblSenha = new JLabel("Senha:");
		lblSenha.setBounds(20, 85, 100, 20);
		pane.add(lblSenha);
		tfSenha = new JPasswordField();
		tfSenha.setBounds(130, 85, 200, 22);
		pane.add(tfSenha);

		JLabel lblConfirmar = new JLabel("Confirmar senha:");
		lblConfirmar.setBounds(20, 125, 110, 20);
		pane.add(lblConfirmar);
		tfConfirmar = new JPasswordField();
		tfConfirmar.setBounds(130, 125, 200, 22);
		pane.add(tfConfirmar);

		JButton btCriar = new JButton("Criar Administrador");
		btCriar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cpf       = tfCpf.getText().trim();
				String senha     = new String(tfSenha.getPassword());
				String confirmar = new String(tfConfirmar.getPassword());
				((CtrlSetupAdmin) getCtrl()).criarAdmin(cpf, senha, confirmar);
			}
		});
		btCriar.setBounds(100, 180, 180, 30);
		pane.add(btCriar);

		setLocationRelativeTo(null);
		setVisible(true);
	}
}

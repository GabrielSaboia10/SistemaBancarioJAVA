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

import controller.CtrlLogin;

public class JanelaLogin extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;

	private JTextField     tfCpf;
	private JPasswordField tfSenha;

	public JanelaLogin(CtrlLogin c) {
		super(c);
		setTitle("Login — Sistema Bancário");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 360, 220);

		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(10, 10, 10, 10));
		pane.setLayout(null);
		setContentPane(pane);

		JLabel lblCpf = new JLabel("CPF:");
		lblCpf.setBounds(20, 25, 80, 20);
		pane.add(lblCpf);

		tfCpf = new JTextField();
		tfCpf.setBounds(110, 25, 200, 22);
		pane.add(tfCpf);

		JLabel lblSenha = new JLabel("Senha:");
		lblSenha.setBounds(20, 65, 80, 20);
		pane.add(lblSenha);

		tfSenha = new JPasswordField();
		tfSenha.setBounds(110, 65, 200, 22);
		pane.add(tfSenha);

		JButton btEntrar = new JButton("Entrar");
		btEntrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cpf   = tfCpf.getText().trim();
				String senha = new String(tfSenha.getPassword());
				((CtrlLogin) getCtrl()).efetuarLogin(cpf, senha);
			}
		});
		btEntrar.setBounds(110, 120, 100, 30);
		pane.add(btEntrar);

		// Enter na senha também confirma
		tfSenha.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btEntrar.doClick();
			}
		});

		setLocationRelativeTo(null);
		setVisible(true);
	}
}

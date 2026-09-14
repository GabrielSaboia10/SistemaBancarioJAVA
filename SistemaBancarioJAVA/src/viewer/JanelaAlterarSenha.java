package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import controller.CtrlAlterarSenha;

public class JanelaAlterarSenha extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;

	private JPasswordField tfAtual;
	private JPasswordField tfNova;
	private JPasswordField tfConfirmar;

	public JanelaAlterarSenha(CtrlAlterarSenha c) {
		super(c);
		setTitle("Alterar Senha");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 360, 240);

		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(10, 10, 10, 10));
		pane.setLayout(null);
		setContentPane(pane);

		JLabel lblAtual = new JLabel("Senha atual:");
		lblAtual.setBounds(20, 25, 110, 20);
		pane.add(lblAtual);
		tfAtual = new JPasswordField();
		tfAtual.setBounds(140, 25, 180, 22);
		pane.add(tfAtual);

		JLabel lblNova = new JLabel("Nova senha:");
		lblNova.setBounds(20, 65, 110, 20);
		pane.add(lblNova);
		tfNova = new JPasswordField();
		tfNova.setBounds(140, 65, 180, 22);
		pane.add(tfNova);

		JLabel lblConfirmar = new JLabel("Confirmar nova:");
		lblConfirmar.setBounds(20, 105, 110, 20);
		pane.add(lblConfirmar);
		tfConfirmar = new JPasswordField();
		tfConfirmar.setBounds(140, 105, 180, 22);
		pane.add(tfConfirmar);

		JButton btSalvar = new JButton("Salvar");
		btSalvar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String atual     = new String(tfAtual.getPassword());
				String nova      = new String(tfNova.getPassword());
				String confirmar = new String(tfConfirmar.getPassword());
				((CtrlAlterarSenha) getCtrl()).alterarSenha(atual, nova, confirmar);
			}
		});
		btSalvar.setBounds(70, 160, 90, 30);
		pane.add(btSalvar);

		JButton btCancelar = new JButton("Cancelar");
		btCancelar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((CtrlAlterarSenha) getCtrl()).finalizar();
			}
		});
		btCancelar.setBounds(190, 160, 90, 30);
		pane.add(btCancelar);

		setLocationRelativeTo(null);
		setVisible(true);
	}
}

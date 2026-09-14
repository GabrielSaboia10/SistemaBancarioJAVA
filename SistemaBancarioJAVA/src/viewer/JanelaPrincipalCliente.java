package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import controller.CtrlPrograma;
import model.Sessao;

public class JanelaPrincipalCliente extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;

	public JanelaPrincipalCliente(CtrlPrograma c) {
		super(c);
		String nome = Sessao.getUsuarioLogado().getPessoaVinculada() != null
			? Sessao.getUsuarioLogado().getPessoaVinculada().getNome()
			: Sessao.getUsuarioLogado().getCpf();

		setTitle("Área do Cliente — " + nome);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 480, 260);

		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(10, 10, 10, 10));
		pane.setLayout(null);
		setContentPane(pane);

		JLabel lblBemVindo = new JLabel("Bem-vindo(a), " + nome + "!");
		lblBemVindo.setBounds(20, 10, 400, 20);
		pane.add(lblBemVindo);

		JButton btMinhasContas = new JButton("Minhas Contas");
		btMinhasContas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((CtrlPrograma) getCtrl()).iniciarMinhasContas();
			}
		});
		btMinhasContas.setBounds(20, 50, 200, 55);
		pane.add(btMinhasContas);

		JButton btMeusDados = new JButton("Meus Dados");
		btMeusDados.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((CtrlPrograma) getCtrl()).iniciarMeusDados();
			}
		});
		btMeusDados.setBounds(250, 50, 200, 55);
		pane.add(btMeusDados);

		JButton btAlterarSenha = new JButton("Alterar Senha");
		btAlterarSenha.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((CtrlPrograma) getCtrl()).iniciarAlterarSenha();
			}
		});
		btAlterarSenha.setBounds(20, 130, 200, 55);
		pane.add(btAlterarSenha);

		JButton btSair = new JButton("Sair");
		btSair.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((CtrlPrograma) getCtrl()).finalizar();
			}
		});
		btSair.setBounds(250, 130, 200, 55);
		pane.add(btSair);

		setLocationRelativeTo(null);
		setVisible(true);
	}
}

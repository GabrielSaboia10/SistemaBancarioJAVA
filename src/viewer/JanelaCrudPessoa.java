package viewer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import controller.CtrlCrudPessoa;
import controller.ICtrl;

public class JanelaCrudPessoa extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;

	public JanelaCrudPessoa(ICtrl c) {
		super(c);
		setTitle("Gerenciar Pessoas");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(440, 360);
		setLocationRelativeTo(null);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBackground(new Color(245, 247, 250));
		setContentPane(contentPane);

		JLabel titulo = new JLabel("Gerenciar Pessoas", SwingConstants.CENTER);
		titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
		titulo.setForeground(new Color(30, 80, 160));
		titulo.setBorder(new EmptyBorder(20, 0, 10, 0));
		contentPane.add(titulo, BorderLayout.NORTH);

		JPanel grid = new JPanel(new GridLayout(3, 2, 12, 12));
		grid.setBackground(new Color(245, 247, 250));
		grid.setBorder(new EmptyBorder(10, 30, 10, 30));

		JButton btnIncluir = criarBotao("Incluir Pessoa", new Color(52, 120, 210));
		btnIncluir.addActionListener(e -> ((CtrlCrudPessoa) getCtrl()).iniciarIncluirPessoa());

		JButton btnAlterar = criarBotao("Alterar Pessoa", new Color(52, 120, 210));
		btnAlterar.addActionListener(e -> ((CtrlCrudPessoa) getCtrl()).iniciarAlterarPessoa());

		JButton btnDeletar = criarBotao("Excluir Pessoa", new Color(200, 60, 60));
		btnDeletar.addActionListener(e -> ((CtrlCrudPessoa) getCtrl()).iniciarExcluirPessoa());

		JButton btnSelecionar = criarBotao("Buscar Pessoa", new Color(52, 120, 210));
		btnSelecionar.addActionListener(e -> ((CtrlCrudPessoa) getCtrl()).iniciarSelecionarPessoa());

		JButton btnTodas = criarBotao("Listar Todas", new Color(34, 150, 100));
		btnTodas.addActionListener(e -> ((CtrlCrudPessoa) getCtrl()).iniciarSelecionarTodasPessoas());

		JButton btnVoltar = criarBotao("Voltar", new Color(120, 120, 120));
		btnVoltar.addActionListener(e -> ((CtrlCrudPessoa) getCtrl()).finalizar());

		grid.add(btnIncluir);
		grid.add(btnAlterar);
		grid.add(btnDeletar);
		grid.add(btnSelecionar);
		grid.add(btnTodas);
		grid.add(btnVoltar);

		contentPane.add(grid, BorderLayout.CENTER);
		contentPane.add(new JPanel() {{ setBackground(new Color(245, 247, 250)); setBorder(new EmptyBorder(10, 0, 10, 0)); }}, BorderLayout.SOUTH);
	}

	private JButton criarBotao(String texto, Color cor) {
		JButton btn = new JButton(texto);
		btn.setFont(new Font("SansSerif", Font.BOLD, 13));
		btn.setBackground(cor);
		btn.setForeground(Color.WHITE);
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return btn;
	}
}

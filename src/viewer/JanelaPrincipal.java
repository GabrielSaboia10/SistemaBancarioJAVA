package viewer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import controller.CtrlPrograma;

public class JanelaPrincipal extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;

	public JanelaPrincipal(CtrlPrograma c) {
		super(c);
		setTitle("Sistema Bancário");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(480, 340);
		setLocationRelativeTo(null);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBackground(new Color(245, 247, 250));
		setContentPane(contentPane);

		JLabel titulo = new JLabel("Sistema Bancário", SwingConstants.CENTER);
		titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
		titulo.setForeground(new Color(30, 80, 160));
		titulo.setBorder(new EmptyBorder(24, 0, 8, 0));
		contentPane.add(titulo, BorderLayout.NORTH);

		JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
		grid.setBackground(new Color(245, 247, 250));
		grid.setBorder(new EmptyBorder(10, 40, 10, 40));

		JButton btPessoa = criarBotao("Pessoas", new Color(52, 120, 210));
		btPessoa.addActionListener(e -> ((CtrlPrograma) getCtrl()).iniciarCrudPessoa());

		JButton btConta = criarBotao("Contas Bancárias", new Color(52, 120, 210));
		btConta.addActionListener(e -> ((CtrlPrograma) getCtrl()).iniciarCrudContaBancaria());

		JButton btAgencia = criarBotao("Agências Bancárias", new Color(52, 120, 210));
		btAgencia.addActionListener(e -> ((CtrlPrograma) getCtrl()).iniciarIncluirCrudAgenciaBancaria());

		JButton btOperacoes = criarBotao("Operações Bancárias", new Color(34, 150, 100));
		btOperacoes.addActionListener(e -> ((CtrlPrograma) getCtrl()).iniciarOperacoesBancarias());

		grid.add(btPessoa);
		grid.add(btConta);
		grid.add(btAgencia);
		grid.add(btOperacoes);
		contentPane.add(grid, BorderLayout.CENTER);

		JButton btSair = new JButton("Sair do Sistema");
		btSair.setFont(new Font("SansSerif", Font.PLAIN, 13));
		btSair.setBackground(new Color(200, 60, 60));
		btSair.setForeground(Color.WHITE);
		btSair.setFocusPainted(false);
		btSair.setBorder(new EmptyBorder(10, 0, 10, 0));
		btSair.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btSair.addActionListener(e -> ((CtrlPrograma) getCtrl()).finalizar());

		JPanel rodape = new JPanel(new BorderLayout());
		rodape.setBackground(new Color(245, 247, 250));
		rodape.setBorder(new EmptyBorder(0, 40, 20, 40));
		rodape.add(btSair, BorderLayout.CENTER);
		contentPane.add(rodape, BorderLayout.SOUTH);

		setVisible(true);
	}

	private JButton criarBotao(String texto, Color cor) {
		JButton btn = new JButton("<html><center>" + texto + "</center></html>");
		btn.setFont(new Font("SansSerif", Font.BOLD, 14));
		btn.setBackground(cor);
		btn.setForeground(Color.WHITE);
		btn.setFocusPainted(false);
		btn.setBorderPainted(false);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return btn;
	}
}

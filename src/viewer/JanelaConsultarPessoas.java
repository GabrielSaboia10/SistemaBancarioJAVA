package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import controller.CtrlSelecionarTodasPessoas;
import controller.ICtrl;
import model.Pessoa;
import model.dao.DaoPessoa;

public class JanelaConsultarPessoas extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;
	//
	// ATRIBUTOS
	//
	private JPanel contentPane;
	private JTable tabela;

	/**
	 * Create the frame.
	 */
	public JanelaConsultarPessoas(ICtrl c) {
		super(c);
		setTitle("TODAS AS PESSOAS!!!");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		DaoPessoa dao = new DaoPessoa();
		Pessoa[] listaPessoas = dao.consultarTodos();
		this.atualizarDados(listaPessoas);

		JButton btSair = new JButton("Sair");
		btSair.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlSelecionarTodasPessoas ctrl = (CtrlSelecionarTodasPessoas) getCtrl();
				ctrl.finalizar();
			}
		});
		btSair.setBounds(181, 227, 89, 23);
		contentPane.add(btSair);

		JScrollPane scrollPane = new JScrollPane(tabela);
		scrollPane.setBounds(10, 11, 414, 200);
		contentPane.add(scrollPane);

		this.setVisible(true);
	}

	/**
	 * Atualiza os dados apresentados no JTable da janela
	 */
	public void atualizarDados(Pessoa[] listaPessoas) {
		HelperTableModel h = new HelperTableModel(listaPessoas);
		tabela = new JTable(h.getTableModel());
	}

	/**
	 * Coloca uma mensagem para o usuário
	 */
	public void notificar(String texto) {
		JOptionPane.showMessageDialog(null, texto);
	}
}
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

import controller.CtrlSelecionarTodasAgencias;
import controller.ICtrl;
import model.AgenciaBancaria;
import model.dao.DaoAgenciaBancaria;

public class JanelaConsultarAgencias extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;
	//
	// ATRIBUTOS
	//
	private JPanel contentPane;
	private JTable tabela;

	/**
	 * Create the frame.
	 */
	public JanelaConsultarAgencias(ICtrl c) {
		super(c);
		setTitle("TODAS AS AgenciaBancariaS!!!");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		DaoAgenciaBancaria dao = new DaoAgenciaBancaria();
		AgenciaBancaria[] listaAgenciaBancarias = dao.consultarTodos();
		this.atualizarDados(listaAgenciaBancarias);

		JButton btSair = new JButton("Sair");
		btSair.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlSelecionarTodasAgencias ctrl = (CtrlSelecionarTodasAgencias) getCtrl();
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
	public void atualizarDados(AgenciaBancaria[] listaAgenciasBancarias) {
		HelperTableModel h = new HelperTableModel(listaAgenciasBancarias);
		tabela = new JTable(h.getTableModel());
	}

	/**
	 * Coloca uma mensagem para o usuário
	 */
	public void notificar(String texto) {
		JOptionPane.showMessageDialog(null, texto);
	}
}
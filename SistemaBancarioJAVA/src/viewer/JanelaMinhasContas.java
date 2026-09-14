package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import controller.CtrlMinhasContas;
import controller.ICtrl;
import model.ContaBancaria;

public class JanelaMinhasContas extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;

	private JTable     tabela;
	private JScrollPane scrollPane;
	private JPanel     contentPane;

	public JanelaMinhasContas(ICtrl c, ContaBancaria[] contas) {
		super(c);
		setTitle("Minhas Contas");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 550, 320);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);

		HelperTableModel h = new HelperTableModel(contas.length > 0 ? contas : new ContaBancaria[0]);
		if (contas.length > 0) {
			tabela = new JTable(h.getTableModel());
			tabela.setEnabled(false); // read-only
		} else {
			tabela = new JTable(new String[][]{}, new String[]{"Nenhuma conta encontrada"});
		}

		scrollPane = new JScrollPane(tabela);
		scrollPane.setBounds(10, 11, 520, 230);
		contentPane.add(scrollPane);

		JButton btSair = new JButton("Fechar");
		btSair.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((CtrlMinhasContas) getCtrl()).finalizar();
			}
		});
		btSair.setBounds(220, 250, 100, 28);
		contentPane.add(btSair);

		setLocationRelativeTo(null);
		setVisible(true);
	}
}

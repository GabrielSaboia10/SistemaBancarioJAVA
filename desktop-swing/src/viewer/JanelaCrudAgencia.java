package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import controller.CtrlCrudAgencia;
import controller.ICtrl;

public class JanelaCrudAgencia extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public JanelaCrudAgencia(ICtrl c) {
		super(c);
		setTitle("JANELA CRUD Agencia");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 461, 348);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnIncluirAgencia = new JButton("Incluir Agencia");
		btnIncluirAgencia.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudAgencia ctrl = (CtrlCrudAgencia)getCtrl();
				ctrl.iniciarIncluirAgencia();
			}
		});
		btnIncluirAgencia.setBounds(10, 11, 160, 63);
		contentPane.add(btnIncluirAgencia);
		
		JButton btnAlterarAgencia = new JButton("Alterar Agencia");
		btnAlterarAgencia.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudAgencia ctrl = (CtrlCrudAgencia)getCtrl();
				ctrl.iniciarAlterarAgencia();
			}
		});
		btnAlterarAgencia.setBounds(260, 11, 160, 63);
		contentPane.add(btnAlterarAgencia);
		
		JButton btnDeletarAgencia = new JButton("Deletar Agencia");
		btnDeletarAgencia.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudAgencia ctrl = (CtrlCrudAgencia)getCtrl();
				ctrl.iniciarExcluirAgencia();
			}
		});
		btnDeletarAgencia.setBounds(10, 89, 160, 63);
		contentPane.add(btnDeletarAgencia);
		
		JButton btnSelecionarAgencia = new JButton("Selecionar Agencia");
		btnSelecionarAgencia.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudAgencia ctrl = (CtrlCrudAgencia)getCtrl();
				ctrl.iniciarSelecionarAgencia();
			}
		});
		btnSelecionarAgencia.setBounds(260, 89, 160, 63);
		contentPane.add(btnSelecionarAgencia);
		
		JButton btnVoltar = new JButton("Voltar");
		btnVoltar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudAgencia ctrl = (CtrlCrudAgencia)getCtrl();
				ctrl.finalizar();
			}
		});
		btnVoltar.setBounds(10, 244, 410, 39);
		contentPane.add(btnVoltar);
		
		JButton btnSelecionarTodasAs = new JButton("Selecionar Todas as Agencias");
		btnSelecionarTodasAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudAgencia ctrl = (CtrlCrudAgencia)getCtrl();
				ctrl.iniciarSelecionarTodasAgencias();
			}
		});
		btnSelecionarTodasAs.setBounds(10, 176, 410, 46);
		contentPane.add(btnSelecionarTodasAs);
	}
}

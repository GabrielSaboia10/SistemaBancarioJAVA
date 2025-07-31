package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import controller.CtrlCrudPessoa;
import controller.ICtrl;

public class JanelaCrudPessoa extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public JanelaCrudPessoa(ICtrl c) {
		super(c);
		setTitle("JANELA CRUD PESSOA");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 461, 348);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnIncluirPessoa = new JButton("Incluir Pessoa");
		btnIncluirPessoa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudPessoa ctrl = (CtrlCrudPessoa)getCtrl();
				ctrl.iniciarIncluirPessoa();
			}
		});
		btnIncluirPessoa.setBounds(10, 11, 160, 63);
		contentPane.add(btnIncluirPessoa);
		
		JButton btnAlterarPessoa = new JButton("Alterar Pessoa");
		btnAlterarPessoa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudPessoa ctrl = (CtrlCrudPessoa)getCtrl();
				ctrl.iniciarAlterarPessoa();
			}
		});
		btnAlterarPessoa.setBounds(260, 11, 160, 63);
		contentPane.add(btnAlterarPessoa);
		
		JButton btnDeletarPessoa = new JButton("Deletar Pessoa");
		btnDeletarPessoa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudPessoa ctrl = (CtrlCrudPessoa)getCtrl();
				ctrl.iniciarExcluirPessoa();
			}
		});
		btnDeletarPessoa.setBounds(10, 89, 160, 63);
		contentPane.add(btnDeletarPessoa);
		
		JButton btnSelecionarPessoa = new JButton("Selecionar Pessoa");
		btnSelecionarPessoa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudPessoa ctrl = (CtrlCrudPessoa)getCtrl();
				ctrl.iniciarSelecionarPessoa();
			}
		});
		btnSelecionarPessoa.setBounds(260, 89, 160, 63);
		contentPane.add(btnSelecionarPessoa);
		
		JButton btnVoltar = new JButton("Voltar");
		btnVoltar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudPessoa ctrl = (CtrlCrudPessoa)getCtrl();
				ctrl.finalizar();
			}
		});
		btnVoltar.setBounds(10, 244, 410, 39);
		contentPane.add(btnVoltar);
		
		JButton btnSelecionarTodasAs = new JButton("Selecionar Todas as Pessoas");
		btnSelecionarTodasAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudPessoa ctrl = (CtrlCrudPessoa)getCtrl();
				ctrl.iniciarSelecionarTodasPessoas();
			}
		});
		btnSelecionarTodasAs.setBounds(10, 176, 410, 46);
		contentPane.add(btnSelecionarTodasAs);
	}
}

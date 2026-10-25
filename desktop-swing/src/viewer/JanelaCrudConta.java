package viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import controller.CtrlCrudConta;
import controller.ICtrl;

public class JanelaCrudConta extends JanelaAbstrata {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public JanelaCrudConta(ICtrl c) {
		super(c);
		setTitle("JANELA CRUD ContaBancaria");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 461, 348);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnIncluirContaBancaria = new JButton("Incluir Conta Bancaria");
		btnIncluirContaBancaria.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudConta ctrl = (CtrlCrudConta)getCtrl();
				ctrl.iniciarIncluirContaBancaria();
			}
		});
		btnIncluirContaBancaria.setBounds(10, 11, 160, 63);
		contentPane.add(btnIncluirContaBancaria);
		
		JButton btnAlterarContaBancaria = new JButton("Alterar Conta Bancaria");
		btnAlterarContaBancaria.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudConta ctrl = (CtrlCrudConta)getCtrl();
				ctrl.iniciarAlterarConta();
			}
		});
		btnAlterarContaBancaria.setBounds(260, 11, 160, 63);
		contentPane.add(btnAlterarContaBancaria);
		
		JButton btnDeletarContaBancaria = new JButton("Deletar Conta Bancaria");
		btnDeletarContaBancaria.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudConta ctrl = (CtrlCrudConta)getCtrl();
				ctrl.iniciarExcluirConta();
			}
		});
		btnDeletarContaBancaria.setBounds(10, 89, 160, 63);
		contentPane.add(btnDeletarContaBancaria);
		
		JButton btnSelecionarContaBancaria = new JButton("Selecionar Conta Bancaria");
		btnSelecionarContaBancaria.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudConta ctrl = (CtrlCrudConta)getCtrl();
				ctrl.iniciarSelecionarConta();
			}
		});
		btnSelecionarContaBancaria.setBounds(260, 89, 160, 63);
		contentPane.add(btnSelecionarContaBancaria);
		
		JButton btnVoltar = new JButton("Voltar");
		btnVoltar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudConta ctrl = (CtrlCrudConta)getCtrl();
				ctrl.finalizar();
			}
		});
		btnVoltar.setBounds(10, 244, 410, 39);
		contentPane.add(btnVoltar);
		
		JButton btnSelecionarTodasAs = new JButton("Selecionar Todas as Conta Bancarias");
		btnSelecionarTodasAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CtrlCrudConta ctrl = (CtrlCrudConta)getCtrl();
				ctrl.iniciarSelecionarTodasContas();
			}
		});
		btnSelecionarTodasAs.setBounds(10, 176, 410, 46);
		contentPane.add(btnSelecionarTodasAs);
	}
}

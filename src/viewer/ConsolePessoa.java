package viewer;

import java.util.Scanner;

import controller.CtrlIncluirPessoa;
import controller.ICtrl;

public class ConsolePessoa extends Thread implements IViewer {

	private CtrlIncluirPessoa ctrl;
	private String cpf;
	private String nome;
	private int    idade;
	
	public ConsolePessoa(CtrlIncluirPessoa c) {
		this.ctrl = c;
	}
	
	public void apresentar() {
		this.start();
	}
	
	public void run() {
		System.out.println("Criação de Pessoa");
		System.out.println("=================");
		Scanner teclado = new Scanner(System.in);
		System.out.print("Entre com o CPF: ");
		cpf = teclado.nextLine();
		System.out.print("Entre com o Nome: ");
		nome = teclado.nextLine();
		System.out.print("Entre com a idade: ");
		String aux = teclado.nextLine();
		idade = Integer.parseInt(aux);		
		System.out.println("Clique [Enter] para confirmar!");
		//ctrl.efetuarInclusao(cpf, nome, idade);
	}
	
	public ICtrl getCtrl() {
		return this.ctrl;
	}
	
	public void notificar(String textoParaNotificacao) {
		System.out.println(textoParaNotificacao);
		this.apresentar();
	}
	
	public void finalizar() {
		System.out.println("OPERAÇÃO ENCERRADA!");
	}
	
}

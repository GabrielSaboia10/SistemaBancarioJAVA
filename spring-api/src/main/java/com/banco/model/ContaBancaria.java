package com.banco.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "contas")
public class ContaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1000, message = "Número da conta deve ser entre 1000 e 99999")
    @Max(value = 99999, message = "Número da conta deve ser entre 1000 e 99999")
    @Column(unique = true, nullable = false)
    private int numContaCorrente;

    @DecimalMin(value = "0.0", message = "Limite não pode ser negativo")
    @DecimalMax(value = "30000.0", message = "Limite máximo é R$ 30.000")
    @Column(nullable = false)
    private double limiteChequeEspecial;

    @Column(nullable = false)
    private double saldo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa correntista;

    @ManyToOne(optional = false)
    @JoinColumn(name = "agencia_id", nullable = false)
    private AgenciaBancaria agencia;

    public ContaBancaria() {}

    public ContaBancaria(int numContaCorrente, double limiteChequeEspecial, double saldo,
                         Pessoa correntista, AgenciaBancaria agencia) {
        this.numContaCorrente = numContaCorrente;
        this.limiteChequeEspecial = limiteChequeEspecial;
        this.saldo = saldo;
        this.correntista = correntista;
        this.agencia = agencia;
    }

    public void depositar(double valor) {
        if (valor <= 0) throw new IllegalArgumentException("Valor do depósito deve ser positivo");
        this.saldo += valor;
    }

    public void sacar(double valor) {
        if (valor <= 0) throw new IllegalArgumentException("Valor do saque deve ser positivo");
        if (this.saldo - valor < -this.limiteChequeEspecial)
            throw new IllegalArgumentException("Saldo insuficiente para saque de R$ " + valor);
        this.saldo -= valor;
    }

    public void transferir(double valor, ContaBancaria destino) {
        if (destino == null) throw new IllegalArgumentException("Conta destino não pode ser nula");
        this.sacar(valor);
        destino.depositar(valor);
    }

    public Long getId() { return id; }
    public int getNumContaCorrente() { return numContaCorrente; }
    public void setNumContaCorrente(int num) { this.numContaCorrente = num; }
    public double getLimiteChequeEspecial() { return limiteChequeEspecial; }
    public void setLimiteChequeEspecial(double limite) { this.limiteChequeEspecial = limite; }
    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }
    public Pessoa getCorrentista() { return correntista; }
    public void setCorrentista(Pessoa correntista) { this.correntista = correntista; }
    public AgenciaBancaria getAgencia() { return agencia; }
    public void setAgencia(AgenciaBancaria agencia) { this.agencia = agencia; }
}

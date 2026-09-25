package com.banco.model;

import com.banco.exception.RegraNegocioException;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;

@Entity
@Table(name = "contas")
public class ContaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int numero;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "limite_cheque_especial", nullable = false, precision = 15, scale = 2)
    private BigDecimal limiteChequeEspecial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa correntista;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agencia_id", nullable = false)
    private AgenciaBancaria agencia;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected ContaBancaria() {}

    public ContaBancaria(int numero, BigDecimal limiteChequeEspecial, Pessoa correntista, AgenciaBancaria agencia) {
        this.numero = numero;
        this.limiteChequeEspecial = limiteChequeEspecial;
        this.correntista = correntista;
        this.agencia = agencia;
    }

    /** Saldo + limite do cheque especial: o máximo que pode sair da conta. */
    public BigDecimal getSaldoDisponivel() {
        return saldo.add(limiteChequeEspecial);
    }

    public void depositar(BigDecimal valor) {
        validarValor(valor);
        saldo = saldo.add(valor);
    }

    public void sacar(BigDecimal valor) {
        validarValor(valor);
        if (valor.compareTo(getSaldoDisponivel()) > 0) {
            throw new RegraNegocioException("Saldo insuficiente. Disponível: " + formatarReais(getSaldoDisponivel()));
        }
        saldo = saldo.subtract(valor);
    }

    public void alterarLimite(BigDecimal novoLimite) {
        if (saldo.add(novoLimite).signum() < 0) {
            throw new RegraNegocioException("O novo limite deixaria a conta abaixo do saldo permitido");
        }
        this.limiteChequeEspecial = novoLimite;
    }

    /** R$ 1.234,56 (com espaço comum, não o espaço especial que o NumberFormat usa). */
    public static String formatarReais(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.of("pt", "BR")).format(valor).replace(' ', ' ');
    }

    private static void validarValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException("O valor deve ser maior que zero");
        }
    }

    public Long getId() { return id; }
    public int getNumero() { return numero; }
    public BigDecimal getSaldo() { return saldo; }
    public BigDecimal getLimiteChequeEspecial() { return limiteChequeEspecial; }
    public Pessoa getCorrentista() { return correntista; }
    public AgenciaBancaria getAgencia() { return agencia; }
    public void setAgencia(AgenciaBancaria agencia) { this.agencia = agencia; }
}

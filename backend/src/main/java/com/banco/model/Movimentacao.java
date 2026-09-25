package com.banco.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "movimentacoes")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private ContaBancaria conta;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private TipoMovimentacao tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "saldo_apos", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoApos;

    @Column(name = "numero_conta_contraparte")
    private Integer numeroContaContraparte;

    @CreationTimestamp
    @Column(name = "data_hora", nullable = false, updatable = false)
    private Instant dataHora;

    protected Movimentacao() {}

    /** Registra a movimentação já aplicada, guardando o saldo resultante da conta. */
    public Movimentacao(ContaBancaria conta, TipoMovimentacao tipo, BigDecimal valor, Integer numeroContaContraparte) {
        this.conta = conta;
        this.tipo = tipo;
        this.valor = valor;
        this.saldoApos = conta.getSaldo();
        this.numeroContaContraparte = numeroContaContraparte;
    }

    public Long getId() { return id; }
    public TipoMovimentacao getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getSaldoApos() { return saldoApos; }
    public Integer getNumeroContaContraparte() { return numeroContaContraparte; }
    public Instant getDataHora() { return dataHora; }
}

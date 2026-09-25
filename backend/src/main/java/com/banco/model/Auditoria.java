package com.banco.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Registro de uma ação administrativa. O CPF do autor é copiado (não é chave estrangeira)
 * para o histórico continuar legível mesmo se o usuário for excluído.
 */
@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_cpf", length = 14)
    private String usuarioCpf;

    @Column(nullable = false, length = 40)
    private String acao;

    @Column(nullable = false, length = 20)
    private String entidade;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(nullable = false)
    private String descricao;

    @CreationTimestamp
    @Column(name = "data_hora", nullable = false, updatable = false)
    private Instant dataHora;

    protected Auditoria() {}

    public Auditoria(Long usuarioId, String usuarioCpf, String acao, String entidade, Long entidadeId, String descricao) {
        this.usuarioId = usuarioId;
        this.usuarioCpf = usuarioCpf;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.descricao = descricao.length() > 255 ? descricao.substring(0, 255) : descricao;
    }

    public Long getId() { return id; }
    public String getUsuarioCpf() { return usuarioCpf; }
    public String getAcao() { return acao; }
    public String getEntidade() { return entidade; }
    public Long getEntidadeId() { return entidadeId; }
    public String getDescricao() { return descricao; }
    public Instant getDataHora() { return dataHora; }
}

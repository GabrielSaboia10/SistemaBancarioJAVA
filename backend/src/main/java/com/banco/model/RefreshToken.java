package com.banco.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Só o hash SHA-256 fica no banco: se ele vazar, os tokens não podem ser reutilizados. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    protected RefreshToken() {}

    public RefreshToken(String tokenHash, Usuario usuario, Instant expiraEm) {
        this.tokenHash = tokenHash;
        this.usuario = usuario;
        this.expiraEm = expiraEm;
    }

    public boolean isExpirado() {
        return expiraEm.isBefore(Instant.now());
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
}

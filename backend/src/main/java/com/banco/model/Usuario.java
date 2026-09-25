package com.banco.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, length = 100)
    private String senha;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", unique = true)
    private Pessoa pessoa;

    @Column(nullable = false)
    private boolean ativo = true;

    /** Verdadeiro enquanto o usuário estiver com uma senha temporária gerada pelo admin. */
    @Column(name = "trocar_senha", nullable = false)
    private boolean trocarSenha;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected Usuario() {}

    public Usuario(String cpf, String senhaHash, Role role, Pessoa pessoa) {
        this.cpf = cpf;
        this.senha = senhaHash;
        this.role = role;
        this.pessoa = pessoa;
    }

    public String getNomeExibicao() {
        if (role == Role.ADMIN) return "Administrador";
        return pessoa != null ? pessoa.getNome() : "Cliente";
    }

    public Long getPessoaId() {
        return pessoa != null ? pessoa.getId() : null;
    }

    /** Senha escolhida pelo próprio usuário. */
    public void definirSenha(String senhaHash) {
        this.senha = senhaHash;
        this.trocarSenha = false;
    }

    /** Senha gerada pelo admin: o usuário é obrigado a trocá-la no próximo acesso. */
    public void definirSenhaTemporaria(String senhaHash) {
        this.senha = senhaHash;
        this.trocarSenha = true;
    }

    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Long getId() { return id; }
    public String getCpf() { return cpf; }
    public String getSenha() { return senha; }
    public Role getRole() { return role; }
    public Pessoa getPessoa() { return pessoa; }
    public boolean isAtivo() { return ativo; }
    public boolean isTrocarSenha() { return trocarSenha; }
}

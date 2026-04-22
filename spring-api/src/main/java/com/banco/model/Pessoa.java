package com.banco.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "pessoas")
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "CPF é obrigatório")
    @Size(min = 14, max = 14, message = "CPF deve ter 14 caracteres (ex: 123.456.789-09)")
    @Column(unique = true, nullable = false, length = 14)
    private String cpf;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 40, message = "Nome deve ter até 40 caracteres")
    @Column(nullable = false, length = 40)
    private String nome;

    @Min(value = 0, message = "Idade não pode ser negativa")
    @Max(value = 150, message = "Idade inválida")
    @Column(nullable = false)
    private int idade;

    public Pessoa() {}

    public Pessoa(String cpf, String nome, int idade) {
        this.cpf = cpf;
        this.nome = nome;
        this.idade = idade;
    }

    public Long getId() { return id; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getIdade() { return idade; }
    public void setIdade(int idade) { this.idade = idade; }
}

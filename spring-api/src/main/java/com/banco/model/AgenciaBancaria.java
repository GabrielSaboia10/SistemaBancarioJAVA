package com.banco.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "agencias")
public class AgenciaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1, message = "Número da agência deve ser positivo")
    @Max(value = 10000, message = "Número da agência inválido")
    @Column(unique = true, nullable = false)
    private int numero;

    @NotBlank(message = "Endereço é obrigatório")
    @Size(max = 100, message = "Endereço deve ter até 100 caracteres")
    @Column(nullable = false, length = 100)
    private String endereco;

    @NotBlank(message = "Cidade é obrigatória")
    @Size(max = 25, message = "Cidade deve ter até 25 caracteres")
    @Column(nullable = false, length = 25)
    private String cidade;

    public AgenciaBancaria() {}

    public AgenciaBancaria(int numero, String endereco, String cidade) {
        this.numero = numero;
        this.endereco = endereco;
        this.cidade = cidade;
    }

    public Long getId() { return id; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
}

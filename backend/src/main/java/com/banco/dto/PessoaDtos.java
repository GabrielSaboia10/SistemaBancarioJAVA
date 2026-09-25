package com.banco.dto;

import com.banco.model.Pessoa;
import com.banco.model.Usuario;
import com.banco.util.CpfValido;
import jakarta.validation.constraints.*;

import java.time.Instant;

public final class PessoaDtos {

    private PessoaDtos() {}

    public record CriarPessoaRequest(
            @NotBlank(message = "Informe o CPF") @CpfValido String cpf,
            @NotBlank(message = "Informe o nome") @Size(max = 40, message = "O nome deve ter até 40 caracteres") String nome,
            @NotNull(message = "Informe a idade") @Min(value = 0, message = "Idade inválida") @Max(value = 150, message = "Idade inválida") Integer idade
    ) {}

    /** O CPF não pode ser alterado: ele é o login do cliente. */
    public record AtualizarPessoaRequest(
            @NotBlank(message = "Informe o nome") @Size(max = 40, message = "O nome deve ter até 40 caracteres") String nome,
            @NotNull(message = "Informe a idade") @Min(value = 0, message = "Idade inválida") @Max(value = 150, message = "Idade inválida") Integer idade
    ) {}

    /**
     * ativo = o cliente consegue acessar o sistema; senhaTemporaria = ainda não trocou a senha
     * gerada pelo admin. Ambos são nulos se a pessoa não tiver login.
     */
    public record PessoaResponse(Long id, String cpf, String nome, int idade, Instant criadoEm,
                                 Boolean ativo, Boolean senhaTemporaria) {
        public static PessoaResponse de(Pessoa p, Usuario u) {
            return new PessoaResponse(p.getId(), p.getCpf(), p.getNome(), p.getIdade(), p.getCriadoEm(),
                    u != null ? u.isAtivo() : null, u != null ? u.isTrocarSenha() : null);
        }
    }

    public record BloqueioRequest(@NotNull(message = "Informe se o acesso fica ativo") Boolean ativo) {}

    /** A senha temporária só é exibida uma vez, para o admin repassar ao cliente. */
    public record PessoaCriadaResponse(PessoaResponse pessoa, String senhaTemporaria) {}

    public record SenhaRedefinidaResponse(String cpf, String senhaTemporaria) {}
}

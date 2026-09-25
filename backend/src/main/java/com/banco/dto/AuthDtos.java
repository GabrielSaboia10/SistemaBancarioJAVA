package com.banco.dto;

import com.banco.model.Role;
import com.banco.model.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank(message = "Informe o CPF") String cpf,
            @NotBlank(message = "Informe a senha") String senha
    ) {}

    public record RefreshRequest(
            @NotBlank(message = "Informe o refresh token") String refreshToken
    ) {}

    public record TrocarSenhaRequest(
            @NotBlank(message = "Informe a senha atual") String senhaAtual,
            @NotBlank(message = "Informe a nova senha")
            @Size(min = 6, max = 72, message = "A nova senha deve ter entre 6 e 72 caracteres")
            String novaSenha
    ) {}

    /** trocarSenha = true enquanto o usuário estiver com senha temporária: o front leva direto à troca. */
    public record UsuarioResponse(Long id, String cpf, String nome, Role role, Long pessoaId, boolean trocarSenha) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getCpf(), u.getNomeExibicao(), u.getRole(), u.getPessoaId(),
                    u.isTrocarSenha());
        }
    }

    /** expiraEm = segundos até o access token expirar. */
    public record TokenResponse(String token, String refreshToken, long expiraEm, UsuarioResponse usuario) {}
}

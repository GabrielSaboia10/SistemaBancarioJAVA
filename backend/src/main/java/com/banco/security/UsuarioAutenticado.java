package com.banco.security;

import com.banco.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/** Dados do usuário logado, extraídos do JWT já validado. */
public record UsuarioAutenticado(Long id, Role role, Long pessoaId, String cpf) {

    public static UsuarioAutenticado de(Jwt jwt) {
        Object pessoaId = jwt.getClaim("pessoaId");
        return new UsuarioAutenticado(
                Long.valueOf(jwt.getSubject()),
                Role.valueOf(jwt.getClaimAsString("role")),
                pessoaId instanceof Number n ? n.longValue() : null,
                jwt.getClaimAsString("cpf"));
    }

    /** Usuário da requisição atual, se houver (vazio em tarefas agendadas e na inicialização). */
    public static Optional<UsuarioAutenticado> atual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof Jwt jwt ? Optional.of(de(jwt)) : Optional.empty();
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}

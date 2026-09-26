package com.banco.security;

import com.banco.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Checagens que não podem esperar o JWT expirar (ele vale 15 min):
 * - usuário bloqueado pelo admin perde o acesso na hora (401);
 * - usuário com senha temporária só pode ver os próprios dados e trocar a senha (403).
 * Registrado só na cadeia do Spring Security, depois da validação do JWT.
 */
public class UsuarioStatusFilter extends OncePerRequestFilter {

    public static final String CODIGO_TROCA_SENHA = "TROCA_DE_SENHA_OBRIGATORIA";

    private static final Set<String> PERMITIDAS_COM_SENHA_TEMPORARIA =
            Set.of("GET /api/auth/me", "PUT /api/auth/senha", "POST /api/auth/logout");

    private final UsuarioRepository usuarioRepo;

    public UsuarioStatusFilter(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth != null && auth.getPrincipal() instanceof Jwt jwt)) {
            chain.doFilter(request, response);
            return;
        }

        var status = usuarioRepo.findStatusById(Long.valueOf(jwt.getSubject())).orElse(null);
        if (status == null || !status.getAtivo()) {
            SecurityContextHolder.clearContext();
            RespostaErro.escrever(response, 401, "Unauthorized",
                    "Seu acesso está bloqueado. Procure sua agência.", request.getRequestURI(), null);
            return;
        }
        if (status.getTrocarSenha()
                && !PERMITIDAS_COM_SENHA_TEMPORARIA.contains(request.getMethod() + " " + request.getRequestURI())) {
            RespostaErro.escrever(response, 403, "Forbidden",
                    "Troque sua senha temporária antes de continuar.", request.getRequestURI(), CODIGO_TROCA_SENHA);
            return;
        }
        chain.doFilter(request, response);
    }
}

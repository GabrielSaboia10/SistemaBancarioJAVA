package com.banco.security;

import com.banco.config.AppProperties;
import com.banco.config.AppProperties.Regra;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Aplica o rate limit antes de a requisição chegar nos controllers.
 * Não é um @Component de propósito: é registrado só na cadeia do Spring Security
 * (depois da autenticação JWT), para não rodar duas vezes nem antes de saber quem é o usuário.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Pattern OPERACAO = Pattern.compile("^/api/contas/\\d+/(depositar|sacar|transferir)$");

    private final RateLimiter rateLimiter;
    private final AppProperties.RateLimit config;

    public RateLimitFilter(RateLimiter rateLimiter, AppProperties.RateLimit config) {
        this.rateLimiter = rateLimiter;
        this.config = config;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !config.habilitado() || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();

        String chave;
        Regra regra;
        if (uri.equals("/api/auth/login") || uri.equals("/api/auth/refresh")) {
            chave = "auth:" + ip;
            regra = config.auth();
        } else if ("POST".equals(request.getMethod()) && OPERACAO.matcher(uri).matches()) {
            chave = "op:" + usuarioOuIp(ip);
            regra = config.operacoes();
        } else {
            chave = "geral:" + ip;
            regra = config.geral();
        }

        RateLimiter.Resultado r = rateLimiter.consumir(chave, regra);
        response.setHeader("X-RateLimit-Limit", String.valueOf(regra.limite()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(r.restantes()));

        if (!r.permitido()) {
            response.setHeader("Retry-After", String.valueOf(r.segundosParaLiberar()));
            RespostaErro.escrever(response, 429, "Too Many Requests",
                    "Muitas requisições. Tente novamente em " + r.segundosParaLiberar() + " segundos.", uri);
            return;
        }
        chain.doFilter(request, response);
    }

    /** Operações financeiras são limitadas por usuário (o JWT já foi validado neste ponto). */
    private static String usuarioOuIp(String ip) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return "u" + jwt.getSubject();
        }
        return ip;
    }
}

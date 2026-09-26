package com.banco.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors, Admin admin, RateLimit rateLimit) {

    public record Jwt(String secret, int accessTokenMinutos, int refreshTokenDias) {}

    public record Cors(List<String> allowedOrigins) {}

    public record Admin(String cpf, String senha) {}

    /**
     * Limites por janela fixa. auth = login/refresh por IP; loginCpf = senhas erradas por CPF;
     * operacoes = depósito/saque/transferência por usuário; geral = demais rotas por IP.
     */
    public record RateLimit(
            @DefaultValue("true") boolean habilitado,
            @DefaultValue Regra auth,
            @DefaultValue Regra loginCpf,
            @DefaultValue Regra operacoes,
            @DefaultValue Regra geral
    ) {}

    public record Regra(@DefaultValue("10") int limite, @DefaultValue("15m") Duration janela) {}
}

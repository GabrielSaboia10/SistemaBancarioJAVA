package com.banco.security;

import com.banco.config.AppProperties.Regra;

/**
 * Limite de requisições por janela fixa: cada chave (ex.: "auth:IP") pode consumir até
 * {@code regra.limite()} fichas por {@code regra.janela()}.
 *
 * Implementações: {@link RateLimiterMemoria} (padrão, uma instância da API) e
 * {@link RateLimiterRedis} (várias instâncias compartilhando o contador), escolhidas por
 * {@code app.rate-limit.armazenamento}.
 */
public interface RateLimiter {

    record Resultado(boolean permitido, int restantes, long segundosParaLiberar) {}

    /** Consome uma ficha da chave e diz se a requisição pode seguir. */
    Resultado consumir(String chave, Regra regra);

    /** Consulta sem consumir (usado para saber se um CPF está bloqueado). */
    Resultado consultar(String chave, Regra regra);

    void limpar(String chave);
}

package com.banco.security;

import com.banco.config.AppProperties.Regra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Contadores no Redis, compartilhados entre todas as instâncias da API.
 * O INCR + PEXPIRE roda num script Lua, então é atômico mesmo com várias instâncias.
 */
@Component
@ConditionalOnProperty(name = "app.rate-limit.armazenamento", havingValue = "redis")
public class RateLimiterRedis implements RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterRedis.class);
    private static final String PREFIXO = "rate-limit:";

    /** Incrementa; na primeira ficha da janela define a expiração. Devolve {consumidas, ms restantes}. */
    private static final RedisScript<List> CONSUMIR = RedisScript.of("""
            local consumidas = redis.call('INCR', KEYS[1])
            if consumidas == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]) end
            return {consumidas, redis.call('PTTL', KEYS[1])}
            """, List.class);

    private final StringRedisTemplate redis;

    public RateLimiterRedis(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Resultado consumir(String chave, Regra regra) {
        try {
            List<?> r = redis.execute(CONSUMIR, List.of(PREFIXO + chave), String.valueOf(regra.janela().toMillis()));
            long consumidas = ((Number) r.get(0)).longValue();
            long msRestantes = ((Number) r.get(1)).longValue();
            boolean permitido = consumidas <= regra.limite();
            return new Resultado(permitido, (int) Math.max(0, regra.limite() - consumidas),
                    permitido ? 0 : Math.max(1, msRestantes / 1000));
        } catch (DataAccessException e) {
            return liberarPorFalha(regra, e);
        }
    }

    @Override
    public Resultado consultar(String chave, Regra regra) {
        try {
            String valor = redis.opsForValue().get(PREFIXO + chave);
            if (valor == null) {
                return new Resultado(true, regra.limite(), 0);
            }
            long consumidas = Long.parseLong(valor);
            Long msRestantes = redis.getExpire(PREFIXO + chave, TimeUnit.MILLISECONDS);
            return new Resultado(consumidas < regra.limite(), (int) Math.max(0, regra.limite() - consumidas),
                    Math.max(1, (msRestantes == null ? 0 : msRestantes) / 1000));
        } catch (DataAccessException e) {
            return liberarPorFalha(regra, e);
        }
    }

    @Override
    public void limpar(String chave) {
        try {
            redis.delete(PREFIXO + chave);
        } catch (DataAccessException e) {
            log.warn("Redis indisponível ao limpar o rate limit: {}", e.getMessage());
        }
    }

    /** Redis fora do ar não pode derrubar a API: libera a requisição e registra o problema. */
    private static Resultado liberarPorFalha(Regra regra, DataAccessException e) {
        log.warn("Redis indisponível, rate limit ignorado nesta requisição: {}", e.getMessage());
        return new Resultado(true, regra.limite(), 0);
    }
}

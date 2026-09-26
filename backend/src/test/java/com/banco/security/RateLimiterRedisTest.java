package com.banco.security;

import com.banco.config.AppProperties.Regra;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Roda contra um Redis de verdade (REDIS_HOST/REDIS_PORT, padrão localhost:6379).
 * Se não houver Redis disponível, o teste é pulado em vez de falhar.
 */
class RateLimiterRedisTest {

    private LettuceConnectionFactory conexao;
    private RateLimiterRedis limiter;
    private final String chave = "teste:" + UUID.randomUUID();

    @BeforeEach
    void conectar() {
        String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        int porta = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        conexao = new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, porta));
        conexao.afterPropertiesSet();
        conexao.start();
        StringRedisTemplate template = new StringRedisTemplate(conexao);
        boolean disponivel;
        try {
            disponivel = "PONG".equals(template.execute(c -> c.ping(), true));
        } catch (RuntimeException e) {
            disponivel = false;
        }
        assumeTrue(disponivel, "Redis indisponível em " + host + ":" + porta);
        limiter = new RateLimiterRedis(template);
    }

    @AfterEach
    void desconectar() {
        if (limiter != null) limiter.limpar(chave);
        conexao.destroy();
    }

    @Test
    void bloqueiaAoPassarDoLimiteEInformaQuandoLibera() {
        Regra regra = new Regra(3, Duration.ofMinutes(1));

        assertThat(limiter.consumir(chave, regra).restantes()).isEqualTo(2);
        limiter.consumir(chave, regra);
        assertThat(limiter.consumir(chave, regra).permitido()).isTrue();

        RateLimiter.Resultado bloqueado = limiter.consumir(chave, regra);
        assertThat(bloqueado.permitido()).isFalse();
        assertThat(bloqueado.segundosParaLiberar()).isBetween(1L, 60L);
        assertThat(limiter.consultar(chave, regra).permitido()).isFalse();
    }

    @Test
    void janelaExpiraSozinha() throws InterruptedException {
        Regra regra = new Regra(1, Duration.ofMillis(300));
        limiter.consumir(chave, regra);
        assertThat(limiter.consumir(chave, regra).permitido()).isFalse();

        Thread.sleep(400);
        assertThat(limiter.consumir(chave, regra).permitido()).isTrue();
    }

    @Test
    void limparZeraOContador() {
        Regra regra = new Regra(1, Duration.ofMinutes(1));
        limiter.consumir(chave, regra);
        limiter.limpar(chave);
        assertThat(limiter.consultar(chave, regra).restantes()).isEqualTo(1);
    }
}

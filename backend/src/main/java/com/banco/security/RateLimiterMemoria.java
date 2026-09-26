package com.banco.security;

import com.banco.config.AppProperties.Regra;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/** Contadores na memória da própria API. Suficiente enquanto houver uma única instância. */
@Component
@ConditionalOnProperty(name = "app.rate-limit.armazenamento", havingValue = "memoria", matchIfMissing = true)
public class RateLimiterMemoria implements RateLimiter {

    private record Janela(Instant fim, int consumidas) {}

    private final ConcurrentHashMap<String, Janela> janelas = new ConcurrentHashMap<>();

    @Override
    public Resultado consumir(String chave, Regra regra) {
        Instant agora = Instant.now();
        Janela janela = janelas.compute(chave, (k, atual) ->
                atual == null || !atual.fim().isAfter(agora)
                        ? new Janela(agora.plus(regra.janela()), 1)
                        : new Janela(atual.fim(), atual.consumidas() + 1));
        boolean permitido = janela.consumidas() <= regra.limite();
        return new Resultado(permitido, Math.max(0, regra.limite() - janela.consumidas()),
                permitido ? 0 : segundosAte(janela.fim(), agora));
    }

    @Override
    public Resultado consultar(String chave, Regra regra) {
        Instant agora = Instant.now();
        Janela janela = janelas.get(chave);
        if (janela == null || !janela.fim().isAfter(agora)) {
            return new Resultado(true, regra.limite(), 0);
        }
        return new Resultado(janela.consumidas() < regra.limite(),
                Math.max(0, regra.limite() - janela.consumidas()), segundosAte(janela.fim(), agora));
    }

    @Override
    public void limpar(String chave) {
        janelas.remove(chave);
    }

    private static long segundosAte(Instant fim, Instant agora) {
        return Math.max(1, Duration.between(agora, fim).toSeconds());
    }

    /** Remove janelas vencidas para o mapa não crescer indefinidamente. */
    @Scheduled(fixedDelay = 300_000)
    void removerExpiradas() {
        Instant agora = Instant.now();
        janelas.entrySet().removeIf(e -> !e.getValue().fim().isAfter(agora));
    }
}

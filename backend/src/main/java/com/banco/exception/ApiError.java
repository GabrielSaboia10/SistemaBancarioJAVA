package com.banco.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/** Formato único de erro devolvido por toda a API. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos
) {
    public static ApiError of(int status, String erro, String mensagem, String caminho) {
        return new ApiError(Instant.now(), status, erro, mensagem, caminho, null);
    }
}

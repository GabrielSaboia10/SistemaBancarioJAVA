package com.banco.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Escreve erros no mesmo formato do GlobalExceptionHandler para respostas geradas
 * pelos filtros de segurança, que rodam antes dos controllers.
 */
final class RespostaErro {

    private RespostaErro() {}

    static void escrever(HttpServletResponse response, int status, String erro, String mensagem, String caminho)
            throws IOException {
        escrever(response, status, erro, mensagem, caminho, null);
    }

    /** codigo: identificador estável para o front reagir (ex.: TROCA_DE_SENHA_OBRIGATORIA). */
    static void escrever(HttpServletResponse response, int status, String erro, String mensagem, String caminho,
                         String codigo) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"timestamp\":\"" + Instant.now() + "\",\"status\":" + status
                + ",\"erro\":\"" + erro + "\",\"mensagem\":\"" + mensagem
                + "\",\"caminho\":\"" + escapar(caminho) + "\""
                + (codigo != null ? ",\"codigo\":\"" + codigo + "\"" : "") + "}");
    }

    private static String escapar(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

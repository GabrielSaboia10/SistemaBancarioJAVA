package com.banco;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Atalhos para os testes de integração: login, cadastro e chamadas autenticadas. */
public abstract class ApiTestSupport {

    private static final AtomicInteger seq = new AtomicInteger();

    @Autowired
    protected MockMvc mvc;

    protected String tokenAdmin() throws Exception {
        return login("000.000.000-00", "admin123");
    }

    protected String login(String cpf, String senha) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpf\":\"" + cpf + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    public static final String SENHA_DEFINITIVA = "senha-definitiva-1";

    /**
     * Primeiro acesso de um cliente criado pelo admin: entra com a senha temporária,
     * troca pela definitiva e entra de novo. Devolve o token já liberado para operar.
     */
    protected String primeiroAcesso(Object[] cliente) throws Exception {
        String cpf = (String) cliente[1];
        String temporario = login(cpf, (String) cliente[2]);
        chamar(put("/api/auth/senha"), temporario,
                "{\"senhaAtual\":\"" + cliente[2] + "\",\"novaSenha\":\"" + SENHA_DEFINITIVA + "\"}")
                .andExpect(status().isNoContent());
        return login(cpf, SENHA_DEFINITIVA);
    }

    protected ResultActions chamar(MockHttpServletRequestBuilder req, String token, String json) throws Exception {
        if (token != null) req.header("Authorization", "Bearer " + token);
        if (json != null) req.contentType(MediaType.APPLICATION_JSON).content(json);
        return mvc.perform(req);
    }

    protected int proximo() {
        return seq.incrementAndGet();
    }

    protected long criarAgencia(String admin) throws Exception {
        String body = chamar(post("/api/agencias"), admin,
                "{\"numero\":" + (100 + proximo()) + ",\"endereco\":\"Rua Teste, 1\",\"cidade\":\"Rio\"}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    /** Gera um CPF válido e único a partir de um contador (calcula os dígitos verificadores). */
    protected String novoCpf() {
        String base = String.format("%09d", 200_000_000 + proximo() * 7919);
        int d1 = digito(base, 10);
        int d2 = digito(base + d1, 11);
        String d = base + d1 + d2;
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    private static int digito(String digitos, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < digitos.length(); i++) soma += (digitos.charAt(i) - '0') * (pesoInicial - i);
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    /** Devolve {pessoaId, cpf, senhaTemporaria}. */
    protected Object[] criarCliente(String admin) throws Exception {
        String cpf = novoCpf();
        String body = chamar(post("/api/pessoas"), admin,
                "{\"cpf\":\"" + cpf + "\",\"nome\":\"Cliente Teste\",\"idade\":30}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return new Object[]{((Number) JsonPath.read(body, "$.pessoa.id")).longValue(), cpf,
                JsonPath.read(body, "$.senhaTemporaria")};
    }

    /** Devolve {contaId, numero}. */
    protected long[] criarConta(String admin, long pessoaId, long agenciaId, String saldo, String limite) throws Exception {
        int numero = 10000 + proximo();
        String body = chamar(post("/api/contas"), admin,
                "{\"numero\":" + numero + ",\"limiteChequeEspecial\":" + limite + ",\"saldoInicial\":" + saldo
                        + ",\"pessoaId\":" + pessoaId + ",\"agenciaId\":" + agenciaId + "}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return new long[]{((Number) JsonPath.read(body, "$.id")).longValue(), numero};
    }
}

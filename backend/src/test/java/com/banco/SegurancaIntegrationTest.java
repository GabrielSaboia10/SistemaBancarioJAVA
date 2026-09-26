package com.banco;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Verifica quem pode acessar o quê: 401 sem login, 403 sem permissão, dono da conta, refresh token. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SegurancaIntegrationTest extends ApiTestSupport {

    @Test
    void rotasProtegidasExigemToken() throws Exception {
        mvc.perform(get("/api/contas")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mvc.perform(get("/api/pessoas")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/contas/1/sacar")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/contas").header("Authorization", "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotasPublicas() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void loginComSenhaErradaDevolve401() throws Exception {
        chamar(post("/api/auth/login"), null, "{\"cpf\":\"000.000.000-00\",\"senha\":\"errada\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("CPF ou senha inválidos"));
    }

    @Test
    void rotaNaoMapeadaEhNegada() throws Exception {
        chamar(get("/api/qualquer-coisa"), tokenAdmin(), null).andExpect(status().isForbidden());
    }

    @Test
    void clienteNaoAcessaRotasDeAdmin() throws Exception {
        String admin = tokenAdmin();
        Object[] cliente = criarCliente(admin);
        String token = primeiroAcesso(cliente);

        chamar(get("/api/pessoas"), token, null).andExpect(status().isForbidden());
        chamar(get("/api/agencias"), token, null).andExpect(status().isForbidden());
        chamar(post("/api/contas"), token, "{}").andExpect(status().isForbidden());
        chamar(delete("/api/contas/1"), token, null).andExpect(status().isForbidden());
        chamar(put("/api/contas/1"), token, "{}").andExpect(status().isForbidden());
    }

    @Test
    void clienteNaoMexeNaContaDeOutroCliente() throws Exception {
        String admin = tokenAdmin();
        long agencia = criarAgencia(admin);
        Object[] maria = criarCliente(admin);
        Object[] joao = criarCliente(admin);
        long[] contaJoao = criarConta(admin, (Long) joao[0], agencia, "500", "0");
        criarConta(admin, (Long) maria[0], agencia, "100", "0");
        String tokenMaria = primeiroAcesso(maria);

        String id = String.valueOf(contaJoao[0]);
        chamar(get("/api/contas/" + id), tokenMaria, null).andExpect(status().isForbidden());
        chamar(get("/api/contas/" + id + "/extrato"), tokenMaria, null).andExpect(status().isForbidden());
        chamar(post("/api/contas/" + id + "/sacar"), tokenMaria, "{\"valor\":10}").andExpect(status().isForbidden());
        chamar(post("/api/contas/" + id + "/transferir"), tokenMaria,
                "{\"numeroContaDestino\":1,\"valor\":10}").andExpect(status().isForbidden());

        // A listagem da Maria só traz a conta dela
        chamar(get("/api/contas"), tokenMaria, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
        // E o saldo do João continua intacto
        chamar(get("/api/contas/" + id), admin, null).andExpect(jsonPath("$.saldo").value(500.0));
    }

    @Test
    void refreshTokenEhRotacionadoENaoPodeSerReutilizado() throws Exception {
        String body = chamar(post("/api/auth/login"), null, "{\"cpf\":\"00000000000\",\"senha\":\"admin123\"}")
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String refresh = JsonPath.read(body, "$.refreshToken");

        chamar(post("/api/auth/refresh"), null, "{\"refreshToken\":\"" + refresh + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
        chamar(post("/api/auth/refresh"), null, "{\"refreshToken\":\"" + refresh + "\"}")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void trocarSenhaEncerraSessoes() throws Exception {
        String admin = tokenAdmin();
        Object[] cliente = criarCliente(admin);
        String body = chamar(post("/api/auth/login"), null,
                "{\"cpf\":\"" + cliente[1] + "\",\"senha\":\"" + cliente[2] + "\"}")
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.token");
        String refresh = JsonPath.read(body, "$.refreshToken");

        chamar(put("/api/auth/senha"), token, "{\"senhaAtual\":\"errada\",\"novaSenha\":\"nova-senha-1\"}")
                .andExpect(status().isUnprocessableContent());
        chamar(put("/api/auth/senha"), token, "{\"senhaAtual\":\"" + cliente[2] + "\",\"novaSenha\":\"nova-senha-1\"}")
                .andExpect(status().isNoContent());

        chamar(post("/api/auth/refresh"), null, "{\"refreshToken\":\"" + refresh + "\"}")
                .andExpect(status().isUnauthorized());
        login((String) cliente[1], "nova-senha-1");
    }
}

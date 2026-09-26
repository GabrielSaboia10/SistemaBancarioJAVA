package com.banco;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "app.rate-limit.auth.limite=6",
        "app.rate-limit.login-cpf.limite=3",
        "app.rate-limit.operacoes.limite=2"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimitIntegrationTest extends ApiTestSupport {

    /** Cada IP falso tem seu próprio contador, então os testes não interferem entre si. */
    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder loginDoIp(String ip, String cpf, String senha) {
        return post("/api/auth/login").with(r -> { r.setRemoteAddr(ip); return r; })
                .contentType("application/json")
                .content("{\"cpf\":\"" + cpf + "\",\"senha\":\"" + senha + "\"}");
    }

    @Test
    void cpfEhBloqueadoAposSenhasErradasMesmoComASenhaCerta() throws Exception {
        Object[] cliente = criarCliente(login("000.000.000-00", "admin123"));
        String cpf = (String) cliente[1];
        for (int i = 0; i < 3; i++) {
            mvc.perform(loginDoIp("10.0.0." + i, cpf, "errada")).andExpect(status().isUnauthorized());
        }
        // Vindo de outro IP e com a senha certa: o CPF continua bloqueado
        mvc.perform(loginDoIp("10.0.0.99", cpf, (String) cliente[2]))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("este CPF")));
    }

    @Test
    void ipEhLimitadoNoLogin() throws Exception {
        // Um CPF diferente por tentativa, para testar só o limite por IP
        for (int i = 0; i < 6; i++) {
            mvc.perform(loginDoIp("10.1.1.1", String.format("%011d", i + 1), "x")).andExpect(status().isUnauthorized());
        }
        mvc.perform(loginDoIp("10.1.1.1", String.format("%011d", 99), "x"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void operacoesFinanceirasSaoLimitadasPorUsuario() throws Exception {
        String admin = login("000.000.000-00", "admin123");
        long agencia = criarAgencia(admin);
        Object[] cliente = criarCliente(admin);
        long conta = criarConta(admin, (Long) cliente[0], agencia, "100", "0")[0];
        String token = primeiroAcesso(cliente);

        for (int i = 0; i < 2; i++) {
            chamar(post("/api/contas/" + conta + "/depositar"), token, "{\"valor\":1}").andExpect(status().isOk());
        }
        chamar(post("/api/contas/" + conta + "/depositar"), token, "{\"valor\":1}")
                .andExpect(status().isTooManyRequests());
    }
}

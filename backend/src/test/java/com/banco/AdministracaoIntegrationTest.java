package com.banco;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Senha temporária, bloqueio de acesso, redefinição de senha, auditoria, busca e extrato. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdministracaoIntegrationTest extends ApiTestSupport {

    @Test
    void senhaTemporariaSoPermiteTrocarASenha() throws Exception {
        String admin = tokenAdmin();
        Object[] cliente = criarCliente(admin);
        String token = login((String) cliente[1], (String) cliente[2]);

        chamar(get("/api/auth/me"), token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trocarSenha").value(true));
        chamar(get("/api/contas"), token, null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("TROCA_DE_SENHA_OBRIGATORIA"));
        chamar(get("/api/pessoas?busca=" + cliente[1]), admin, null)
                .andExpect(jsonPath("$.content[0].ativo").value(true))
                .andExpect(jsonPath("$.content[0].senhaTemporaria").value(true));

        String definitivo = primeiroAcesso(cliente);
        chamar(get("/api/pessoas?busca=" + cliente[1]), admin, null)
                .andExpect(jsonPath("$.content[0].senhaTemporaria").value(false));
        chamar(get("/api/contas"), definitivo, null).andExpect(status().isOk());
        chamar(get("/api/auth/me"), definitivo, null).andExpect(jsonPath("$.trocarSenha").value(false));
    }

    @Test
    void bloqueioDerrubaOAcessoNaHora() throws Exception {
        String admin = tokenAdmin();
        Object[] cliente = criarCliente(admin);
        String token = primeiroAcesso(cliente);
        long pessoaId = (Long) cliente[0];

        chamar(put("/api/pessoas/" + pessoaId + "/bloqueio"), admin, "{\"ativo\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));

        // O JWT ainda não expirou, mas o acesso já foi cortado
        chamar(get("/api/contas"), token, null).andExpect(status().isUnauthorized());
        chamar(post("/api/auth/login"), null,
                "{\"cpf\":\"" + cliente[1] + "\",\"senha\":\"" + SENHA_DEFINITIVA + "\"}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value(containsString("bloqueado")));

        chamar(put("/api/pessoas/" + pessoaId + "/bloqueio"), admin, "{\"ativo\":true}").andExpect(status().isOk());
        login((String) cliente[1], SENHA_DEFINITIVA);
    }

    @Test
    void redefinirSenhaGeraNovaSenhaTemporaria() throws Exception {
        String admin = tokenAdmin();
        Object[] cliente = criarCliente(admin);
        primeiroAcesso(cliente);

        String body = chamar(post("/api/pessoas/" + cliente[0] + "/redefinir-senha"), admin, null)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String novaTemporaria = JsonPath.read(body, "$.senhaTemporaria");

        chamar(post("/api/auth/login"), null,
                "{\"cpf\":\"" + cliente[1] + "\",\"senha\":\"" + SENHA_DEFINITIVA + "\"}")
                .andExpect(status().isUnauthorized());
        String token = login((String) cliente[1], novaTemporaria);
        chamar(get("/api/auth/me"), token, null).andExpect(jsonPath("$.trocarSenha").value(true));
    }

    @Test
    void acoesDoAdminFicamNaAuditoria() throws Exception {
        String admin = tokenAdmin();
        long agencia = criarAgencia(admin);
        Object[] cliente = criarCliente(admin);
        long conta = criarConta(admin, (Long) cliente[0], agencia, "50", "0")[0];
        chamar(post("/api/contas/" + conta + "/depositar"), admin, "{\"valor\":10}").andExpect(status().isOk());

        chamar(get("/api/auditoria?entidade=CONTA"), admin, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].acao").value("DEPOSITO"))
                .andExpect(jsonPath("$.content[0].usuarioCpf").value("000.000.000-00"))
                .andExpect(jsonPath("$.content[1].acao").value("CRIAR"));
        chamar(get("/api/auditoria?entidade=PESSOA"), admin, null)
                .andExpect(jsonPath("$.content[*].descricao", hasItem(containsString((String) cliente[1]))));

        String token = primeiroAcesso(cliente);
        chamar(get("/api/auditoria"), token, null).andExpect(status().isForbidden());
    }

    @Test
    void buscaDeContasPorNumeroENome() throws Exception {
        String admin = tokenAdmin();
        long agencia = criarAgencia(admin);
        Object[] maria = criarCliente(admin);
        Object[] joao = criarCliente(admin);
        long[] contaMaria = criarConta(admin, (Long) maria[0], agencia, "0", "0");
        long[] contaJoao = criarConta(admin, (Long) joao[0], agencia, "0", "0");

        chamar(get("/api/contas?busca=" + contaMaria[1]), admin, null)
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].numero").value(contaMaria[1]));
        chamar(get("/api/contas?busca=cliente teste"), admin, null)
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)));

        // Cliente buscando o número da conta de outro não encontra nada
        String tokenMaria = primeiroAcesso(maria);
        chamar(get("/api/contas?busca=" + contaJoao[1]), tokenMaria, null)
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void extratoPorPeriodoEExportacaoCsv() throws Exception {
        String admin = tokenAdmin();
        long agencia = criarAgencia(admin);
        Object[] cliente = criarCliente(admin);
        long conta = criarConta(admin, (Long) cliente[0], agencia, "100.50", "0")[0];
        chamar(post("/api/contas/" + conta + "/sacar"), admin, "{\"valor\":20.25}").andExpect(status().isOk());

        String hoje = LocalDate.now().toString();
        String amanha = LocalDate.now().plusDays(1).toString();
        chamar(get("/api/contas/" + conta + "/extrato?de=" + hoje + "&ate=" + hoje), admin, null)
                .andExpect(jsonPath("$.totalElements").value(2));
        chamar(get("/api/contas/" + conta + "/extrato?de=" + amanha), admin, null)
                .andExpect(jsonPath("$.totalElements").value(0));
        chamar(get("/api/contas/" + conta + "/extrato?de=" + amanha + "&ate=" + hoje), admin, null)
                .andExpect(status().isUnprocessableContent());

        chamar(get("/api/contas/" + conta + "/extrato.csv"), admin, null)
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", startsWith("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("extrato-conta-" + conta + ".csv")))
                .andExpect(content().string(containsString("Data;Tipo;Valor;Saldo após;Conta contraparte")))
                .andExpect(content().string(containsString("Saque;-20,25;80,25;")))
                .andExpect(content().string(containsString("Depósito;100,50;100,50;")));
    }
}

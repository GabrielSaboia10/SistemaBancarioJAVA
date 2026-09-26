package com.banco;

import com.banco.model.Role;
import com.banco.repository.ContaBancariaRepository;
import com.banco.security.UsuarioAutenticado;
import com.banco.service.ContaBancariaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OperacoesIntegrationTest extends ApiTestSupport {

    @Autowired
    ContaBancariaService contaService;

    @Autowired
    ContaBancariaRepository contaRepo;

    @Test
    void fluxoCompletoDoCliente() throws Exception {
        String admin = tokenAdmin();
        long agencia = criarAgencia(admin);
        Object[] cliente = criarCliente(admin);
        long[] conta = criarConta(admin, (Long) cliente[0], agencia, "100.00", "50.00");
        String token = primeiroAcesso(cliente);
        String base = "/api/contas/" + conta[0];

        chamar(post(base + "/depositar"), token, "{\"valor\":50.00}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(150.0));

        // Disponível = 150 + 50 de limite = 200
        chamar(post(base + "/sacar"), token, "{\"valor\":200.01}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("Saldo insuficiente. Disponível: R$ 200,00"));

        chamar(post(base + "/sacar"), token, "{\"valor\":200.00}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(-50.0))
                .andExpect(jsonPath("$.saldoDisponivel").value(0.0));

        chamar(get(base + "/extrato"), token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].tipo").value("SAQUE"))
                .andExpect(jsonPath("$.content[0].saldoApos").value(-50.0))
                .andExpect(jsonPath("$.content[2].tipo").value("DEPOSITO"));

        chamar(get("/api/contas/stats"), token, null)
                .andExpect(jsonPath("$.totalContas").value(1))
                .andExpect(jsonPath("$.saldoTotal").value(-50.0))
                .andExpect(jsonPath("$.totalClientes").doesNotExist());
    }

    @Test
    void transferenciaMovimentaAsDuasContasERegistraExtrato() throws Exception {
        String admin = tokenAdmin();
        long agencia = criarAgencia(admin);
        Object[] maria = criarCliente(admin);
        Object[] joao = criarCliente(admin);
        long[] origem = criarConta(admin, (Long) maria[0], agencia, "300", "0");
        long[] destino = criarConta(admin, (Long) joao[0], agencia, "0", "0");
        String token = primeiroAcesso(maria);

        chamar(post("/api/contas/" + origem[0] + "/transferir"), token,
                "{\"numeroContaDestino\":" + destino[1] + ",\"valor\":120.50}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(179.5));

        chamar(get("/api/contas/" + destino[0]), admin, null).andExpect(jsonPath("$.saldo").value(120.5));
        chamar(get("/api/contas/" + destino[0] + "/extrato"), admin, null)
                .andExpect(jsonPath("$.content[0].tipo").value("TRANSFERENCIA_RECEBIDA"))
                .andExpect(jsonPath("$.content[0].numeroContaContraparte").value(origem[1]));

        chamar(post("/api/contas/" + origem[0] + "/transferir"), token,
                "{\"numeroContaDestino\":" + origem[1] + ",\"valor\":1}")
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void validacoesDeEntrada() throws Exception {
        String admin = tokenAdmin();
        chamar(post("/api/pessoas"), admin, "{\"cpf\":\"123.456.789-00\",\"nome\":\"\",\"idade\":200}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cpf").value("CPF inválido"))
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.idade").exists());

        long agencia = criarAgencia(admin);
        Object[] cliente = criarCliente(admin);
        long[] conta = criarConta(admin, (Long) cliente[0], agencia, "10", "0");
        chamar(post("/api/contas/" + conta[0] + "/depositar"), admin, "{\"valor\":0}")
                .andExpect(status().isBadRequest());
        chamar(post("/api/contas/" + conta[0] + "/depositar"), admin, "{\"valor\":1.999}")
                .andExpect(status().isBadRequest());
        chamar(get("/api/contas?sort=campoQueNaoExiste"), admin, null)
                .andExpect(status().isBadRequest());
    }

    @Test
    void regrasDeExclusao() throws Exception {
        String admin = tokenAdmin();
        long agencia = criarAgencia(admin);
        Object[] cliente = criarCliente(admin);
        long[] conta = criarConta(admin, (Long) cliente[0], agencia, "10", "0");

        chamar(delete("/api/pessoas/" + cliente[0]), admin, null).andExpect(status().isConflict());
        chamar(delete("/api/agencias/" + agencia), admin, null).andExpect(status().isConflict());
        chamar(delete("/api/contas/" + conta[0]), admin, null).andExpect(status().isUnprocessableContent());

        chamar(post("/api/contas/" + conta[0] + "/sacar"), admin, "{\"valor\":10}").andExpect(status().isOk());
        chamar(delete("/api/contas/" + conta[0]), admin, null).andExpect(status().isNoContent());
        chamar(delete("/api/pessoas/" + cliente[0]), admin, null).andExpect(status().isNoContent());
        chamar(delete("/api/agencias/" + agencia), admin, null).andExpect(status().isNoContent());
    }

    /**
     * 20 saques de R$ 10 ao mesmo tempo numa conta com R$ 100 e sem limite:
     * com o lock pessimista, exatamente 10 passam e o saldo termina em zero (nunca negativo).
     */
    @Test
    void saquesSimultaneosNaoDeixamSaldoNegativo() throws Exception {
        String admin = tokenAdmin();
        long agencia = criarAgencia(admin);
        Object[] cliente = criarCliente(admin);
        long contaId = criarConta(admin, (Long) cliente[0], agencia, "100", "0")[0];
        UsuarioAutenticado adminUser = new UsuarioAutenticado(1L, Role.ADMIN, null, "000.000.000-00");

        AtomicInteger sucessos = new AtomicInteger();
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(20);
        List<Future<?>> tarefas = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            tarefas.add(pool.submit(() -> {
                largada.await();
                try {
                    contaService.sacar(contaId, new BigDecimal("10"), adminUser);
                    sucessos.incrementAndGet();
                } catch (RuntimeException esperado) {
                    // saldo insuficiente
                }
                return null;
            }));
        }
        largada.countDown();
        for (Future<?> t : tarefas) t.get(30, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(sucessos.get()).isEqualTo(10);
        assertThat(contaRepo.findById(contaId).orElseThrow().getSaldo()).isEqualByComparingTo("0");
    }
}

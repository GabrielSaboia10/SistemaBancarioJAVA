package com.banco.config;

import com.banco.model.*;
import com.banco.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Dados de exemplo para testar o sistema localmente. Roda só no perfil dev e só com o banco vazio.
 * Clientes: 529.982.247-25 e 111.444.777-35, ambos com a senha "cliente123".
 */
@Component
@Profile("dev")
@Order(2)
public class DevDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);
    private static final String SENHA_CLIENTES = "cliente123";

    private final PessoaRepository pessoaRepo;
    private final UsuarioRepository usuarioRepo;
    private final AgenciaBancariaRepository agenciaRepo;
    private final ContaBancariaRepository contaRepo;
    private final MovimentacaoRepository movRepo;
    private final PasswordEncoder encoder;

    public DevDataSeeder(PessoaRepository pessoaRepo, UsuarioRepository usuarioRepo, AgenciaBancariaRepository agenciaRepo,
                         ContaBancariaRepository contaRepo, MovimentacaoRepository movRepo, PasswordEncoder encoder) {
        this.pessoaRepo = pessoaRepo;
        this.usuarioRepo = usuarioRepo;
        this.agenciaRepo = agenciaRepo;
        this.contaRepo = contaRepo;
        this.movRepo = movRepo;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (pessoaRepo.count() > 0) return;

        AgenciaBancaria centro = agenciaRepo.save(new AgenciaBancaria(1, "Av. Rio Branco, 100 - Centro", "Rio de Janeiro"));
        AgenciaBancaria niteroi = agenciaRepo.save(new AgenciaBancaria(2, "Rua da Conceição, 50 - Centro", "Niterói"));

        Pessoa maria = criarCliente("529.982.247-25", "Maria Silva", 34);
        Pessoa joao = criarCliente("111.444.777-35", "João Souza", 28);

        abrirConta(1001, "1000.00", "2500.00", maria, centro);
        abrirConta(1002, "0.00", "800.00", maria, niteroi);
        abrirConta(2001, "500.00", "1200.00", joao, centro);

        log.info("Dados de exemplo criados. Clientes: 529.982.247-25 e 111.444.777-35 (senha: {})", SENHA_CLIENTES);
    }

    private Pessoa criarCliente(String cpf, String nome, int idade) {
        Pessoa pessoa = pessoaRepo.save(new Pessoa(cpf, nome, idade));
        usuarioRepo.save(new Usuario(cpf, encoder.encode(SENHA_CLIENTES), Role.CLIENTE, pessoa));
        return pessoa;
    }

    private void abrirConta(int numero, String limite, String saldoInicial, Pessoa dono, AgenciaBancaria agencia) {
        ContaBancaria conta = contaRepo.save(new ContaBancaria(numero, new BigDecimal(limite), dono, agencia));
        BigDecimal saldo = new BigDecimal(saldoInicial);
        conta.depositar(saldo);
        movRepo.save(new Movimentacao(conta, TipoMovimentacao.DEPOSITO, saldo, null));
    }
}

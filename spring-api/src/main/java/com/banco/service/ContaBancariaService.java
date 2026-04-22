package com.banco.service;

import com.banco.model.AgenciaBancaria;
import com.banco.model.ContaBancaria;
import com.banco.model.Pessoa;
import com.banco.repository.ContaBancariaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ContaBancariaService {

    private final ContaBancariaRepository repo;
    private final PessoaService pessoaService;
    private final AgenciaBancariaService agenciaService;

    public ContaBancariaService(ContaBancariaRepository repo,
                                PessoaService pessoaService,
                                AgenciaBancariaService agenciaService) {
        this.repo = repo;
        this.pessoaService = pessoaService;
        this.agenciaService = agenciaService;
    }

    public List<ContaBancaria> listarTodas() {
        return repo.findAll();
    }

    public ContaBancaria buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: id=" + id));
    }

    public ContaBancaria buscarPorNumero(int numero) {
        return repo.findByNumContaCorrente(numero)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada: numero=" + numero));
    }

    public ContaBancaria incluir(int numero, double limite, double saldo, Long pessoaId, Long agenciaId) {
        if (repo.existsByNumContaCorrente(numero))
            throw new IllegalArgumentException("Já existe uma conta com o número: " + numero);
        Pessoa pessoa = pessoaService.buscarPorId(pessoaId);
        AgenciaBancaria agencia = agenciaService.buscarPorId(agenciaId);
        return repo.save(new ContaBancaria(numero, limite, saldo, pessoa, agencia));
    }

    public ContaBancaria alterar(Long id, double limite, Long agenciaId) {
        ContaBancaria conta = buscarPorId(id);
        conta.setLimiteChequeEspecial(limite);
        conta.setAgencia(agenciaService.buscarPorId(agenciaId));
        return repo.save(conta);
    }

    public void remover(Long id) {
        if (!repo.existsById(id))
            throw new IllegalArgumentException("Conta não encontrada: id=" + id);
        repo.deleteById(id);
    }

    @Transactional
    public ContaBancaria depositar(Long id, double valor) {
        ContaBancaria conta = buscarPorId(id);
        conta.depositar(valor);
        return repo.save(conta);
    }

    @Transactional
    public ContaBancaria sacar(Long id, double valor) {
        ContaBancaria conta = buscarPorId(id);
        conta.sacar(valor);
        return repo.save(conta);
    }

    @Transactional
    public ContaBancaria transferir(Long origemId, Long destinoId, double valor) {
        ContaBancaria origem = buscarPorId(origemId);
        ContaBancaria destino = buscarPorId(destinoId);
        origem.transferir(valor, destino);
        repo.save(destino);
        return repo.save(origem);
    }
}

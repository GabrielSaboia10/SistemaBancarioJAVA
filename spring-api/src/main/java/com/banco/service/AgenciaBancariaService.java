package com.banco.service;

import com.banco.model.AgenciaBancaria;
import com.banco.repository.AgenciaBancariaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AgenciaBancariaService {

    private final AgenciaBancariaRepository repo;

    public AgenciaBancariaService(AgenciaBancariaRepository repo) {
        this.repo = repo;
    }

    public List<AgenciaBancaria> listarTodas() {
        return repo.findAll();
    }

    public AgenciaBancaria buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agência não encontrada: id=" + id));
    }

    public AgenciaBancaria buscarPorNumero(int numero) {
        return repo.findByNumero(numero)
                .orElseThrow(() -> new IllegalArgumentException("Agência não encontrada: numero=" + numero));
    }

    public AgenciaBancaria incluir(AgenciaBancaria agencia) {
        if (repo.existsByNumero(agencia.getNumero()))
            throw new IllegalArgumentException("Já existe uma agência com o número: " + agencia.getNumero());
        return repo.save(agencia);
    }

    public AgenciaBancaria alterar(Long id, AgenciaBancaria dados) {
        AgenciaBancaria existente = buscarPorId(id);
        existente.setEndereco(dados.getEndereco());
        existente.setCidade(dados.getCidade());
        return repo.save(existente);
    }

    public void remover(Long id) {
        if (!repo.existsById(id))
            throw new IllegalArgumentException("Agência não encontrada: id=" + id);
        repo.deleteById(id);
    }
}

package com.banco.service;

import com.banco.model.Pessoa;
import com.banco.repository.PessoaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PessoaService {

    private final PessoaRepository repo;

    public PessoaService(PessoaRepository repo) {
        this.repo = repo;
    }

    public List<Pessoa> listarTodas() {
        return repo.findAll();
    }

    public Pessoa buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pessoa não encontrada: id=" + id));
    }

    public Pessoa buscarPorCpf(String cpf) {
        return repo.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Pessoa não encontrada: CPF=" + cpf));
    }

    public Pessoa incluir(Pessoa pessoa) {
        if (repo.existsByCpf(pessoa.getCpf()))
            throw new IllegalArgumentException("Já existe uma pessoa com o CPF: " + pessoa.getCpf());
        return repo.save(pessoa);
    }

    public Pessoa alterar(Long id, Pessoa dados) {
        Pessoa existente = buscarPorId(id);
        existente.setNome(dados.getNome());
        existente.setIdade(dados.getIdade());
        return repo.save(existente);
    }

    public void remover(Long id) {
        if (!repo.existsById(id))
            throw new IllegalArgumentException("Pessoa não encontrada: id=" + id);
        repo.deleteById(id);
    }
}

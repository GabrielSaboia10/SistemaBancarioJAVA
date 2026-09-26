package com.banco.service;

import com.banco.dto.AgenciaDtos.*;
import com.banco.dto.PageResponse;
import com.banco.exception.ConflitoException;
import com.banco.exception.RecursoNaoEncontradoException;
import com.banco.model.AgenciaBancaria;
import com.banco.repository.AgenciaBancariaRepository;
import com.banco.repository.ContaBancariaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgenciaBancariaService {

    private static final String ENTIDADE = "AGENCIA";

    private final AgenciaBancariaRepository repo;
    private final ContaBancariaRepository contaRepo;
    private final AuditoriaService auditoria;

    public AgenciaBancariaService(AgenciaBancariaRepository repo, ContaBancariaRepository contaRepo,
                                  AuditoriaService auditoria) {
        this.repo = repo;
        this.contaRepo = contaRepo;
        this.auditoria = auditoria;
    }

    /** Busca pelo número exato (se for numérica) ou por parte do nome da cidade. */
    @Transactional(readOnly = true)
    public PageResponse<AgenciaResponse> listar(String busca, Pageable pageable) {
        if (busca == null || busca.isBlank()) {
            return PageResponse.de(repo.findAll(pageable), AgenciaResponse::de);
        }
        String termo = busca.trim();
        var page = termo.matches("\\d{1,4}")
                ? repo.findByNumero(Integer.parseInt(termo), pageable)
                : repo.findByCidadeContainingIgnoreCase(termo, pageable);
        return PageResponse.de(page, AgenciaResponse::de);
    }

    @Transactional(readOnly = true)
    public AgenciaResponse buscar(Long id) {
        return AgenciaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public AgenciaResponse criar(AgenciaRequest req) {
        if (repo.existsByNumero(req.numero())) {
            throw new ConflitoException("Já existe uma agência com o número " + req.numero());
        }
        AgenciaBancaria agencia = repo.save(new AgenciaBancaria(req.numero(), req.endereco().trim(), req.cidade().trim()));
        auditoria.registrar("CRIAR", ENTIDADE, agencia.getId(),
                "Cadastrou a agência " + agencia.getNumero() + " (" + agencia.getCidade() + ")");
        return AgenciaResponse.de(agencia);
    }

    @Transactional
    public AgenciaResponse atualizar(Long id, AgenciaRequest req) {
        AgenciaBancaria agencia = buscarEntidade(id);
        if (repo.existsByNumeroAndIdNot(req.numero(), id)) {
            throw new ConflitoException("Já existe uma agência com o número " + req.numero());
        }
        agencia.setNumero(req.numero());
        agencia.setEndereco(req.endereco().trim());
        agencia.setCidade(req.cidade().trim());
        auditoria.registrar("ATUALIZAR", ENTIDADE, id, "Alterou os dados da agência " + agencia.getNumero());
        return AgenciaResponse.de(agencia);
    }

    @Transactional
    public void remover(Long id) {
        AgenciaBancaria agencia = buscarEntidade(id);
        if (contaRepo.existsByAgenciaId(id)) {
            throw new ConflitoException("Esta agência possui contas vinculadas e não pode ser excluída");
        }
        repo.delete(agencia);
        auditoria.registrar("EXCLUIR", ENTIDADE, id,
                "Excluiu a agência " + agencia.getNumero() + " (" + agencia.getCidade() + ")");
    }

    AgenciaBancaria buscarEntidade(Long id) {
        return repo.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Agência", id));
    }
}

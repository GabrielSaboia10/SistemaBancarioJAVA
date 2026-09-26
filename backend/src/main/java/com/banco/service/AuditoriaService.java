package com.banco.service;

import com.banco.dto.AuditoriaResponse;
import com.banco.dto.PageResponse;
import com.banco.model.Auditoria;
import com.banco.repository.AuditoriaRepository;
import com.banco.security.UsuarioAutenticado;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Guarda quem fez cada ação administrativa. Roda na mesma transação da ação:
 * se a ação falhar, o registro também não é gravado.
 */
@Service
public class AuditoriaService {

    private final AuditoriaRepository repo;

    public AuditoriaService(AuditoriaRepository repo) {
        this.repo = repo;
    }

    public void registrar(String acao, String entidade, Long entidadeId, String descricao) {
        UsuarioAutenticado autor = UsuarioAutenticado.atual().orElse(null);
        repo.save(new Auditoria(autor != null ? autor.id() : null, autor != null ? autor.cpf() : null,
                acao, entidade, entidadeId, descricao));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditoriaResponse> listar(String entidade, Pageable pageable) {
        var page = (entidade == null || entidade.isBlank())
                ? repo.findAllByOrderByDataHoraDescIdDesc(pageable)
                : repo.findByEntidadeOrderByDataHoraDescIdDesc(entidade.toUpperCase(), pageable);
        return PageResponse.de(page, AuditoriaResponse::de);
    }
}

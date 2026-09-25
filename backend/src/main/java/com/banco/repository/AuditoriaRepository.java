package com.banco.repository;

import com.banco.model.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    Page<Auditoria> findAllByOrderByDataHoraDescIdDesc(Pageable pageable);

    Page<Auditoria> findByEntidadeOrderByDataHoraDescIdDesc(String entidade, Pageable pageable);
}

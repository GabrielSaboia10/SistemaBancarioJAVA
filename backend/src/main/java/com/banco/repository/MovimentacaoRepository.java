package com.banco.repository;

import com.banco.model.Movimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    /** Movimentações da conta no intervalo [de, ate), da mais recente para a mais antiga. */
    Page<Movimentacao> findByContaIdAndDataHoraGreaterThanEqualAndDataHoraLessThanOrderByDataHoraDescIdDesc(
            Long contaId, Instant de, Instant ate, Pageable pageable);

    @Modifying
    @Query("delete from Movimentacao m where m.conta.id = :contaId")
    void deleteByContaId(Long contaId);
}

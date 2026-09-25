package com.banco.repository;

import com.banco.model.ContaBancaria;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {

    @EntityGraph(attributePaths = {"correntista", "agencia"})
    Page<ContaBancaria> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = {"correntista", "agencia"})
    Page<ContaBancaria> findByCorrentistaId(Long pessoaId, Pageable pageable);

    @EntityGraph(attributePaths = {"correntista", "agencia"})
    Page<ContaBancaria> findByNumero(int numero, Pageable pageable);

    @EntityGraph(attributePaths = {"correntista", "agencia"})
    Page<ContaBancaria> findByCorrentistaIdAndNumero(Long pessoaId, int numero, Pageable pageable);

    @EntityGraph(attributePaths = {"correntista", "agencia"})
    Page<ContaBancaria> findByCorrentistaNomeContainingIgnoreCase(String nome, Pageable pageable);

    @EntityGraph(attributePaths = {"correntista", "agencia"})
    Optional<ContaBancaria> findWithDetalhesById(Long id);

    Optional<ContaBancaria> findByNumero(int numero);

    /**
     * Carrega a conta com SELECT ... FOR UPDATE. Duas operações simultâneas na mesma
     * conta ficam em fila, então o saldo nunca é validado com um valor desatualizado.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ContaBancaria c where c.id = :id")
    Optional<ContaBancaria> findByIdParaAtualizacao(Long id);

    boolean existsByNumero(int numero);

    boolean existsByCorrentistaId(Long pessoaId);

    boolean existsByAgenciaId(Long agenciaId);

    long countByCorrentistaId(Long pessoaId);

    @Query("select coalesce(sum(c.saldo), 0) from ContaBancaria c")
    BigDecimal somarSaldos();

    @Query("select coalesce(sum(c.saldo), 0) from ContaBancaria c where c.correntista.id = :pessoaId")
    BigDecimal somarSaldosPorPessoa(Long pessoaId);
}

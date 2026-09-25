package com.banco.repository;

import com.banco.model.AgenciaBancaria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgenciaBancariaRepository extends JpaRepository<AgenciaBancaria, Long> {

    boolean existsByNumero(int numero);

    boolean existsByNumeroAndIdNot(int numero, Long id);

    Page<AgenciaBancaria> findByNumero(int numero, Pageable pageable);

    Page<AgenciaBancaria> findByCidadeContainingIgnoreCase(String cidade, Pageable pageable);
}

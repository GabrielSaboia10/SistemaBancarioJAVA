package com.banco.repository;

import com.banco.model.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {
    Optional<ContaBancaria> findByNumContaCorrente(int numero);
    boolean existsByNumContaCorrente(int numero);
}

package com.banco.repository;

import com.banco.model.AgenciaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AgenciaBancariaRepository extends JpaRepository<AgenciaBancaria, Long> {
    Optional<AgenciaBancaria> findByNumero(int numero);
    boolean existsByNumero(int numero);
}

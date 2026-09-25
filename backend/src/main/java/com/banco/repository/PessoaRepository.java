package com.banco.repository;

import com.banco.model.Pessoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    boolean existsByCpf(String cpf);

    Page<Pessoa> findByNomeContainingIgnoreCaseOrCpfContaining(String nome, String cpf, Pageable pageable);
}

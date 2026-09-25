package com.banco.repository;

import com.banco.model.Role;
import com.banco.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = "pessoa")
    Optional<Usuario> findByCpf(String cpf);

    @EntityGraph(attributePaths = "pessoa")
    Optional<Usuario> findWithPessoaById(Long id);

    boolean existsByCpf(String cpf);

    Optional<Usuario> findByPessoa_Id(Long pessoaId);

    List<Usuario> findByPessoa_IdIn(Collection<Long> pessoaIds);

    /** Consulta enxuta usada a cada requisição autenticada (bloqueio e senha temporária). */
    @Query("select u.ativo as ativo, u.trocarSenha as trocarSenha from Usuario u where u.id = :id")
    Optional<StatusUsuario> findStatusById(Long id);

    interface StatusUsuario {
        boolean getAtivo();
        boolean getTrocarSenha();
    }

    boolean existsByRole(Role role);

    @Modifying
    @Query("delete from Usuario u where u.pessoa.id = :pessoaId")
    void deleteByPessoaId(Long pessoaId);
}

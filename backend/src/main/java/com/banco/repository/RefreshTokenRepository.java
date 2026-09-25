package com.banco.repository;

import com.banco.model.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @EntityGraph(attributePaths = {"usuario", "usuario.pessoa"})
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from RefreshToken r where r.tokenHash = :tokenHash")
    void deleteByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from RefreshToken r where r.usuario.id = :usuarioId")
    void deleteByUsuarioId(Long usuarioId);

    @Modifying
    @Query("delete from RefreshToken r where r.expiraEm < :agora")
    int deleteExpirados(Instant agora);
}

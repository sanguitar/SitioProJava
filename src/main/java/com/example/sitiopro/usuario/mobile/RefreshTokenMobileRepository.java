package com.example.sitiopro.usuario.mobile;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenMobileRepository extends JpaRepository<RefreshTokenMobile, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshTokenMobile t join fetch t.usuario where t.tokenHash = :hash")
    Optional<RefreshTokenMobile> buscarPorHashParaAtualizacao(@Param("hash") String hash);

    boolean existsBySessaoIdAndUsuarioIdAndRevogadoEmIsNullAndExpiraEmAfter(
            String sessaoId, Long usuarioId, LocalDateTime agora);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RefreshTokenMobile t
               set t.revogadoEm = :agora, t.motivoRevogacao = :motivo
             where t.sessaoId = :sessaoId
               and t.usuario.id = :usuarioId
               and t.revogadoEm is null
            """)
    int revogarSessao(@Param("sessaoId") String sessaoId, @Param("usuarioId") Long usuarioId,
            @Param("agora") LocalDateTime agora, @Param("motivo") String motivo);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RefreshTokenMobile t
               set t.revogadoEm = :agora, t.motivoRevogacao = :motivo
             where t.usuario.id = :usuarioId
               and t.revogadoEm is null
            """)
    int revogarTodosDoUsuario(@Param("usuarioId") Long usuarioId, @Param("agora") LocalDateTime agora,
            @Param("motivo") String motivo);
}
